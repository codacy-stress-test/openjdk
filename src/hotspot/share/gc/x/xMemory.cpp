/*
 * Copyright (c) 2015, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include "precompiled.hpp"
#include "gc/x/xList.inline.hpp"
#include "gc/x/xLock.inline.hpp"
#include "gc/x/xMemory.inline.hpp"

XMemory* XMemoryManager::create(uintptr_t start, size_t size) {
  XMemory* const area = new XMemory(start, size);
  if (_callbacks._create != nullptr) {
    _callbacks._create(area);
  }
  return area;
}

void XMemoryManager::destroy(XMemory* area) {
  if (_callbacks._destroy != nullptr) {
    _callbacks._destroy(area);
  }
  delete area;
}

void XMemoryManager::shrink_from_front(XMemory* area, size_t size) {
  if (_callbacks._shrink_from_front != nullptr) {
    _callbacks._shrink_from_front(area, size);
  }
  area->shrink_from_front(size);
}

void XMemoryManager::shrink_from_back(XMemory* area, size_t size) {
  if (_callbacks._shrink_from_back != nullptr) {
    _callbacks._shrink_from_back(area, size);
  }
  area->shrink_from_back(size);
}

void XMemoryManager::grow_from_front(XMemory* area, size_t size) {
  if (_callbacks._grow_from_front != nullptr) {
    _callbacks._grow_from_front(area, size);
  }
  area->grow_from_front(size);
}

void XMemoryManager::grow_from_back(XMemory* area, size_t size) {
  if (_callbacks._grow_from_back != nullptr) {
    _callbacks._grow_from_back(area, size);
  }
  area->grow_from_back(size);
}

XMemoryManager::Callbacks::Callbacks() :
    _create(nullptr),
    _destroy(nullptr),
    _shrink_from_front(nullptr),
    _shrink_from_back(nullptr),
    _grow_from_front(nullptr),
    _grow_from_back(nullptr) {}

XMemoryManager::XMemoryManager() :
    _freelist(),
    _callbacks() {}

void XMemoryManager::register_callbacks(const Callbacks& callbacks) {
  _callbacks = callbacks;
}

uintptr_t XMemoryManager::peek_low_address() const {
  XLocker<XLock> locker(&_lock);

  const XMemory* const area = _freelist.first();
  if (area != nullptr) {
    return area->start();
  }

  // Out of memory
  return UINTPTR_MAX;
}

uintptr_t XMemoryManager::alloc_low_address(size_t size) {
  XLocker<XLock> locker(&_lock);

  XListIterator<XMemory> iter(&_freelist);
  for (XMemory* area; iter.next(&area);) {
    if (area->size() >= size) {
      if (area->size() == size) {
        // Exact match, remove area
        const uintptr_t start = area->start();
        _freelist.remove(area);
        destroy(area);
        return start;
      } else {
        // Larger than requested, shrink area
        const uintptr_t start = area->start();
        shrink_from_front(area, size);
        return start;
      }
    }
  }

  // Out of memory
  return UINTPTR_MAX;
}

uintptr_t XMemoryManager::alloc_low_address_at_most(size_t size, size_t* allocated) {
  XLocker<XLock> locker(&_lock);

  XMemory* area = _freelist.first();
  if (area != nullptr) {
    if (area->size() <= size) {
      // Smaller than or equal to requested, remove area
      const uintptr_t start = area->start();
      *allocated = area->size();
      _freelist.remove(area);
      destroy(area);
      return start;
    } else {
      // Larger than requested, shrink area
      const uintptr_t start = area->start();
      shrink_from_front(area, size);
      *allocated = size;
      return start;
    }
  }

  // Out of memory
  *allocated = 0;
  return UINTPTR_MAX;
}

uintptr_t XMemoryManager::alloc_high_address(size_t size) {
  XLocker<XLock> locker(&_lock);

  XListReverseIterator<XMemory> iter(&_freelist);
  for (XMemory* area; iter.next(&area);) {
    if (area->size() >= size) {
      if (area->size() == size) {
        // Exact match, remove area
        const uintptr_t start = area->start();
        _freelist.remove(area);
        destroy(area);
        return start;
      } else {
        // Larger than requested, shrink area
        shrink_from_back(area, size);
        return area->end();
      }
    }
  }

  // Out of memory
  return UINTPTR_MAX;
}

void XMemoryManager::free(uintptr_t start, size_t size) {
  assert(start != UINTPTR_MAX, "Invalid address");
  const uintptr_t end = start + size;

  XLocker<XLock> locker(&_lock);

  XListIterator<XMemory> iter(&_freelist);
  for (XMemory* area; iter.next(&area);) {
    if (start < area->start()) {
      XMemory* const prev = _freelist.prev(area);
      if (prev != nullptr && start == prev->end()) {
        if (end == area->start()) {
          // Merge with prev and current area
          grow_from_back(prev, size + area->size());
          _freelist.remove(area);
          delete area;
        } else {
          // Merge with prev area
          grow_from_back(prev, size);
        }
      } else if (end == area->start()) {
        // Merge with current area
        grow_from_front(area, size);
      } else {
        // Insert new area before current area
        assert(end < area->start(), "Areas must not overlap");
        XMemory* const new_area = create(start, size);
        _freelist.insert_before(area, new_area);
      }

      // Done
      return;
    }
  }

  // Insert last
  XMemory* const last = _freelist.last();
  if (last != nullptr && start == last->end()) {
    // Merge with last area
    grow_from_back(last, size);
  } else {
    // Insert new area last
    XMemory* const new_area = create(start, size);
    _freelist.insert_last(new_area);
  }
}

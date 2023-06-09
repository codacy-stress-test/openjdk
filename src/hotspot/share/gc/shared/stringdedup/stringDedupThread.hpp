/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef SHARE_GC_SHARED_STRINGDEDUP_STRINGDEDUPTHREAD_HPP
#define SHARE_GC_SHARED_STRINGDEDUP_STRINGDEDUPTHREAD_HPP

#include "gc/shared/stringdedup/stringDedup.hpp"
#include "runtime/javaThread.hpp"
#include "utilities/exceptions.hpp"
#include "utilities/macros.hpp"

// Thread class for string deduplication.  There is only one instance of this
// class.  This class provides thread management.  It uses the Processor
// to perform most of the work.
//
// Unlike most of the classes in the stringdedup implementation, this class is
// not an inner class of StringDedup.  This is because we need a simple public
// identifier for use by VMStructs.
class StringDedupThread : public JavaThread {
  friend class VMStructs;

  StringDedupThread();
  ~StringDedupThread() = default;

  NONCOPYABLE(StringDedupThread);

  static void thread_entry(JavaThread* thread, TRAPS);

public:
  static void initialize();

  bool is_hidden_from_external_view() const override;
};

#endif // SHARE_GC_SHARED_STRINGDEDUP_STRINGDEDUPTHREAD_HPP

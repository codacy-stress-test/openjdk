/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package jdk.vm.ci.code.stack;

import jdk.vm.ci.meta.ResolvedJavaMethod;

public interface StackIntrospection {

    /**
     * Walks the current stack, providing {@link InspectedFrame}s to the visitor that can be used to
     * inspect the stack frame's contents. Iteration continues as long as
     * {@link InspectedFrameVisitor#visitFrame}, which is invoked for every {@link InspectedFrame},
     * returns {@code null}. A non-null return value from {@link InspectedFrameVisitor#visitFrame}
     * indicates that frame iteration should stop.
     *
     * @param initialMethods if this is non-{@code null}, then the stack walk will start at the
     *            first frame whose method is one of these methods.
     * @param matchingMethods if this is non-{@code null}, then only frames whose methods are in
     *            this array are visited
     * @param initialSkip the number of matching methods to skip (including the initial method)
     * @param visitor the visitor that is called for every matching method
     * @return the last result returned by the visitor (which is non-null to indicate that iteration
     *         should stop), or null if the whole stack was iterated.
     */
    <T> T iterateFrames(ResolvedJavaMethod[] initialMethods, ResolvedJavaMethod[] matchingMethods, int initialSkip, InspectedFrameVisitor<T> visitor);

    /**
     * Determines if {@link InspectedFrame#materializeVirtualObjects(boolean)} can be called for frames
     * visited by {@link #iterateFrames}.
     */
    default boolean canMaterializeVirtualObjects() {
        return true;
    }
}

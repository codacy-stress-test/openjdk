/*
 * Copyright (c) 2017, 2023, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8046171
 * @summary Test access to private static methods between nestmates and nest-host
 *          using different flavours of named nested types using core reflection
 * @run main TestReflection
 */

// The first run will use NativeMethodAccessor and due to the limited number
// of calls we will not reach the inflation threshold.
// The second run disables inflation so we will use the GeneratedMethodAccessor
// instead. In this way both sets of Reflection classes are tested.


public class TestReflection {

    // Private method of nest-host for nestmates to access
    private static void priv_static_invoke() {
        System.out.println("TestReflection::priv_static_invoke");
    }

    // public constructor so we aren't relying on private access
    public TestReflection() {}

    // Methods that will access private static methods of nestmates

    // NOTE: No InnerNested calls in this test because non-static nested types
    // can't have static methods

    void access_priv(TestReflection o) throws Throwable {
        o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
    }
    void access_priv(StaticNested o) throws Throwable {
        o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
    }
    void access_priv(StaticIface o) throws Throwable {
        // Can't use o.getClass() as the method is not in that class
        StaticIface.class.getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
    }

    // The various nestmates

    static interface StaticIface {

        private static void priv_static_invoke() {
            System.out.println("StaticIface::priv_static_invoke");
        }

        // Methods that will access private static methods of nestmates

        default void access_priv(TestReflection o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        default void access_priv(StaticNested o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        default void access_priv(StaticIface o) throws Throwable {
            // Can't use o.getClass() as the method is not in that class
            StaticIface.class.getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
    }

    static class StaticNested {

        private static void priv_static_invoke() {
            System.out.println("StaticNested::priv_static_invoke");
        }

        // public constructor so we aren't relying on private access
        public StaticNested() {}

        // Methods that will access private static methods of nestmates

        void access_priv(TestReflection o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        void access_priv(StaticNested o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        void access_priv(StaticIface o) throws Throwable {
            // Can't use o.getClass() as the method is not in that class
            StaticIface.class.getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
    }

    class InnerNested {

        // public constructor so we aren't relying on private access
        public InnerNested() {}

        void access_priv(TestReflection o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        void access_priv(StaticNested o) throws Throwable {
            o.getClass().getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
        void access_priv(StaticIface o) throws Throwable {
            // Can't use o.getClass() as the method is not in that class
            StaticIface.class.getDeclaredMethod("priv_static_invoke", new Class<?>[0]).invoke(null, new Object[0]);
        }
    }

    public static void main(String[] args) throws Throwable {
        TestReflection o = new TestReflection();
        StaticNested s = new StaticNested();
        InnerNested i = o.new InnerNested();
        StaticIface intf = new StaticIface() {};

        o.access_priv(new TestReflection());
        o.access_priv(s);
        o.access_priv(intf);

        s.access_priv(o);
        s.access_priv(new StaticNested());
        s.access_priv(intf);

        i.access_priv(o);
        i.access_priv(s);
        i.access_priv(intf);

        intf.access_priv(o);
        intf.access_priv(s);
        intf.access_priv(new StaticIface(){});
    }
}

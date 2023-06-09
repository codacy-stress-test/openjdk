/*
 * Copyright (c) 2012, 2023, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 7021614 8273244 6934301
 * @summary extend com.sun.source API to support parsing javadoc comments
 * @modules jdk.compiler/com.sun.tools.javac.api
 *          jdk.compiler/com.sun.tools.javac.file
 *          jdk.compiler/com.sun.tools.javac.tree
 *          jdk.compiler/com.sun.tools.javac.util
 * @build DocCommentTester
 * @run main DocCommentTester InheritDocTest.java
 */

class InheritDocTest {
    /** abc {@inheritDoc} */
    void standard() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    InheritDoc[INHERIT_DOC, pos:4
      supertype: null
    ]
  body: empty
  block tags: empty
]
*/

    /** abc {@inheritDoc } */
    void standard_ws1() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    InheritDoc[INHERIT_DOC, pos:4
      supertype: null
    ]
  body: empty
  block tags: empty
]
*/

    /** abc {@inheritDoc  } */
    void standard_ws2() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    InheritDoc[INHERIT_DOC, pos:4
      supertype: null
    ]
  body: empty
  block tags: empty
]
*/

    /** abc {@inheritDoc String} */
    void simple() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    InheritDoc[INHERIT_DOC, pos:4
      supertype:
        Reference[REFERENCE, pos:17, String]
    ]
  body: empty
  block tags: empty
]
*/

    /** abc {@inheritDoc java.util.List<E>} */
    void fullyQualifiedTypeWithWildCardUpperBound() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    InheritDoc[INHERIT_DOC, pos:4
      supertype:
        Reference[REFERENCE, pos:17, java.util.List<E>]
    ]
  body: empty
  block tags: empty
]
*/

    /** abc {@inheritDoc Integer   Number} */
    void unexpectedContentAfterReference() { }
/*
DocComment[DOC_COMMENT, pos:0
  firstSentence: 2
    Text[TEXT, pos:0, abc_]
    Erroneous[ERRONEOUS, pos:4, prefPos:27
      code: compiler.err.dc.unexpected.content
      body: {@inheritDoc_Integer___Number}
    ]
  body: empty
  block tags: empty
]
*/

}

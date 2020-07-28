package org.move.lang.parser

import org.move.utils.tests.parser.MvParsingTestCase

class CompleteParsingTest : MvParsingTestCase("complete") {
    fun `test while loop inline assignment`() = doTest(true)
    fun `test contextual token operators`() = doTest(true)
    fun `test generics`() = doTest(true)
    fun `test annotated literals`() = doTest(true)
    fun `test struct fields`() = doTest(true)
    fun `test assignment lhs`() = doTest(true)
    fun `test function calls`() = doTest(true)
    fun `test expressions`() = doTest(true)
    fun `test let`() = doTest(true)
}
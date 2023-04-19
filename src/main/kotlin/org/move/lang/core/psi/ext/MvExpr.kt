package org.move.lang.core.psi.ext

import org.move.lang.core.psi.*

val MvExpr.isAtomExpr: Boolean get() =
    this is MvAnnotatedExpr
            || this is MvTupleLitExpr
            || this is MvParensExpr
            || this is MvVectorLitExpr
            || this is MvDotExpr
            || this is MvIndexExpr
            || this is MvCallExpr
            || this is MvRefExpr
            || this is MvLambdaExpr
            || this is MvLitExpr
            || this is MvCodeBlockExpr

package org.move.lang.core.types.infer

import org.move.lang.core.types.ty.Ty
import org.move.lang.core.types.ty.TyInfer
import org.move.lang.core.types.ty.TyReference
import org.move.lang.core.types.ty.TyVector

sealed class Constraint: TypeFoldable<Constraint> {
    /** `T1 == T2` */
    data class Equate(val ty1: Ty, val ty2: Ty): Constraint() {
        override fun innerFoldWith(folder: TypeFolder): Constraint =
            Equate(ty1.foldWith(folder), ty2.foldWith(folder))

        override fun innerVisitWith(visitor: TypeVisitor): Boolean =
            ty1.visitWith(visitor) || ty2.visitWith(visitor)

        override fun toString(): String =
            "$ty1 == $ty2"
    }
}

data class Obligation(val constraint: Constraint)

class ObligationForest {
    enum class NodeState {
        /** Obligations for which selection had not yet returned a non-ambiguous result */
        Pending,
        /** This obligation was selected successfully, but may or may not have subobligations */
        Success,
        /** This obligation was resolved to an error. Error nodes are removed from the vector by the compression step */
        Error,
    }

    class Node(val obligation: Obligation) {
        var state: NodeState = NodeState.Pending
    }

    private val nodes: MutableList<Node> = mutableListOf()

    val obligations: Sequence<Obligation> =
        nodes.asSequence().filter { it.state == NodeState.Pending }.map { it.obligation }

    fun registerObligationAt(constraint: Obligation) {
        nodes.add(Node(constraint))
    }

    fun processObligations(processor: (Obligation) -> ProcessPredicateResult): Boolean {
        var stalled = true
        for (node in nodes) {
            if (node.state != NodeState.Pending) continue
            val result = processor(node.obligation)
            when (result) {
                is ProcessPredicateResult.NoChanges -> {}
                is ProcessPredicateResult.Ok -> {
                    stalled = false
                    node.state = NodeState.Success
                    for (child in result.children) {
                        registerObligationAt(child)
                    }
                }
                is ProcessPredicateResult.Err -> {
                    stalled = false
                    node.state = NodeState.Error
                }
            }
        }
        return stalled
    }
}

class ConstraintSolver(val unificationTable: UnificationTable<TyInfer.TyVar, Ty>) {
    private val constraints = mutableListOf<Constraint>()

    fun registerConstraint(constraint: Constraint) {
        constraints.add(constraint)
    }

    fun processConstraints() {
        while (constraints.isNotEmpty()) {
            val constraint = constraints.removeFirst()
            processConstraint(constraint)
        }
    }

    private fun processConstraint(constraint: Constraint) {
//        val resolvedConstraint = fctx.foldResolvingTyInfersFromCurrentContext(constraint)
        when (constraint) {
            is Constraint.Equate -> {
                val ty1 = constraint.ty1
                val ty2 = constraint.ty2
                when {
                    ty1 is TyInfer.TyVar && ty2 is TyInfer.TyVar -> {
                        unificationTable.unifyVarVar(ty1, ty2)
                    }
                    ty1 is TyInfer.TyVar && ty2 !is TyInfer.TyVar -> {
                        unificationTable.unifyVarValue(ty1, ty2)
                    }
                    ty2 is TyInfer.TyVar && ty1 !is TyInfer.TyVar -> {
                        unificationTable.unifyVarValue(ty2, ty1)
                    }
                    else -> {
                        when {
                            ty1 is TyVector && ty2 is TyVector -> {
                                registerConstraint(Constraint.Equate(ty1.item, ty2.item))
                            }
                            ty1 is TyReference && ty2 is TyReference -> {
                                registerConstraint(Constraint.Equate(ty1.referenced, ty2.referenced))
                            }
                            else -> {
                            // TODO: add
                            // error("type == type should not occur for now")
                            }
                        }
                    }
                }
            }
        }
    }

//    fun processObligationsWherePossible() {
//        while (!obligations.processObligations(this::processPredicate)) {}
//    }
}

sealed class ProcessPredicateResult {
    object Err: ProcessPredicateResult()
    object NoChanges: ProcessPredicateResult()
    data class Ok(val children: List<Obligation>): ProcessPredicateResult() {
        constructor(vararg children: Obligation): this(listOf(*children))
    }
}

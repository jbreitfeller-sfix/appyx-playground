package com.justinbreitfeller.appyxplayground

import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.active
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.BackStackOperation
import com.bumble.appyx.navmodel.backstack.operation.Push
import com.bumble.appyx.navmodel.backstack.operation.Remove
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed class BringToFront<T : Any> : BackStackOperation<T> {

    @Parcelize
    class RemoveThenPush<T : Any>(
        private val remove: BackStackOperation<T>,
        private val push: BackStackOperation<T>
    ) : BringToFront<T>() {
        override fun isApplicable(elements: NavElements<T, BackStack.State>): Boolean {
            return remove.isApplicable(elements = elements)
        }

        override fun invoke(elements: NavElements<T, BackStack.State>): NavElements<T, BackStack.State> {
            val result = remove.invoke(elements)
            return push.invoke(result)
        }
    }

    @Parcelize
    class MoveToFrontOperation<T : Any>(
        private val element: @RawValue T,
    ) : BringToFront<T>() {

        override fun isApplicable(elements: BackStackElements<T>): Boolean =
            element != elements.activeElement

        override fun invoke(
            elements: BackStackElements<T>
        ): BackStackElements<T> {
            val current = elements.active
            requireNotNull(current)
            val existing = elements.firstOrNull { it.key.navTarget == element }
            requireNotNull(existing)

            val newElements = elements.toMutableList().also { it.remove(existing) }

            return newElements.transitionTo(BackStack.State.STASHED) {
                it.targetState == BackStack.State.ACTIVE
            } + existing.transitionTo(BackStack.State.ACTIVE, this)
        }

        override fun equals(other: Any?): Boolean = this.javaClass == other?.javaClass

        override fun hashCode(): Int = this.javaClass.hashCode()
    }

    companion object {
        fun <T : Any> init(
            element: T,
            elements: BackStackElements<T>,
            clearState: Boolean,
        ): BackStackOperation<T> {
            val existing = elements.firstOrNull { it.key.navTarget == element }
            return if (existing == null) {
                Push(element = element)
            } else if (clearState) {
                RemoveThenPush(Remove(existing.key), Push(element = element))
            } else {
                MoveToFrontOperation(element)
            }
        }
    }
}

fun <T : Any> BackStack<T>.bringToFront(element: T, clearState: Boolean) {
    accept(BringToFront.init(element, elements.value, clearState))
}

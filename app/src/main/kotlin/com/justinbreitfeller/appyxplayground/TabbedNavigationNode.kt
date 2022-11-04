package com.justinbreitfeller.appyxplayground

import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.push
import kotlinx.parcelize.Parcelize

class TabbedNavigationNode(
    buildContext: BuildContext,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.TabA,
        savedStateMap = buildContext.savedStateMap,
    ),
    private val oneNodePerTab: Boolean = true,
    private val application: Application?,
) : ParentNode<TabbedNavigationNode.NavTarget>(
    navModel = backStack,
    buildContext = buildContext
) {
    companion object {
        val targets = listOf(
            NavTarget.TabA,
            NavTarget.TabB,
            NavTarget.TabC,
            NavTarget.NestedTab,
        )
    }

    sealed class NavTarget : Parcelable {
        abstract val name: String

        @Parcelize
        object TabA : NavTarget() {
            override val name: String
                get() = "A"
        }

        @Parcelize
        object TabB : NavTarget() {
            override val name: String
                get() = "B"
        }

        @Parcelize
        object TabC : NavTarget() {
            override val name: String
                get() = "C"
        }

        @Parcelize
        object NestedTab : NavTarget() {
            override val name: String
                get() = "Nested"
        }
    }

    @ExperimentalUnitApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            NavTarget.TabA,
            NavTarget.TabB,
            NavTarget.TabC ->
                hiltNode(
                    buildContext = buildContext,
                    application = application,
                    defaultArgs = Bundle().apply {
                        putString(
                            TestViewModel.NAV_TARGET_NAME_ARG,
                            navTarget.name
                        )
                    }
                ) {
                    Child()
                }

            NavTarget.NestedTab -> TabbedNavigationNode(
                buildContext = buildContext,
                oneNodePerTab = false,
                application = application
            )
        }


    @Composable
    fun Child(testViewModel: TestViewModel = appyxHiltViewModel()) {
        val state by testViewModel.state.collectAsState()
        Column {
            Text(text = "You are on nav target: ${state.name}")
            Text(text = "VM saved state: ${state.savedMessage}")
        }

    }


    @Composable
    override fun View(modifier: Modifier) {
        Scaffold(
            bottomBar = { BottomNavigationBar() },
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
                    .padding(top = 10.dp)
            ) {
                BackStack()
                Children(
                    modifier = Modifier.fillMaxSize(),
                    navModel = backStack
                )
            }
        }
    }


    @Composable
    private fun BackStack() {
        val elementsState by backStack.elements.collectAsState()
        val names = elementsState
            .filter { it.targetState != BackStack.State.DESTROYED }
            .map { it.key.navTarget.name }
        Row {
            names.forEachIndexed { index, text ->
                Text(text)
                if (index != names.lastIndex)
                    Text("->")
            }
        }
    }

    @Composable
    private fun BottomNavigationBar() {
        val elementsState by backStack.elements.collectAsState()
        val prefix = if (oneNodePerTab) "Tab " else ""
        BottomNavigation {
            targets.forEach {
                val selected = it == elementsState.activeElement
                BottomNavigationItem(
                    selected = selected,
                    onClick = {
                        if (oneNodePerTab) {
                            backStack.bringToFront(
                                element = it,
                                clearState = selected,
                            )
                        } else {
                            backStack.push(it)
                        }
                    },
                    icon = { Text(prefix + it.name) })
            }
        }
    }
}


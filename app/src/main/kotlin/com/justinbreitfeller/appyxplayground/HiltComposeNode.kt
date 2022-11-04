package com.justinbreitfeller.appyxplayground

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.state.MutableSavedStateMap
import kotlin.collections.set

private const val SAVED_STATE_REGISTRY_KEY = "SAVED_STATE_REGISTRY"

class HiltComposeNode(
    buildContext: BuildContext,
    application: Application?,
    defaultArgs: Bundle? = null,
    private val composable: @Composable (Modifier) -> Unit
) : Node(buildContext), HasHiltSupport {

    override val hiltSupport = HiltSupport(
        lifecycle = lifecycle,
        initialSavedState = buildContext.savedStateMap?.get(SAVED_STATE_REGISTRY_KEY) as Bundle?,
        application = application,
        defaultArgs = defaultArgs
    )

    override fun onSaveInstanceState(state: MutableSavedStateMap) {
        super.onSaveInstanceState(state)
        state[SAVED_STATE_REGISTRY_KEY] = hiltSupport.saveState()
    }

    @Composable
    override fun View(modifier: Modifier) {
        composable(modifier)
    }
}


fun hiltNode(
    buildContext: BuildContext,
    application: Application?,
    defaultArgs: Bundle? = null,
    composable: @Composable (Modifier) -> Unit
): Node = HiltComposeNode(buildContext, application, defaultArgs, composable)

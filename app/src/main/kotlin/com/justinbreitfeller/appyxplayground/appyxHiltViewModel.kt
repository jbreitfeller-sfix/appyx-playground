package com.justinbreitfeller.appyxplayground

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumble.appyx.core.node.LocalNode
import com.bumble.appyx.core.node.Node
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory

@Composable
inline fun <reified VM : ViewModel> appyxHiltViewModel(
    node: Node = checkNotNull(LocalNode.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): VM {
    require(node is HasHiltSupport) { "Node must implement HasHiltSupport" }
    val hiltNode = node as HasHiltSupport
    val factory = createHiltViewModelFactory(hiltNode)
    return viewModel(hiltNode.hiltSupport, factory = factory)
}

@Composable
@PublishedApi
internal fun createHiltViewModelFactory(
    node: HasHiltSupport
): ViewModelProvider.Factory? {
    val context = LocalContext.current
    val activity = context.let {
        var ctx = it
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return@let ctx
            }
            ctx = ctx.baseContext
        }
        throw IllegalStateException(
            "Expected an activity context for creating a HiltViewModelFactory for a " +
                    "Node but instead found: $ctx"
        )
    }
    return HiltViewModelFactory.createInternal(
        /* activity = */
        activity,
        /* SavedStateRegistryOwner*/
        node.hiltSupport,
        /* defaultArgs*/
        node.hiltSupport.defaultArgs,
        /* delegateFactory */
        node.hiltSupport.defaultViewModelProviderFactory,
    )
}

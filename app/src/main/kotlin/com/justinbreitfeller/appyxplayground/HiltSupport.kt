package com.justinbreitfeller.appyxplayground

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class HiltSupport(
    private val lifecycle: Lifecycle,
    private val initialSavedState: Bundle?,
    private val application: Application?,
    val defaultArgs: Bundle? = null,
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory, SavedStateRegistryOwner {

    private val viewModelStore = ViewModelStore()
    private val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    //Don't replace the initial saved state until we have at least started
    private var canSaveState: Boolean = false

    init {
        savedStateRegistryController.performAttach()

        // We copy the bundle because the `savedStateRegistryController` will modify it.
        // We don't want to modify `initialSavedState` since we may need to return that as our
        // state in `saveState`.
        savedStateRegistryController.performRestore(initialSavedState?.let { Bundle(it) })
        enableSavedStateHandles()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                canSaveState = true
            }

            override fun onDestroy(owner: LifecycleOwner) {
                viewModelStore.clear()
            }
        })
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return SavedStateViewModelFactory(application, this, defaultArgs)
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        val extras = MutableCreationExtras()
        application?.let { application ->
            extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] = application
        }
        extras[SAVED_STATE_REGISTRY_OWNER_KEY] = this
        extras[VIEW_MODEL_STORE_OWNER_KEY] = this
        defaultArgs?.let { args ->
            extras[DEFAULT_ARGS_KEY] = args
        }
        return extras
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    fun saveState(): Bundle? {
        return if (canSaveState) {
            Bundle().also(savedStateRegistryController::performSave)
        } else {
            initialSavedState
        }
    }
}

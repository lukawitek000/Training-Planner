package com.lukasz.witkowski.training.planner.navigation

import androidx.lifecycle.ViewModelStoreOwner

class NavEntryOwnerRegistry {
    private val owners = mutableMapOf<Any, ViewModelStoreOwner>()

    fun register(key: Any, owner: ViewModelStoreOwner) {
        owners[key] = owner
    }

    fun get(key: Any): ViewModelStoreOwner {
        return requireNotNull(owners[key]) {
            "No ViewModelStoreOwner registered for key: $key. Ensure the root screen is on the backstack."
        }
    }

    fun remove(key: Any) {
        owners.remove(key)
    }
}
package com.lukasz.witkowski.training.planner.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidUserPreferencesStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AndroidUserPreferencesStorageTest {

    private lateinit var context: Context
    private lateinit var userPreferencesStorage: AndroidUserPreferencesStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userPreferencesStorage = AndroidUserPreferencesStorage(context)
    }

    @Test
    fun defaultWeightUnitIsKg() = runTest {
        val initialUnit = userPreferencesStorage.weightUnit.first()
        assertEquals(WeightUnit.KG, initialUnit)
    }

    @Test
    fun setWeightUnitPersistsNewValue() = runTest {
        userPreferencesStorage.setWeightUnit(WeightUnit.LBS)

        val updatedUnit = userPreferencesStorage.weightUnit.first()
        assertEquals(WeightUnit.LBS, updatedUnit)

        userPreferencesStorage.setWeightUnit(WeightUnit.KG)
    }
}

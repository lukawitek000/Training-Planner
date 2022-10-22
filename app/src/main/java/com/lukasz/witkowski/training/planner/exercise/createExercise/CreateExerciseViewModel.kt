package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.CategoriesCollection
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseMapper
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageMapper
import com.lukasz.witkowski.training.planner.image.ImageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/** StateFlow with SavedStateHandle, do I need it?
 * https://medium.com/mobile-app-development-publication/saving-stateflow-state-in-viewmodel-2ee9ed9b1a83
 */

@HiltViewModel
open class CreateExerciseViewModel @Inject constructor(
    private val exerciseService: ExerciseService,
    private val categoriesCollection: CategoriesCollection,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CategoriesCollection by categoriesCollection {

    private val _exerciseId = savedStateHandle.get<String>("exerciseId")
    protected var exerciseId = _exerciseId?.let { ExerciseId(it) } ?: ExerciseId.create()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _category = MutableStateFlow(Category())
    val category: StateFlow<Category> = _category

    private val _image = MutableStateFlow<Image?>(null)
    val image: StateFlow<Image?> = _image

    protected val _savingState = MutableStateFlow<ResultHandler<Boolean>>(ResultHandler.Idle)
    val savingState: StateFlow<ResultHandler<Boolean>> = _savingState

    fun onExerciseNameChange(newName: String) {
        _name.value = newName
    }

    fun onExerciseDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onCategorySelected(newCategoryIndex: Int) {
        _category.value = allCategories[newCategoryIndex]
    }

    fun onImageChange(bitmap: Bitmap) {
        _image.value = Image(ImageId.create(), listOf(exerciseId.value), bitmap)
    }

    open fun createExercise() {
        viewModelScope.launch {
            val exercise = Exercise(
                id = exerciseId,
                name = name.value,
                description = description.value,
                category = category.value,
            )
            saveExercise(exercise)
        }
    }

    private suspend fun saveExercise(exercise: Exercise) {
        try {
            _savingState.value = ResultHandler.Loading
            val imageReference = saveImage()
            val exerciseWithImage = exercise.copy(image = imageReference)
            val domainExercise = ExerciseMapper.toDomainExercise(exerciseWithImage)
            exerciseService.saveExercise(domainExercise)
            _savingState.value = ResultHandler.Success(true)
        } catch (e: Exception) {
            _savingState.value = ResultHandler.Error(message = "Saving exercise failed")
            _savingState.value = ResultHandler.Idle
        }
    }

    private suspend fun saveImage(): ImageReference? {
        val imageByteArray = image.value?.let { ImageMapper.toImageByteArray(it) } ?: return null
        return exerciseService.saveImage(imageByteArray)
    }
}

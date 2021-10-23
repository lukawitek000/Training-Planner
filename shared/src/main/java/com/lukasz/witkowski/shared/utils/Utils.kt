package com.lukasz.witkowski.shared.utils

import com.lukasz.witkowski.shared.models.Category

val allCategories = Category::class.sealedSubclasses.map { it.objectInstance as Category }
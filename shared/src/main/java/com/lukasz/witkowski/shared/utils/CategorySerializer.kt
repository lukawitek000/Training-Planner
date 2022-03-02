package com.lukasz.witkowski.shared.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

//class CategoryAdapter : JsonSerializer<Category>, JsonDeserializer<Category> {
//    override fun serialize(
//        src: Category?,
//        typeOfSrc: Type?,
//        context: JsonSerializationContext?
//    ): JsonElement {
//        val jsn = JsonObject()
//        val category = src ?: Category.None
//        jsn.addProperty("category", category.name)
//        return jsn
//    }
//
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): Category {
//        val jsonObject = json?.asJsonObject ?: return Category.None
//        return allCategories.firstOrNull {
//            it.name == jsonObject.get("category").asString
//        } ?: Category.None
//    }
//}

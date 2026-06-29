package com.martin.storage.data.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MealPlanEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,          // dd/MM/yyyy
    val mealType: String,      // MealType.name
    val recipeId: String,
    val recipeName: String = "",
    val servings: Int = 1
)

@Serializable
data class PreparedMeal(
    val id: String = UUID.randomUUID().toString(),
    val date: String,          // dd/MM/yyyy
    val recipeId: String,
    val recipeName: String,
    val servings: Double = 1.0,
    val nutritionConsumed: NutritionInfo = NutritionInfo(),
    val eaten: Boolean = false  // true = consumed; false = cooked but not yet eaten/may be discarded
)

/** Simple undo/redo history stack. */
class UndoRedoManager<T>(private val maxDepth: Int = 30) {
    private val past  = ArrayDeque<T>()
    private val future = ArrayDeque<T>()

    val canUndo get() = past.isNotEmpty()
    val canRedo get() = future.isNotEmpty()

    /** Call BEFORE applying a mutation; saves the pre-change state. */
    fun push(state: T) {
        past.addLast(state)
        future.clear()
        if (past.size > maxDepth) past.removeFirst()
    }

    /** Returns the state to restore; pass in currentState to enable redo. */
    fun undo(currentState: T): T? {
        if (!canUndo) return null
        future.addFirst(currentState)
        return past.removeLast()
    }

    fun redo(currentState: T): T? {
        if (!canRedo) return null
        past.addLast(currentState)
        return future.removeFirst()
    }

    fun clear() { past.clear(); future.clear() }
}
/*
 * MIT License
 *
 * Copyright (c) 2022 Steven Choo, Netherlands
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This code is inspired by Mitchell Herrijgers
 *
 */

package com.luminis.todo.app.service

import com.luminis.todo.app.entity.Writer

enum class ConditionName {
    LASTNAME_DELETED,
    FIRSTNAME_DELETED
}

typealias ValidateLogicCondition<A> = (A?, A) -> ValidateLogicResult

data class ValidateLogicResult(
    val description: String,
    val invalid: Boolean,
    val children: List<ValidateLogicResult>? = null,
    val matchedInvalidConditions: Set<ConditionName> = emptySet()
)

fun writerLogic(description: String, condition: ValidateLogicCondition<Writer>) =
    fun(old: Writer?, new: Writer): ValidateLogicResult {
        val result = condition.invoke(old, new)
        return ValidateLogicResult(description, result.invalid, listOf(result), result.matchedInvalidConditions)
    }

fun <A> logic(description: String, condition: ValidateLogicCondition<A>, vararg fields: ConditionName) = fun(old: A?, new: A): ValidateLogicResult {
    val result = condition.invoke(old, new)
    return ValidateLogicResult(description, result.invalid, listOf(result), if (result.invalid) result.matchedInvalidConditions + fields else emptySet())
}

fun <A> not(condition: ValidateLogicCondition<A>) = fun(old: A?, new: A): ValidateLogicResult {
    val result = condition.invoke(old, new)
    return ValidateLogicResult("not", !result.invalid, listOf(result), if (!result.invalid) result.matchedInvalidConditions else emptySet())
}

fun <A> and(vararg conditions: ValidateLogicCondition<A>) = fun(old: A?, new: A): ValidateLogicResult {
    val results = conditions.map { it.invoke(old, new) }
    val allValid = results.all { it.invalid }
    val triggeredFields = if (allValid) results.merge() else emptySet()
    return ValidateLogicResult("and", allValid, results, triggeredFields)
}

fun <A> or(vararg conditions: ValidateLogicCondition<A>) = fun(old: A?, new: A): ValidateLogicResult {
    val results = conditions.map { it.invoke(old, new) }
    val triggeredFields = results.filter { it.invalid }.merge()
    return ValidateLogicResult("or", results.any { it.invalid }, results, triggeredFields)
}

fun <A> ifThenElse(condition: ValidateLogicCondition<A>, then: ValidateLogicCondition<A>, otherwise: ValidateLogicCondition<A>) =
    fun(old: A?, new: A): ValidateLogicResult {
        val ifConditionResult = condition.invoke(old, new)
        val actualConditionResult = if (ifConditionResult.invalid) {
            then.invoke(old, new)
        } else {
            otherwise.invoke(old, new)
        }
        return ValidateLogicResult(
            "ifThenElse(result=${ifConditionResult.invalid})",
            actualConditionResult.invalid,
            listOf(actualConditionResult),
            actualConditionResult.matchedInvalidConditions
        )
    }

fun <A, T> changed(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("changed (old: $oldValue, new: $newValue)", oldValue != newValue)
}

fun <A, T> isFalse(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("isFalse (new: $newValue)", newValue == false)
}

fun <A, T> isTrue(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("isTrue (new: $newValue)", newValue == true)
}

fun <A, T> wasFalse(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    return ValidateLogicResult("wasFalse (old: $oldValue)", oldValue == false)
}

fun <A, T> wasTrue(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    return ValidateLogicResult("wasTrue (old: $oldValue)", oldValue == true)
}

fun <A, T> isEmpty(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("isEmpty (new: $newValue)", newValue == null)
}

fun <A, T> wasEmpty(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    return ValidateLogicResult("wasEmpty (old: $oldValue)", oldValue == null)
}

fun <A, T> present(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("isPresent (new: $newValue)", newValue != null)
}

fun <A, T> wasPresent(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    return ValidateLogicResult("wasPresent (new: $oldValue)", oldValue != null)
}

fun <A, T> wasSet(supplier: (A) -> T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("changed (old: $oldValue, new: $newValue)", oldValue == null && newValue != null)
}

fun <A, T> isEqualTo(supplier: (A) -> T, value: T) = fun(old: A?, new: A): ValidateLogicResult {
    val newValue = supplier.invoke(new)
    return ValidateLogicResult("isEqualTo (new: $newValue, desired: $value)", newValue == value)
}

fun <A, T> wasEqualTo(supplier: (A) -> T, value: T) = fun(old: A?, new: A): ValidateLogicResult {
    val oldValue = old?.let { supplier.invoke(it) }
    return ValidateLogicResult("wasEqualTo (old: $oldValue, desired: $value)", oldValue == value)
}

fun List<ValidateLogicResult>.merge() = fold(emptySet<ConditionName>()) { a, b -> a + b.matchedInvalidConditions }

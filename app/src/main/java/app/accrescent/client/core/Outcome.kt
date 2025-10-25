// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.core

sealed class Outcome<out T, out E> {
    data class Ok<T>(val value: T) : Outcome<T, Nothing>()
    data class Err<E>(val error: E) : Outcome<Nothing, E>()

    fun <F> mapErr(op: (E) -> F): Outcome<T, F> = when (this) {
        is Err -> Err(op(this.error))
        is Ok -> this
    }

    fun <U> mapOrElse(default: (E) -> U, f: (T) -> U): U = when (this) {
        is Err -> default(error)
        is Ok -> f(value)
    }
}

private class OutcomeError(val error: Any?) : Throwable()
private class OutcomeSuccess(val value: Any?) : Throwable()

class OutcomeScope<T, E> {
    fun <U> Outcome<U, E>.bind(): U = when (this) {
        is Outcome.Err -> throw OutcomeError(error)
        is Outcome.Ok -> value
    }

    fun <U, F> Outcome<U, F>.bindWith(op: (F) -> E): U = this.mapErr(op).bind()

    fun returnErr(error: E): Nothing = throw OutcomeError(error)
    fun returnOk(value: T): Nothing = throw OutcomeSuccess(value)
}

fun <T, E> outcome(block: OutcomeScope<T, E>.() -> T): Outcome<T, E> = try {
    Outcome.Ok(OutcomeScope<T, E>().block())
} catch (t: OutcomeError) {
    // We only ever set OutcomeError.error to E in Outcome.bind(), so it is safe to interpret it as
    // E when catching it
    @Suppress("UNCHECKED_CAST")
    Outcome.Err(t.error as E)
} catch (t: OutcomeSuccess) {
    // We only ever set OutcomeSuccess.value to T in returnOk(), so it is safe to interpret it as
    // T when catching it.
    @Suppress("UNCHECKED_CAST")
    Outcome.Ok(t.value as T)
}

suspend fun <T, E> outcomeSuspending(block: suspend OutcomeScope<T, E>.() -> T): Outcome<T, E> = try {
    Outcome.Ok(OutcomeScope<T, E>().block())
} catch (t: OutcomeError) {
    // We only ever set OutcomeError.error to E in Outcome.bind(), so it is safe to interpret it as
    // E when catching it
    @Suppress("UNCHECKED_CAST")
    Outcome.Err(t.error as E)
} catch (t: OutcomeSuccess) {
    // We only ever set OutcomeSuccess.value to T in returnOk(), so it is safe to interpret it as
    // T when catching it.
    @Suppress("UNCHECKED_CAST")
    Outcome.Ok(t.value as T)
}

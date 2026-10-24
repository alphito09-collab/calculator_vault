package com.calculator.vault.feature.calculator.domain.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class Operator(val symbol: String, val precedence: Int) {
    ADD("+", 1), SUBTRACT("−", 1), MULTIPLY("×", 2), DIVIDE("÷", 2),
}

enum class CalcInput { D0, D1, D2, D3, D4, D5, D6, D7, D8, D9, DECIMAL, ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT, NEGATE, CLEAR, BACKSPACE, EQUALS }

/**
 * Immutable calculator state. [expression] holds committed numbers and operators; [entry] is
 * the number being typed ("" right after an operator).
 */
data class CalcState(
    val expression: List<Any> = emptyList(),
    val entry: String = "0",
    val history: String = "",
    val justEvaluated: Boolean = false,
    val error: Boolean = false,
) {
    /** Big number on screen. */
    val display: String
        get() = when {
            error -> ERROR
            entry.isNotEmpty() -> CalculatorEngine.formatEntry(entry)
            else -> (expression.lastOrNull { it is BigDecimal } as? BigDecimal)?.let(CalculatorEngine::format) ?: "0"
        }

    /** Small line above the result: "12 + 7 ×" while typing, "12 + 7 =" after equals. */
    val subtitle: String
        get() = if (justEvaluated) history else expression.joinToString(" ") { token ->
            when (token) {
                is BigDecimal -> CalculatorEngine.format(token)
                is Operator -> token.symbol
                else -> ""
            }
        }

    companion object {
        const val ERROR = "Error"
    }
}

/** A plain four-function calculator with operator precedence, % and ±. */
object CalculatorEngine {
    private const val MAX_DIGITS = 15
    private val MATH = MathContext(20, RoundingMode.HALF_UP)

    /** The digits typed for a PIN attempt: exactly what is on screen when "=" is pressed alone. */
    fun pinCandidate(state: CalcState): String? =
        state.entry.takeIf { state.expression.isEmpty() && !state.justEvaluated && !state.error && it.matches(Regex("\\d{4}")) }

    /** True when "=" is pressed with exactly [code] typed on its own (the PIN reset code). */
    fun isCode(state: CalcState, code: String): Boolean =
        state.expression.isEmpty() && !state.justEvaluated && !state.error && state.entry == code

    fun reduce(state: CalcState, input: CalcInput): CalcState = when (input) {
        CalcInput.CLEAR -> CalcState()
        CalcInput.DECIMAL -> decimal(state)
        CalcInput.ADD -> operator(state, Operator.ADD)
        CalcInput.SUBTRACT -> operator(state, Operator.SUBTRACT)
        CalcInput.MULTIPLY -> operator(state, Operator.MULTIPLY)
        CalcInput.DIVIDE -> operator(state, Operator.DIVIDE)
        CalcInput.PERCENT -> transformEntry(state) { it.divide(BigDecimal(100), MATH) }
        CalcInput.NEGATE -> negate(state)
        CalcInput.BACKSPACE -> backspace(state)
        CalcInput.EQUALS -> equals(state)
        else -> digit(state, input.ordinal) // D0..D9 occupy ordinals 0..9
    }

    private fun digit(state: CalcState, digit: Int): CalcState {
        val base = if (state.justEvaluated || state.error) CalcState(entry = "") else state
        val entry = base.entry
        val newEntry = when {
            entry == "0" || entry.isEmpty() -> digit.toString()
            entry == "-0" -> "-$digit"
            entry.count(Char::isDigit) >= MAX_DIGITS -> entry
            else -> entry + digit
        }
        return base.copy(entry = newEntry)
    }

    private fun decimal(state: CalcState): CalcState {
        val base = if (state.justEvaluated || state.error) CalcState(entry = "") else state
        return when {
            base.entry.isEmpty() -> base.copy(entry = "0.")
            '.' in base.entry -> base
            else -> base.copy(entry = base.entry + ".")
        }
    }

    private fun operator(state: CalcState, op: Operator): CalcState {
        if (state.error) return state
        if (state.justEvaluated) {
            val result = parse(state.entry) ?: return CalcState()
            return CalcState(expression = listOf(result, op), entry = "")
        }
        if (state.entry.isEmpty()) {
            if (state.expression.isEmpty()) return state.copy(expression = listOf(BigDecimal.ZERO, op))
            return state.copy(expression = state.expression.dropLast(1) + op)
        }
        val number = parse(state.entry) ?: return state
        return state.copy(expression = state.expression + number + op, entry = "")
    }

    private fun negate(state: CalcState): CalcState {
        if (state.error) return state
        if (state.entry.isEmpty()) return state.copy(entry = "-0")
        val entry = if (state.entry.startsWith("-")) state.entry.drop(1) else "-${state.entry}"
        return state.copy(entry = entry, justEvaluated = false, history = if (state.justEvaluated) "" else state.history)
    }

    private fun transformEntry(state: CalcState, transform: (BigDecimal) -> BigDecimal): CalcState {
        if (state.error || state.entry.isEmpty()) return state
        val value = parse(state.entry) ?: return state
        return state.copy(entry = plain(transform(value)), justEvaluated = false)
    }

    private fun backspace(state: CalcState): CalcState {
        if (state.error) return CalcState()
        if (state.justEvaluated) return state.copy(history = "", justEvaluated = false)
        if (state.entry.isNotEmpty()) {
            val trimmed = state.entry.dropLast(1)
            return state.copy(entry = if (trimmed.isEmpty() || trimmed == "-") (if (state.expression.isEmpty()) "0" else "") else trimmed)
        }
        if (state.expression.isEmpty()) return state.copy(entry = "0")
        val withoutOp = state.expression.dropLast(1)
        val last = withoutOp.lastOrNull() as? BigDecimal
        return state.copy(expression = withoutOp.dropLast(1), entry = last?.let(::plain) ?: "0")
    }

    private fun equals(state: CalcState): CalcState {
        if (state.error) return CalcState()
        if (state.justEvaluated) return state
        val tokens = buildList {
            addAll(state.expression)
            if (state.entry.isNotEmpty()) parse(state.entry)?.let(::add) else if (isNotEmpty()) removeAt(lastIndex)
        }
        if (tokens.isEmpty()) return state
        val history = tokens.joinToString(" ") { if (it is BigDecimal) format(it) else (it as Operator).symbol } + " ="
        val result = evaluate(tokens) ?: return CalcState(error = true, entry = "", history = history, justEvaluated = true)
        return CalcState(entry = plain(result), history = history, justEvaluated = true)
    }

    /** Shunting-yard-free precedence evaluation: collapse × ÷ first, then + −. */
    fun evaluate(tokens: List<Any>): BigDecimal? {
        val numbers = ArrayDeque<BigDecimal>()
        val ops = ArrayDeque<Operator>()
        fun apply(): Boolean {
            val op = ops.removeLast()
            val b = numbers.removeLast()
            val a = numbers.removeLast()
            numbers.addLast(
                when (op) {
                    Operator.ADD -> a.add(b, MATH)
                    Operator.SUBTRACT -> a.subtract(b, MATH)
                    Operator.MULTIPLY -> a.multiply(b, MATH)
                    Operator.DIVIDE -> if (b.signum() == 0) return false else a.divide(b, MATH)
                },
            )
            return true
        }
        for (token in tokens) {
            when (token) {
                is BigDecimal -> numbers.addLast(token)
                is Operator -> {
                    while (ops.isNotEmpty() && ops.last().precedence >= token.precedence) if (!apply()) return null
                    ops.addLast(token)
                }
            }
        }
        while (ops.isNotEmpty()) if (!apply()) return null
        return numbers.singleOrNull()
    }

    private fun parse(entry: String): BigDecimal? = entry.removeSuffix(".").toBigDecimalOrNull()

    private fun plain(value: BigDecimal): String {
        val rounded = value.round(MathContext(MAX_DIGITS, RoundingMode.HALF_UP)).stripTrailingZeros()
        return if (rounded.signum() == 0) "0" else rounded.toPlainString()
    }

    /** "1234567.5" → "1,234,567.5"; huge/tiny numbers use scientific notation. */
    fun format(value: BigDecimal): String {
        val abs = value.abs()
        if (abs.signum() != 0 && (abs >= BigDecimal("1e15") || abs < BigDecimal("1e-9"))) {
            return String.format(java.util.Locale.US, "%.6e", value).replace("e+0", "e").replace("e+", "e")
        }
        return formatEntry(plain(value))
    }

    fun formatEntry(entry: String): String {
        if (entry == CalcState.ERROR) return entry
        val negative = entry.startsWith("-")
        val body = entry.removePrefix("-")
        val intPart = body.substringBefore('.')
        val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
        val decimal = if ('.' in body) "." + body.substringAfter('.') else ""
        return (if (negative) "-" else "") + grouped + decimal
    }
}

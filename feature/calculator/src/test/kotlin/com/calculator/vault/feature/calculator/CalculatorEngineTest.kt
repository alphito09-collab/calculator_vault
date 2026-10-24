package com.calculator.vault.feature.calculator

import com.calculator.vault.feature.calculator.domain.engine.CalcInput
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.ADD
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.BACKSPACE
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.DECIMAL
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.DIVIDE
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.EQUALS
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.MULTIPLY
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.NEGATE
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.PERCENT
import com.calculator.vault.feature.calculator.domain.engine.CalcInput.SUBTRACT
import com.calculator.vault.feature.calculator.domain.engine.CalcState
import com.calculator.vault.feature.calculator.domain.engine.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private fun type(vararg keys: Any): CalcState = keys.fold(CalcState()) { state, key ->
        when (key) {
            is Int -> key.toString().fold(state) { s, c -> CalculatorEngine.reduce(s, CalcInput.entries[c.digitToInt()]) }
            is CalcInput -> CalculatorEngine.reduce(state, key)
            else -> error("bad key $key")
        }
    }

    @Test fun precedence() = assertEquals("14", type(2, ADD, 3, MULTIPLY, 4, EQUALS).display)

    @Test fun division() = assertEquals("2.5", type(10, DIVIDE, 4, EQUALS).display)

    @Test fun divideByZeroIsError() = assertEquals(CalcState.ERROR, type(5, DIVIDE, 0, EQUALS).display)

    @Test fun percent() = assertEquals("0.5", type(50, PERCENT).display)

    @Test fun negate() = assertEquals("-12", type(12, NEGATE).display)

    @Test fun decimals() = assertEquals("0.3", type(0, DECIMAL, 1, ADD, 0, DECIMAL, 2, EQUALS).display)

    @Test fun grouping() = assertEquals("1,234,567", type(1234567).display)

    @Test fun replaceOperator() = assertEquals("2", type(4, ADD, SUBTRACT, 2, EQUALS).display)

    @Test fun backspaceRestoresPreviousNumber() = assertEquals("45", type(45, ADD, BACKSPACE).display)

    @Test fun chainAfterEquals() = assertEquals("12", type(3, ADD, 3, EQUALS, MULTIPLY, 2, EQUALS).display)

    @Test fun subtitleShowsExpression() = assertEquals("12 + 7 =", type(12, ADD, 7, EQUALS).subtitle)

    @Test fun resetCodeOnlyWhenTypedAlone() {
        assertTrue(CalculatorEngine.isCode(type(7860786), "7860786"))
        assertFalse(CalculatorEngine.isCode(type(78607861), "7860786"))
        assertFalse(CalculatorEngine.isCode(type(1, ADD, 7860786), "7860786"))
        assertFalse(CalculatorEngine.isCode(type(7860786, EQUALS), "7860786"))
    }

    @Test fun pinCandidateOnlyForBareFourDigits() {
        assertEquals("1234", CalculatorEngine.pinCandidate(type(1234)))
        assertNull(CalculatorEngine.pinCandidate(type(12345)))
        assertNull(CalculatorEngine.pinCandidate(type(12, ADD, 34)))
        assertNull(CalculatorEngine.pinCandidate(type(1234, EQUALS)))
    }
}

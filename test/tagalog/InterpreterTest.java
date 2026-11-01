package tagalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InterpreterTest {
    private String run(String source) {
        return TestSupport.run(source);
    }

    private String errorFor(String source) {
        return assertThrows(RuntimeError.class, () -> TestSupport.run(source)).getMessage();
    }

    // Math and text

    @Test
    void followsMathPrecedence() {
        assertEquals("7\n", run("sabihin 1 + 2 * 3"));
        assertEquals("9\n", run("sabihin (1 + 2) * 3"));
    }

    @Test
    void dividesWithDecimals() {
        assertEquals("3.5\n", run("sabihin 7 / 2"));
    }

    @Test
    void negativeNumbers() {
        assertEquals("0\n", run("sabihin -5 + 10 / 2"));
        assertEquals("-3\n", run("sabihin 2 - 5"));
    }

    @Test
    void joinsTextAndNumbers() {
        assertEquals("Bilang: 5\n", run("sabihin \"Bilang: \" + 5"));
    }

    @Test
    void printsBooleansInTagalog() {
        assertEquals("totoo\n", run("sabihin 3 < 5"));
        assertEquals("mali\n", run("sabihin 3 > 5"));
    }

    @Test
    void equalityWorksForNumbersTextAndBooleans() {
        assertEquals("totoo\n", run("sabihin 2 == 2"));
        assertEquals("totoo\n", run("sabihin \"a\" == \"a\""));
        assertEquals("totoo\n", run("sabihin 1 != 2"));
        assertEquals("mali\n", run("sabihin 1 == \"1\""));
    }

    // Logic

    @Test
    void logicOperators() {
        assertEquals("totoo\n", run("sabihin 3 < 5 at 5 >= 5"));
        assertEquals("mali\n", run("sabihin hindi totoo o mali"));
    }

    @Test
    void logicStopsEarly() {
        assertEquals("totoo\n", run("sabihin totoo o (1 / 0)"));
        assertEquals("mali\n", run("sabihin mali at (1 / 0)"));
    }

    // Variables and control flow

    @Test
    void storesAndUpdatesVariables() {
        String source = """
                itakda x = 5
                itakda x = x + 1
                sabihin x
                """;
        assertEquals("6\n", run(source));
    }

    @Test
    void whileLoopCounts() {
        String source = """
                itakda i = 1
                habang i <= 3
                    sabihin i
                    itakda i = i + 1
                tapos
                """;
        assertEquals("1\n2\n3\n", run(source));
    }

    @Test
    void whileLoopCanSkipEntirely() {
        assertEquals("", run("habang mali\n    sabihin 1\ntapos\n"));
    }

    @Test
    void ifTakesTheTrueBranch() {
        String source = """
                kung 2 > 1
                    sabihin "oo"
                kundi
                    sabihin "hindi"
                tapos
                """;
        assertEquals("oo\n", run(source));
    }

    @Test
    void ifTakesTheElseBranch() {
        String source = """
                kung 1 > 2
                    sabihin "oo"
                kundi
                    sabihin "hindi"
                tapos
                """;
        assertEquals("hindi\n", run(source));
    }

    @Test
    void nestedBlocks() {
        String source = """
                itakda i = 1
                habang i <= 3
                    kung i == 2
                        sabihin "dalawa"
                    kundi
                        sabihin i
                    tapos
                    itakda i = i + 1
                tapos
                """;
        assertEquals("1\ndalawa\n3\n", run(source));
    }

    // Functions

    @Test
    void callsFunctionWithReturnValue() {
        String source = """
                gawin kuwadrado(x)
                    ibalik x * x
                tapos
                sabihin kuwadrado(7)
                """;
        assertEquals("49\n", run(source));
    }

    @Test
    void callsFunctionWithTwoParameters() {
        String source = """
                gawin dagdag(a, b)
                    ibalik a + b
                tapos
                sabihin dagdag(2, 3)
                """;
        assertEquals("5\n", run(source));
    }

    @Test
    void callsFunctionAsStatement() {
        String source = """
                gawin bati(pangalan)
                    sabihin "Kumusta, " + pangalan + "!"
                tapos
                bati("Juan")
                """;
        assertEquals("Kumusta, Juan!\n", run(source));
    }

    @Test
    void recursionWorks() {
        String source = """
                gawin factorial(n)
                    kung n <= 1
                        ibalik 1
                    tapos
                    ibalik n * factorial(n - 1)
                tapos
                sabihin factorial(5)
                """;
        assertEquals("120\n", run(source));
    }

    @Test
    void returnStopsTheFunction() {
        String source = """
                gawin f()
                    ibalik 1
                    sabihin "hindi lalabas"
                tapos
                sabihin f()
                """;
        assertEquals("1\n", run(source));
    }

    @Test
    void functionWithoutReturnGivesWala() {
        String source = """
                gawin f()
                    sabihin 1
                tapos
                sabihin f()
                """;
        assertEquals("1\nwala\n", run(source));
    }

    @Test
    void localVariablesStayInsideTheFunction() {
        String source = """
                itakda x = 10
                gawin baguhin()
                    itakda x = 99
                    sabihin x
                tapos
                baguhin()
                sabihin x
                """;
        assertEquals("99\n10\n", run(source));
    }

    @Test
    void functionCanReadGlobalVariables() {
        String source = """
                itakda dagdag = 5
                gawin f(x)
                    ibalik x + dagdag
                tapos
                sabihin f(1)
                """;
        assertEquals("6\n", run(source));
    }

    // Runtime errors

    @Test
    void reportsUndefinedVariable() {
        String message = errorFor("sabihin bilang");
        assertTrue(message.contains("Linya 1"));
        assertTrue(message.contains("walang variable na 'bilang'"));
    }

    @Test
    void reportsDivisionByZero() {
        assertTrue(errorFor("sabihin 5 / 0").contains("zero"));
    }

    @Test
    void reportsNonBooleanCondition() {
        assertTrue(errorFor("kung 1 + 2\n    sabihin 1\ntapos\n").contains("totoo o mali"));
    }

    @Test
    void reportsWrongTypesInMath() {
        assertTrue(errorFor("sabihin totoo + 1").contains("'+'"));
        assertTrue(errorFor("sabihin \"a\" * 2").contains("'*'"));
        assertTrue(errorFor("sabihin hindi 5").contains("'hindi'"));
    }

    @Test
    void reportsWrongArgumentCount() {
        String source = """
                gawin dagdag(a, b)
                    ibalik a + b
                tapos
                sabihin dagdag(1)
                """;
        String message = errorFor(source);
        assertTrue(message.contains("Linya 4"));
        assertTrue(message.contains("2 argumento pero 1"));
    }

    @Test
    void reportsCallingANonFunction() {
        assertTrue(errorFor("itakda x = 5\nsabihin x(1)").contains("hindi function"));
    }

    @Test
    void reportsInfiniteRecursion() {
        String source = """
                gawin walang_hanggan(n)
                    ibalik walang_hanggan(n + 1)
                tapos
                sabihin walang_hanggan(1)
                """;
        assertTrue(errorFor(source).contains("masyadong malalim"));
    }
}

package tagalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParserTest {
    private String errorFor(String source) {
        return assertThrows(ParseException.class, () -> TestSupport.tree(source)).getMessage();
    }

    @Test
    void multiplicationBindsTighterThanAddition() {
        assertEquals("Sabihin (+ 1 (* 2 3))\n", TestSupport.tree("sabihin 1 + 2 * 3"));
    }

    @Test
    void parenthesesChangeTheOrder() {
        assertEquals("Sabihin (* (grupo (+ 1 2)) 3)\n", TestSupport.tree("sabihin (1 + 2) * 3"));
    }

    @Test
    void subtractionGroupsFromTheLeft() {
        assertEquals("Sabihin (- (- 10 3) 2)\n", TestSupport.tree("sabihin 10 - 3 - 2"));
    }

    @Test
    void unaryMinusAndNot() {
        assertEquals("Sabihin (- 5)\n", TestSupport.tree("sabihin -5"));
        assertEquals("Sabihin (hindi totoo)\n", TestSupport.tree("sabihin hindi totoo"));
    }

    @Test
    void atBindsTighterThanO() {
        assertEquals("Sabihin (o totoo (at mali mali))\n", TestSupport.tree("sabihin totoo o mali at mali"));
    }

    @Test
    void comparisonBindsTighterThanEquality() {
        assertEquals("Sabihin (== (< 1 2) totoo)\n", TestSupport.tree("sabihin 1 < 2 == totoo"));
    }

    @Test
    void assignmentStatement() {
        assertEquals("Itakda x = 5\n", TestSupport.tree("itakda x = 5"));
    }

    @Test
    void whileLoopWithBlock() {
        String source = """
                habang i < 3
                    sabihin i
                tapos
                """;
        assertEquals("Habang (< i 3)\n  Sabihin i\nTapos\n", TestSupport.tree(source));
    }

    @Test
    void ifWithElse() {
        String source = """
                kung x == 1
                    sabihin "isa"
                kundi
                    sabihin "iba"
                tapos
                """;
        assertEquals("Kung (== x 1)\n  Sabihin \"isa\"\nKundi\n  Sabihin \"iba\"\nTapos\n",
                TestSupport.tree(source));
    }

    @Test
    void functionDeclaration() {
        String source = """
                gawin dagdag(a, b)
                    ibalik a + b
                tapos
                """;
        assertEquals("Gawin dagdag(a, b)\n  Ibalik (+ a b)\nTapos\n", TestSupport.tree(source));
    }

    @Test
    void functionCallInsideExpression() {
        assertEquals("Sabihin (+ (tawag dagdag 1 2) 3)\n", TestSupport.tree("sabihin dagdag(1, 2) + 3"));
    }

    @Test
    void callWithoutArguments() {
        assertEquals("(tawag bati)\n", TestSupport.tree("bati()"));
    }

    @Test
    void blankLinesAndCommentsAreIgnored() {
        String source = """
                # simula

                sabihin 1

                # wakas
                """;
        assertEquals("Sabihin 1\n", TestSupport.tree(source));
    }

    @Test
    void reportsMissingVariableName() {
        String message = errorFor("itakda = 5");
        assertTrue(message.contains("Linya 1"));
        assertTrue(message.contains("pangalan ng variable"));
    }

    @Test
    void reportsMissingTapos() {
        String message = errorFor("habang totoo\n    sabihin 1\n");
        assertTrue(message.contains("tapos"));
    }

    @Test
    void reportsMissingClosingParenthesis() {
        assertTrue(errorFor("sabihin (1 + 2").contains("')'"));
    }

    @Test
    void reportsReturnOutsideFunction() {
        assertTrue(errorFor("ibalik 5").contains("loob ng function"));
    }

    @Test
    void reportsMissingItakda() {
        assertTrue(errorFor("x = 5").contains("itakda"));
    }

    @Test
    void rejectsPlainExpressionStatement() {
        assertThrows(ParseException.class, () -> TestSupport.tree("5 + 5"));
    }

    @Test
    void rejectsStrayTapos() {
        assertThrows(ParseException.class, () -> TestSupport.tree("tapos"));
    }
}

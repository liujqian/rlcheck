package edu.berkeley.cs.jqf.fuzz.rl.js;

import edu.berkeley.cs.jqf.fuzz.rl.RLGenerator;
import edu.berkeley.cs.jqf.fuzz.rl.RLGuide;
import edu.berkeley.cs.jqf.fuzz.rl.RLParams;
import edu.berkeley.cs.jqf.fuzz.rl.experiments.TrieBasedMonteCarloLearner;

import java.util.*;
import java.util.function.Supplier;


public class JavaScriptRLGeneratorWithTrieMCGuide implements RLGenerator {

    private TrieBasedMonteCarloLearner learner;
    private static final int MAX_IDENTIFIERS = 50;
    private static final int MAX_EXPRESSION_DEPTH = 7;
    private static final int MAX_STATEMENT_DEPTH = 4;
    private Set<String> identifiers;
    private int statementDepth;
    private int expressionDepth;
    private int stateSize;


    /**
     * Terminal action output
     */
    public static final String terminal = "END"; // Unused


    private int MIN_INT = 0;
    private int MAX_INT = 5;
    private int MAX_STR_LEN = 3;

    private static final Boolean[] BOOLEANS = {true, false};

    private static final String[] STATEMENTS_1 = {
            "expression", "break", "continue", "return", "throw", "var", "empty"
    };

    private static final String[] STATEMENTS_2 = {
            "if", "for", "while", "namedfunc", "switch", "try", "block"
    };

    private static final String[] EXPRESSIONS_1 = {
            "literal", "ident"
    };
    private static final String[] EXPRESSIONS_2 = {
            "unary", "binary", "ternary", "call", "function",
            "property", "index", "arrow"
    };
    private static final String[] UNARY_TOKENS = {
            "!", "++", "--", "~",
            "delete", "new", "typeof"
    };

    private static final String[] BINARY_TOKENS = {
            "!=", "!==", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "+=", ",",
            "-", "-=", "/", "/=", "<", "<<", ">>=", "<=", "=", "==", "===",
            ">", ">=", ">>", ">>=", ">>>", ">>>=", "^", "^=", "|", "|=", "||",
            "in", "instanceof"
    };

    private static final String[] LITERAL_TYPES = {
            "int", "boolean", "string", "undefined", "null", "this"
    };
    private List<Object> ints;
    private List<Object> bools;
    private List<Object> ascii;

    public JavaScriptRLGeneratorWithTrieMCGuide() {
    }

    /**
     * Parameter initialization function
     *
     * @param params: int stateSize,
     *                int seed (optional)
     *                double defaultEpsilon // TODO: per learner epsilon values
     */
    public void init(RLParams params) {
        double e = (double) params.get("defaultEpsilon", true);
        boolean useBonus = false;
        if (params.exists("explorationBonus")) {
            useBonus = true;
        }
        if (params.exists("seed")) {
            learner = new TrieBasedMonteCarloLearner(e, new Random((long) params.get("seed")),useBonus);
        } else {
            learner = new TrieBasedMonteCarloLearner(e,useBonus);
        }
        ints = Arrays.asList(RLGuide.range(MIN_INT, MAX_INT + 1));
        bools = Arrays.asList(BOOLEANS);
        ascii = new ArrayList<>(26);
        for (char c = 'A'; c <= 'Z'; c++)
            ascii.add(String.valueOf(c));
        System.out.println("The \"init\" method of JavaScriptRLGeneratorWithTrieMCGuide is called! useBonus is " + useBonus + "!");
    }

    /**
     * Generate the next input
     *
     * @return The next input as an InputStream
     */
    public String generate() {
        this.identifiers = new HashSet<>();
        this.statementDepth = 0;
        this.expressionDepth = 0;
        String[] state = new String[stateSize];
        return generateStatement();
    }

    /**
     * Update the state of the generator given a reward
     *
     * @param r reward
     */
    public void update(int r) {
        learner.update(r);
    }


    private String generateExpression() {
        expressionDepth++;
        String result;
        if (expressionDepth >= MAX_EXPRESSION_DEPTH || (Boolean) learner.select(Arrays.asList(true, false))) {
            String fn = (String) learner.select(
                    Arrays.asList(EXPRESSIONS_1)
            );
            switch (fn) {
                case "literal":
                    result = generateLiteralNode();
                    break;
                case "ident":
                    result = generateIdentNode();
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }

        } else {
            String fn = (String) learner.select(
                    Arrays.asList(EXPRESSIONS_2)
            );
            switch (fn) {
                case "unary":
                    result = generateUnaryNode();
                    break;
                case "binary":
                    result = generateBinaryNode();
                    break;
                case "ternary":
                    result = generateTernaryNode();
                    break;
                case "call":
                    result = generateCallNode();
                    break;
                case "function":
                    result = generateFunctionNode();
                    break;
                case "property":
                    result = generatePropertylNode();
                    break;
                case "index":
                    result = generateIndexNode();
                    break;
                case "arrow":
                    result = generateArrowFunctionNode();
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }
        }
        expressionDepth--;
        return "(" + result + ")";
    }

    private String generateStatement() {
        statementDepth++;
        String result;
        if (statementDepth >= MAX_STATEMENT_DEPTH || (Boolean) learner.select(Arrays.asList(true, false))) {
            String fn = (String) learner.select(
                    Arrays.asList(STATEMENTS_1)
            );
            switch (fn) {
                case "expression":
                    result = generateExpression();
                    break;
                case "break":
                    result = generateBreakNode();
                    break;
                case "continue":
                    result = generateContinueNode();
                    break;
                case "return":
                    result = generateReturnNode();
                    break;
                case "throw":
                    result = generateThrowNode();
                    break;
                case "var":
                    result = generateVarNode();
                    break;
                case "empty":
                    result = generateEmptyNode();
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");

            }
        } else {
            String fn = (String) learner.select(
                    Arrays.asList(STATEMENTS_2)
            );
            switch (fn) {
                case "if":
                    result = generateIfNode();
                    break;
                case "for":
                    result = generateForNode();
                    break;
                case "while":
                    result = generateWhileNode();
                    break;
                case "namedfunc":
                    result = generateNamedFunctionNode();
                    break;
                case "switch":
                    result = generateSwitchNode();
                    break;
                case "try":
                    result = generateTryNode();
                    break;
                case "block":
                    result = generateBlock();
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }
        }
        statementDepth--;
        return result;
    }


    private String generateLiteralNode() {
        if (expressionDepth < MAX_EXPRESSION_DEPTH && (Boolean) learner.select(Arrays.asList(true, false))) {
            //TODO multiple expressions in brackets
            int numArgs = (int) learner.select(ints);
            if ((Boolean) learner.select(Arrays.asList(true, false))) {
                return "[" + generateItems(this::generateExpression, numArgs) + "]";
            } else {
                return "{" + generateItems(this::generateObjectProperty, numArgs) + "}";
            }
        } else {
            String type = (String) learner.select(
                    Arrays.asList(LITERAL_TYPES)
            );
            switch (type) {
                case "int":
                    return String.valueOf(learner.select(ints));
                case "boolean":
                    return String.valueOf(learner.select(Arrays.asList(true, false)));
                case "string":
                    Supplier<String> genChr = () -> (String) learner.select(ascii);
                    return String.join("", generateItems(genChr, MAX_STR_LEN));
                default:
                    return type;
            }
        }
    }

    private String generateIdentNode() {
        String identifier;
        if (identifiers.isEmpty() || (identifiers.size() < MAX_IDENTIFIERS && (Boolean) learner.select(Arrays.asList(true, false)))) {
            identifier = learner.select(ascii) + "_" + identifiers.size();
            identifiers.add(identifier);
        } else {
            List<Object> identList = new ArrayList<>(identifiers);
            identifier = (String) learner.select(identList);
        }
        return identifier;
    }

    private String generateUnaryNode() {
        String token = (String) learner.select(
                Arrays.asList(UNARY_TOKENS)
        );
        return token + " " + generateExpression();
    }

    private String generateBinaryNode() {
        String token = (String) learner.select(
                Arrays.asList(BINARY_TOKENS)
        );
        String lhs = generateExpression();
        String rhs = generateExpression();
        return lhs + " " + token + " " + rhs;
    }

    private String generateTernaryNode() {
        return generateExpression() + " ? " + generateExpression() +
                " : " + generateExpression();
    }

    private String generateCallNode() {
        String func = generateExpression();

        int numArgs = (int) learner.select(ints);
        String args = String.join(",", generateItems(this::generateExpression, numArgs));

        String call = func + "(" + args + ")";
        if ((Boolean) learner.select(Arrays.asList(true, false))) {
            return call;
        } else {
            return "new" + call;
        }
    }

    private String generateFunctionNode() {
        int numArgs = (int) learner.select(ints);
        return "function(" + String.join(", ", generateItems(this::generateIdentNode, numArgs)) + ")"
                + generateBlock();
    }

    private String generatePropertylNode() {
        return generateExpression() + "." + generateIdentNode();
    }

    private String generateIndexNode() {
        return generateExpression() + "[" + generateExpression() + "]";

    }

    private String generateArrowFunctionNode() {
        int numArgs = (int) learner.select(ints);
        String params = "(" + String.join(", ", generateItems(this::generateIdentNode, numArgs)) + ")";
        if ((Boolean) learner.select(Arrays.asList(true, false))) {
            return params + " => " + generateBlock();
        } else {
            return params + " => " + generateExpression();
        }
    }

    private String generateBlock() {
        int numArgs = (int) learner.select(ints);
        return "{ " + String.join(";", generateItems(this::generateStatement, numArgs)) + " }";

    }

    private String generateBreakNode() {
        return "break";
    }

    private String generateContinueNode() {
        return "continue";
    }

    private String generateReturnNode() {
        return (Boolean) learner.select(Arrays.asList(true, false)) ? "return" : "return " + generateExpression();
    }

    private String generateThrowNode() {
        return "throw " + generateExpression();
    }

    private String generateVarNode() {
        return "var " + generateIdentNode();
    }

    private String generateEmptyNode() {
        return "";
    }

    private String generateIfNode() {
        return "if (" +
                generateExpression() + ") " +
                generateBlock() +
                ((Boolean) learner.select(Arrays.asList(true, false)) ? generateBlock() : "");
    }

    private String generateForNode() {
        String s = "for(";
        if ((Boolean) learner.select(Arrays.asList(true, false))) {
            s += generateExpression();
        }
        s += ";";
        if ((Boolean) learner.select(Arrays.asList(true, false))) {
            s += generateExpression();
        }
        s += ";";
        if ((Boolean) learner.select(Arrays.asList(true, false))) {
            s += generateExpression();
        }
        s += ")";
        s += generateBlock();
        return s;
    }

    private String generateWhileNode() {
        return "while (" + generateExpression() + ")" + generateBlock();

    }

    private String generateNamedFunctionNode() {
        int numArgs = (int) learner.select(ints);
        return "function " + generateIdentNode() + "(" + String.join(", ", generateItems(this::generateIdentNode, numArgs)) + ")" + generateBlock();
    }

    private String generateSwitchNode() {
        int numArgs = (int) learner.select(ints);
        return "switch(" + generateExpression() + ") {"
                + String.join(" ", generateItems(this::generateCaseNode, numArgs) + "}");
    }

    private String generateTryNode() {
        return "try " + generateBlock() + generateCatchNode();

    }

    private String generateCatchNode() {
        return "catch (" + generateIdentNode() + ") " +
                generateBlock();
    }


    private String generateObjectProperty() {
        return generateIdentNode() + ": " + generateExpression();
    }


    private String generateCaseNode() {
        return "case " + generateExpression() + ": " + generateBlock();
    }


    private <T> List<T> generateItems(Supplier<T> generator, int len) {
        List<T> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(generator.get());
        }
        return items;
    }

    /* Update and return new state. Removes items if too long */
    private String[] updateState(String[] stateArr, String stateX) {
        String[] newState = new String[stateSize];
        int end = stateSize - 1;
        newState[end] = stateX;
        for (int i = 0; i < end; i++) {
            newState[i] = stateArr[i + 1];
        }
        return newState;
    }


}

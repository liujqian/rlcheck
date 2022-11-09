package edu.berkeley.cs.jqf.examples.js;

import edu.berkeley.cs.jqf.fuzz.rl.RLGenerator;
import edu.berkeley.cs.jqf.fuzz.rl.RLGuide;
import edu.berkeley.cs.jqf.fuzz.rl.RLParams;

import java.util.*;
import java.util.function.Function;


public class SequentialJavaScriptRLGeneratorWithTableMCGuide implements RLGenerator {

    private RLGuide guide;

    private static final int MAX_IDENTIFIERS = 50;
    private static final int MAX_EXPRESSION_DEPTH = 7;
    private static final int MAX_STATEMENT_DEPTH = 4;
    private Set<String> identifiers;
    private int statementDepth;
    private int expressionDepth;
    private int stateSize;
    private int intId;
    private int boolId;
    private int chrId;
    private int selectId;

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

    public SequentialJavaScriptRLGeneratorWithTableMCGuide() {
    }

    /**
     * Parameter initialization function
     *
     * @param params: int stateSize,
     *                int seed (optional)
     *                double defaultEpsilon // TODO: per learner epsilon values
     */
    public void init(RLParams params) {
        if (params.exists("seed")) {
            guide = new RLGuide((long) params.get("seed"));
        } else {
            guide = new RLGuide();
        }
        double e = (double) params.get("defaultEpsilon", true);
        List<Object> ints = Arrays.asList(RLGuide.range(MIN_INT, MAX_INT + 1));
        List<Object> bools = Arrays.asList(BOOLEANS);
        List<Object> ascii = new ArrayList<>(26);
        for (char c = 'A'; c <= 'Z'; c++)
            ascii.add(String.valueOf(c));

        this.stateSize = (int) params.get("stateSize", true);
        this.selectId = guide.addLearner(null, e);
        this.intId = guide.addLearner(ints, e);
        this.boolId = guide.addLearner(bools, e);
        this.chrId = guide.addLearner(ascii, e);

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
        return generateStatement(state);
    }

    /**
     * Update the state of the generator given a reward
     *
     * @param r reward
     */
    public void update(int r) {
        guide.update(r);
    }


    private String generateExpression(String[] stateArr) {
        updateState(stateArr, "node=expression");
        expressionDepth++;
        String result;

        boolean greaterThanMaxExprDepth = expressionDepth >= MAX_EXPRESSION_DEPTH;
        boolean selected = false;
        if (!greaterThanMaxExprDepth) {
            selected = (Boolean) guide.select(stateArr, boolId);
            updateState(stateArr, "haveExpression_1=" + selected);
        }

        if (greaterThanMaxExprDepth || selected) {
            String fn = (String) guide.select(
                    Arrays.asList(EXPRESSIONS_1),
                    stateArr,
                    selectId
            );
            updateState(stateArr, "selectedExpression_1=" + fn);
            switch (fn) {
                case "literal":
                    result = generateLiteralNode(stateArr);
                    break;
                case "ident":
                    result = generateIdentNode(stateArr);
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }

        } else {
            String fn = (String) guide.select(
                    Arrays.asList(EXPRESSIONS_2),
                    stateArr,
                    selectId
            );
            updateState(stateArr, "selectedExpression_2=" + fn);
            switch (fn) {
                case "unary":
                    result = generateUnaryNode(stateArr);
                    break;
                case "binary":
                    result = generateBinaryNode(stateArr);
                    break;
                case "ternary":
                    result = generateTernaryNode(stateArr);
                    break;
                case "call":
                    result = generateCallNode(stateArr);
                    break;
                case "function":
                    result = generateFunctionNode(stateArr);
                    break;
                case "property":
                    result = generatePropertylNode(stateArr);
                    break;
                case "index":
                    result = generateIndexNode(stateArr);
                    break;
                case "arrow":
                    result = generateArrowFunctionNode(stateArr);
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }
        }
        expressionDepth--;
        return "(" + result + ")";
    }

    private String generateStatement(String[] stateArr) {
        updateState(stateArr, "node=statement");
        statementDepth++;
        String result;

        boolean greaterThanMaxStatementDepth = statementDepth >= MAX_STATEMENT_DEPTH;
        boolean selected = false;
        if (!greaterThanMaxStatementDepth) {
            selected = (Boolean) guide.select(stateArr, boolId);
            updateState(stateArr, "haveStatement_1=" + selected);
        }

        if (greaterThanMaxStatementDepth || selected) {
            String fn = (String) guide.select(
                    Arrays.asList(STATEMENTS_1),
                    stateArr,
                    selectId
            );
            updateState(stateArr, "selectedStatement_1" + fn);
            switch (fn) {
                case "expression":
                    result = generateExpression(stateArr);
                    break;
                case "break":
                    result = generateBreakNode(stateArr);
                    break;
                case "continue":
                    result = generateContinueNode(stateArr);
                    break;
                case "return":
                    result = generateReturnNode(stateArr);
                    break;
                case "throw":
                    result = generateThrowNode(stateArr);
                    break;
                case "var":
                    result = generateVarNode(stateArr);
                    break;
                case "empty":
                    result = generateEmptyNode(stateArr);
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");

            }
        } else {
            String fn = (String) guide.select(
                    Arrays.asList(STATEMENTS_2),
                    stateArr,
                    selectId
            );
            updateState(stateArr, "selectedStatement_2" + fn);
            switch (fn) {
                case "if":
                    result = generateIfNode(stateArr);
                    break;
                case "for":
                    result = generateForNode(stateArr);
                    break;
                case "while":
                    result = generateWhileNode(stateArr);
                    break;
                case "namedfunc":
                    result = generateNamedFunctionNode(stateArr);
                    break;
                case "switch":
                    result = generateSwitchNode(stateArr);
                    break;
                case "try":
                    result = generateTryNode(stateArr);
                    break;
                case "block":
                    result = generateBlock(stateArr);
                    break;
                default:
                    throw new Error(fn + " NOT FOUND");
            }
        }
        statementDepth--;
        return result;
    }


    private String generateLiteralNode(String[] stateArr) {
        updateState(stateArr, "node=literal");

        boolean lessThanMaxExprDepth = expressionDepth < MAX_EXPRESSION_DEPTH;
        boolean selected = false;
        if (lessThanMaxExprDepth) {
            selected = (Boolean) guide.select(stateArr, boolId);
            updateState(stateArr, "generateLiteralNode_1=" + selected);
        }

        if (lessThanMaxExprDepth && selected) {
            updateState(stateArr, "branch=1");
            //TODO multiple expressions in brackets
            int numArgs = (int) guide.select(stateArr, intId);
            updateState(stateArr, "numArgs=" + numArgs);

            boolean generateBrackets = (Boolean) guide.select(stateArr, boolId);
            updateState(stateArr, "generateBrackets=" + generateBrackets);

            if (generateBrackets) {
                return "[" + generateItems(this::generateExpression, stateArr, numArgs) + "]";
            } else {
                return "{" + generateItems(this::generateObjectProperty, stateArr, numArgs) + "}";
            }
        } else {
            String type = (String) guide.select(
                    Arrays.asList(LITERAL_TYPES),
                    stateArr,
                    selectId
            );
            updateState(stateArr, "literalType=" + type);
            switch (type) {
                case "int":
                    int literalInt = (int) guide.select(stateArr, intId);
                    updateState(stateArr, "literalInt=" + literalInt);
                    return String.valueOf(literalInt);
                case "boolean":
                    boolean literalBool = (boolean) guide.select(stateArr, boolId);
                    updateState(stateArr, "literalBool=" + literalBool);
                    return String.valueOf(literalBool);
                case "string":
                    Function<String[], String> genChr = s -> {
                        String literalChar = (String) guide.select(s, chrId);
                        updateState(stateArr, "literalChar=" + literalChar);
                        return literalChar;
                    };
                    return String.join("", generateItems(genChr, stateArr, MAX_STR_LEN));
                default:
                    return type;
            }
        }
    }

    private String generateIdentNode(String[] stateArr) {
        String identifier;
        updateState(stateArr, "node=ident");
        boolean emptyIdentifiers = identifiers.isEmpty();
        boolean lessThanMaxIdentifiers = identifiers.size() < MAX_IDENTIFIERS;
        boolean addIdentifier = false;
        if (!emptyIdentifiers && lessThanMaxIdentifiers) {
            addIdentifier = (Boolean) guide.select(stateArr, boolId);
            updateState(stateArr, "addIdentifier=" + addIdentifier);
        }
        if (emptyIdentifiers || (lessThanMaxIdentifiers && addIdentifier)) {
            String identifier_pre = (String) guide.select(stateArr, chrId);
            updateState(stateArr, "identifierPre=" + identifier_pre);
            identifier = identifier_pre + "_" + identifiers.size();
            identifiers.add(identifier);
        } else {
            List<Object> identList = new ArrayList<>(identifiers);
            String selectedId = (String) guide.select(identList, stateArr, selectId);
            updateState(stateArr, "selectedIdentifier=" + selectedId);
            identifier = selectedId;
        }
        return identifier;
    }

    private String generateUnaryNode(String[] stateArr) {
        updateState(stateArr, "node=unary");
        String token = (String) guide.select(
                Arrays.asList(UNARY_TOKENS),
                stateArr,
                selectId
        );
        updateState(stateArr, "unary=" + token);
        return token + " " + generateExpression(stateArr);
    }

    private String generateBinaryNode(String[] stateArr) {
        updateState(stateArr, "node=binary");
        String token = (String) guide.select(
                Arrays.asList(BINARY_TOKENS),
                stateArr,
                selectId
        );
        updateState(stateArr, "binary=" + token);
        String lhs = generateExpression(stateArr);
        String rhs = generateExpression(stateArr);
        return lhs + " " + token + " " + rhs;
    }

    private String generateTernaryNode(String[] stateArr) {
        updateState(stateArr, "node=ternary");
        return generateExpression(stateArr) + " ? " + generateExpression(stateArr) +
                " : " + generateExpression(stateArr);
    }

    private String generateCallNode(String[] stateArr) {
        updateState(stateArr, "node=call");
        String func = generateExpression(stateArr);

        updateState(stateArr, "func=" + func);
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr, "numArgs=" + numArgs);
        String args = String.join(",", generateItems(this::generateExpression, stateArr, numArgs));

        updateState(stateArr, "args=" + args);
        String call = func + "(" + args + ")";
        boolean useNes = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "useNew=" + useNes);
        if (useNes) {
            return call;
        } else {
            return "new" + call;
        }
    }

    private String generateFunctionNode(String[] stateArr) {
        updateState(stateArr, "node=function");
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr, "numArgs=" + numArgs);
        return "function(" + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")"
                + generateBlock(stateArr);
    }

    private String generatePropertylNode(String[] stateArr) {
        updateState(stateArr, "node=property");
        return generateExpression(stateArr) + "." + generateIdentNode(stateArr);
    }

    private String generateIndexNode(String[] stateArr) {
        updateState(stateArr, "node=index");
        return generateExpression(stateArr) + "[" + generateExpression(stateArr) + "]";

    }

    private String generateArrowFunctionNode(String[] stateArr) {
        updateState(stateArr, "node=arrow");
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr, "numArgs=" + numArgs);
        String params = "(" + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")";

        boolean generateBlock = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "generateBlock=" + generateBlock);
        if (generateBlock) {
            return params + " => " + generateBlock(stateArr);
        } else {
            return params + " => " + generateExpression(stateArr);
        }
    }

    private String generateBlock(String[] stateArr) {
        updateState(stateArr, "node=block");
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr, "numArgs=" + numArgs);
        return "{ " + String.join(";", generateItems(this::generateStatement, stateArr, numArgs)) + " }";

    }

    private String generateBreakNode(String[] stateArr) {
        return "break";
    }

    private String generateContinueNode(String[] stateArr) {
        return "continue";
    }

    private String generateReturnNode(String[] stateArr) {
        updateState(stateArr, "node=return");
        boolean emptyReturn = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "emptyReturn=" + emptyReturn);
        return emptyReturn ? "return" : "return " + generateExpression(stateArr);
    }

    private String generateThrowNode(String[] stateArr) {
        updateState(stateArr, "node=throw");
        return "throw " + generateExpression(stateArr);
    }

    private String generateVarNode(String[] stateArr) {
        updateState(stateArr, "node=var");
        return "var " + generateIdentNode(stateArr);
    }

    private String generateEmptyNode(String[] stateArr) {
        return "";
    }

    private String generateIfNode(String[] stateArr) {
        updateState(stateArr, "node=if");
        String first = "if (" +
                generateExpression(stateArr) + ") " +
                generateBlock(stateArr);
        boolean generateBlock = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "generateBlock=" + generateBlock);
        return first +
                (generateBlock ? generateBlock(stateArr) : "");
    }

    private String generateForNode(String[] stateArr) {
        updateState(stateArr, "node=for");
        String s = "for(";
        boolean hasForExpr1 = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "hasForExpr1=" + hasForExpr1);
        if (hasForExpr1) {
            updateState(stateArr, "branch=1");
            s += generateExpression(stateArr);
        }
        s += ";";
        boolean hasForExpr2 = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "hasForExpr2=" + hasForExpr2);
        if (hasForExpr2) {
            updateState(stateArr, "branch=2");
            s += generateExpression(stateArr);
        }
        s += ";";
        boolean hasForExpr3 = (Boolean) guide.select(stateArr, boolId);
        updateState(stateArr, "hasForExpr3=" + hasForExpr3);
        if (hasForExpr3) {
            updateState(stateArr, "branch=3");
            s += generateExpression(stateArr);
        }
        s += ")";
        s += generateBlock(stateArr);
        return s;
    }

    private String generateWhileNode(String[] stateArr) {
        updateState(stateArr, "node=while");
        return "while (" + generateExpression(stateArr) + ")" + generateBlock(stateArr);

    }

    private String generateNamedFunctionNode(String[] stateArr) {
        updateState(stateArr, "node=namedfunc");
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr,"numArgs="+numArgs);
        return "function " + generateIdentNode(stateArr) + "(" + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")" + generateBlock(stateArr);
    }

    private String generateSwitchNode(String[] stateArr) {
        updateState(stateArr, "node=switch");
        int numArgs = (int) guide.select(stateArr, intId);
        updateState(stateArr,"numArgs="+numArgs);
        return "switch(" + generateExpression(stateArr) + ") {"
                + String.join(" ", generateItems(this::generateCaseNode, stateArr, numArgs) + "}");
    }

    private String generateTryNode(String[] stateArr) {
        updateState(stateArr, "node=try");
        return "try " + generateBlock(stateArr) + generateCatchNode(stateArr);

    }

    private String generateCatchNode(String[] stateArr) {
        updateState(stateArr, "node=catch");
        return "catch (" + generateIdentNode(stateArr) + ") " +
                generateBlock(stateArr);
    }


    private String generateObjectProperty(String[] stateArr) {
        updateState(stateArr, "node=property");
        return generateIdentNode(stateArr) + ": " + generateExpression(stateArr);
    }


    private String generateCaseNode(String[] stateArr) {
        updateState(stateArr, "node=case");
        return "case " + generateExpression(stateArr) + ": " + generateBlock(stateArr);
    }


    private <T> List<T> generateItems(Function<String[], T> generator, String[] stateArr, int len) {
        List<T> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            updateState(stateArr, "index=" + String.valueOf(i));
            items.add(generator.apply(stateArr));
        }
        return items;
    }

    /* Update and return new state. Removes items if too long */
    private void updateState(String[] stateArr, String stateX) {
        int end = stateSize - 1;
        stateArr[end] = stateX;
        System.arraycopy(stateArr, 1, stateArr, 0, end);
    }
}

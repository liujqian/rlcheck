package edu.berkeley.cs.jqf.examples.js;

import edu.berkeley.cs.jqf.fuzz.rl.Guide;
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

    int hasExpr1BoolID;
    int hasStat1BoolID;
    int genLitNode1BoolID;
    int genBracketsBoolID;
    int literalBoolBoolID;
    int addIdentifierBoolID;
    int useNewBoolID;
    int generateBlockBoolID;
    int emptyReturnBoolID;
    int generateElseBlockBoolID;
    int hasForExpr1BoolID;
    int hasForExpr2BoolID;
    int hasForExpr3BoolID;

    int literalStrCharID;
    int identifierPreCharID;

    int expression1SelectID;
    int expression2SelectID;
    int statements1SelectID;
    int statements2SelectID;
    int literalTypesSelectID;
    int identListSelectID;
    int unaryTokensSelectID;
    int binaryTokensSelectID;

    int numArgsLiteralNodeIntID;
    int literalIntIntID;
    int numArgsCallNodeIntID;
    int numArgsFunctionNodeIntID;
    int numArgsArrowFuncNodeIntID;
    int numArgsBlockNodeIntID;
    int numArgsNamedFuncNodeIntID;
    int numArgsSwitchNodeIntID;
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
        System.out.println("The state size for this run is " + this.stateSize);
        this.expression1SelectID = guide.addLearner(null, e);
        this.expression2SelectID = guide.addLearner(null, e);
        this.statements1SelectID = guide.addLearner(null, e);
        this.statements2SelectID = guide.addLearner(null, e);
        this.literalTypesSelectID = guide.addLearner(null, e);
        this.identListSelectID = guide.addLearner(null, e);
        this.unaryTokensSelectID = guide.addLearner(null, e);
        this.binaryTokensSelectID = guide.addLearner(null, e);

        this.numArgsLiteralNodeIntID = guide.addLearner(ints, e);
        this.literalIntIntID = guide.addLearner(ints, e);
        this.numArgsCallNodeIntID = guide.addLearner(ints, e);
        this.numArgsFunctionNodeIntID = guide.addLearner(ints, e);
        this.numArgsArrowFuncNodeIntID = guide.addLearner(ints, e);
        this.numArgsBlockNodeIntID = guide.addLearner(ints, e);
        this.numArgsNamedFuncNodeIntID = guide.addLearner(ints, e);
        this.numArgsSwitchNodeIntID = guide.addLearner(ints, e);

        this.hasExpr1BoolID = guide.addLearner(bools, e);
        this.hasStat1BoolID = guide.addLearner(bools, e);
        this.genLitNode1BoolID = guide.addLearner(bools, e);
        this.genBracketsBoolID = guide.addLearner(bools, e);
        this.literalBoolBoolID = guide.addLearner(bools, e);
        this.addIdentifierBoolID = guide.addLearner(bools, e);
        this.useNewBoolID = guide.addLearner(bools, e);
        this.generateBlockBoolID = guide.addLearner(bools, e);
        this.emptyReturnBoolID = guide.addLearner(bools, e);
        this.generateElseBlockBoolID = guide.addLearner(bools, e);
        this.hasForExpr1BoolID = guide.addLearner(bools, e);
        this.hasForExpr2BoolID = guide.addLearner(bools, e);
        this.hasForExpr3BoolID = guide.addLearner(bools, e);

        this.literalStrCharID = guide.addLearner(ascii, e);
        this.identifierPreCharID = guide.addLearner(ascii, e);
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
        expressionDepth++;
        String result;

        boolean greaterThanMaxExprDepth = expressionDepth >= MAX_EXPRESSION_DEPTH;
        boolean selected = false;
        if (!greaterThanMaxExprDepth) {
            selected = (Boolean) guide.select(stateArr, hasExpr1BoolID);
            updateState(stateArr, "haveExpression_1=" + selected);
        }

        if (greaterThanMaxExprDepth || selected) {
            String fn = (String) guide.select(
                    Arrays.asList(EXPRESSIONS_1),
                    stateArr,
                    expression1SelectID);
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
                    expression2SelectID);
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
        statementDepth++;
        String result;

        boolean greaterThanMaxStatementDepth = statementDepth >= MAX_STATEMENT_DEPTH;
        boolean selected = false;
        if (!greaterThanMaxStatementDepth) {
            selected = (Boolean) guide.select(stateArr, hasStat1BoolID);
            updateState(stateArr, "haveStatement_1=" + selected);
        }

        if (greaterThanMaxStatementDepth || selected) {
            String fn = (String) guide.select(
                    Arrays.asList(STATEMENTS_1),
                    stateArr,
                    statements1SelectID);
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
                    statements2SelectID);
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

        boolean lessThanMaxExprDepth = expressionDepth < MAX_EXPRESSION_DEPTH;
        boolean selected = false;
        if (lessThanMaxExprDepth) {
            selected = (Boolean) guide.select(stateArr, genLitNode1BoolID);
            updateState(stateArr, "generateLiteralNode_1=" + selected);
        }

        if (lessThanMaxExprDepth && selected) {
            // TODO multiple expressions in brackets
            int numArgs = (int) guide.select(stateArr, numArgsLiteralNodeIntID);
            updateState(stateArr, "numArgsLiteralNode=" + numArgs);

            boolean generateBrackets = (Boolean) guide.select(stateArr, genBracketsBoolID);
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
                    literalTypesSelectID);
            updateState(stateArr, "literalType=" + type);
            switch (type) {
                case "int":
                    int literalInt = (int) guide.select(stateArr, literalIntIntID);
                    updateState(stateArr, "literalInt=" + literalInt);
                    return String.valueOf(literalInt);
                case "boolean":
                    boolean literalBool = (boolean) guide.select(stateArr, literalBoolBoolID);
                    updateState(stateArr, "literalBool=" + literalBool);
                    return String.valueOf(literalBool);
                case "string":
                    Function<String[], String> genChr = s -> {
                        String literalChar = (String) guide.select(s, literalStrCharID);
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
        boolean emptyIdentifiers = identifiers.isEmpty();
        boolean lessThanMaxIdentifiers = identifiers.size() < MAX_IDENTIFIERS;
        boolean addIdentifier = false;
        if (!emptyIdentifiers && lessThanMaxIdentifiers) {
            addIdentifier = (Boolean) guide.select(stateArr, addIdentifierBoolID);
            updateState(stateArr, "addIdentifier=" + addIdentifier);
        }
        if (emptyIdentifiers || (lessThanMaxIdentifiers && addIdentifier)) {
            String identifier_pre = (String) guide.select(stateArr, identifierPreCharID);
            updateState(stateArr, "identifierPre=" + identifier_pre);
            identifier = identifier_pre + "_" + identifiers.size();
            identifiers.add(identifier);
        } else {
            List<Object> identList = new ArrayList<>(identifiers);
            String selectedId = (String) guide.select(identList, stateArr, identListSelectID);
            updateState(stateArr, "selectedIdentifier=" + selectedId);
            identifier = selectedId;
        }
        return identifier;
    }

    private String generateUnaryNode(String[] stateArr) {
        String token = (String) guide.select(
                Arrays.asList(UNARY_TOKENS),
                stateArr,
                unaryTokensSelectID);
        updateState(stateArr, "unary=" + token);
        return token + " " + generateExpression(stateArr);
    }

    private String generateBinaryNode(String[] stateArr) {
        String token = (String) guide.select(
                Arrays.asList(BINARY_TOKENS),
                stateArr,
                binaryTokensSelectID);
        updateState(stateArr, "binary=" + token);
        String lhs = generateExpression(stateArr);
        String rhs = generateExpression(stateArr);
        return lhs + " " + token + " " + rhs;
    }

    private String generateTernaryNode(String[] stateArr) {
        return generateExpression(stateArr) + " ? " + generateExpression(stateArr) +
                " : " + generateExpression(stateArr);
    }

    private String generateCallNode(String[] stateArr) {
        String func = generateExpression(stateArr);
        int numArgs = (int) guide.select(stateArr, numArgsCallNodeIntID);
        updateState(stateArr, "numArgsCallNode=" + numArgs);
        String args = String.join(",", generateItems(this::generateExpression, stateArr, numArgs));

        String call = func + "(" + args + ")";
        boolean useNes = (Boolean) guide.select(stateArr, useNewBoolID);
        updateState(stateArr, "useNew=" + useNes);
        if (useNes) {
            return call;
        } else {
            return "new" + call;
        }
    }

    private String generateFunctionNode(String[] stateArr) {
        int numArgs = (int) guide.select(stateArr, numArgsFunctionNodeIntID);
        updateState(stateArr, "numArgsFunctionNode=" + numArgs);
        return "function(" + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")"
                + generateBlock(stateArr);
    }

    private String generatePropertylNode(String[] stateArr) {
        return generateExpression(stateArr) + "." + generateIdentNode(stateArr);
    }

    private String generateIndexNode(String[] stateArr) {
        return generateExpression(stateArr) + "[" + generateExpression(stateArr) + "]";

    }

    private String generateArrowFunctionNode(String[] stateArr) {
        int numArgs = (int) guide.select(stateArr, numArgsArrowFuncNodeIntID);
        updateState(stateArr, "numArgsArrowFuncNode=" + numArgs);
        String params = "(" + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")";

        boolean generateBlock = (Boolean) guide.select(stateArr, generateBlockBoolID);
        updateState(stateArr, "generateBlock=" + generateBlock);
        if (generateBlock) {
            return params + " => " + generateBlock(stateArr);
        } else {
            return params + " => " + generateExpression(stateArr);
        }
    }

    private String generateBlock(String[] stateArr) {
        int numArgs = (int) guide.select(stateArr, numArgsBlockNodeIntID);
        updateState(stateArr, "numArgsBlockNode=" + numArgs);
        return "{ " + String.join(";", generateItems(this::generateStatement, stateArr, numArgs)) + " }";

    }

    private String generateBreakNode(String[] stateArr) {
        return "break";
    }

    private String generateContinueNode(String[] stateArr) {
        return "continue";
    }

    private String generateReturnNode(String[] stateArr) {
        boolean emptyReturn = (Boolean) guide.select(stateArr, emptyReturnBoolID);
        updateState(stateArr, "emptyReturn=" + emptyReturn);
        return emptyReturn ? "return" : "return " + generateExpression(stateArr);
    }

    private String generateThrowNode(String[] stateArr) {
        return "throw " + generateExpression(stateArr);
    }

    private String generateVarNode(String[] stateArr) {
        return "var " + generateIdentNode(stateArr);
    }

    private String generateEmptyNode(String[] stateArr) {
        return "";
    }

    private String generateIfNode(String[] stateArr) {
        String first = "if (" +
                generateExpression(stateArr) + ") " +
                generateBlock(stateArr);
        boolean generateBlock = (Boolean) guide.select(stateArr, generateElseBlockBoolID);
        updateState(stateArr, "generateElseBlock=" + generateBlock);
        return first +
                (generateBlock ? generateBlock(stateArr) : "");
    }

    private String generateForNode(String[] stateArr) {
        String s = "for(";
        boolean hasForExpr1 = (Boolean) guide.select(stateArr, hasForExpr1BoolID);
        updateState(stateArr, "hasForExpr1=" + hasForExpr1);
        if (hasForExpr1) {
            s += generateExpression(stateArr);
        }
        s += ";";
        boolean hasForExpr2 = (Boolean) guide.select(stateArr, hasForExpr2BoolID);
        updateState(stateArr, "hasForExpr2=" + hasForExpr2);
        if (hasForExpr2) {
            s += generateExpression(stateArr);
        }
        s += ";";
        boolean hasForExpr3 = (Boolean) guide.select(stateArr, hasForExpr3BoolID);
        updateState(stateArr, "hasForExpr3=" + hasForExpr3);
        if (hasForExpr3) {
            s += generateExpression(stateArr);
        }
        s += ")";
        s += generateBlock(stateArr);
        return s;
    }

    private String generateWhileNode(String[] stateArr) {
        return "while (" + generateExpression(stateArr) + ")" + generateBlock(stateArr);

    }

    private String generateNamedFunctionNode(String[] stateArr) {
        int numArgs = (int) guide.select(stateArr, numArgsNamedFuncNodeIntID);
        updateState(stateArr, "numArgsNamedFuncNode=" + numArgs);
        return "function " + generateIdentNode(stateArr) + "("
                + String.join(", ", generateItems(this::generateIdentNode, stateArr, numArgs)) + ")"
                + generateBlock(stateArr);
    }

    private String generateSwitchNode(String[] stateArr) {
        int numArgs = (int) guide.select(stateArr, numArgsSwitchNodeIntID);
        updateState(stateArr, "numArgsSwitchNode=" + numArgs);
        return "switch(" + generateExpression(stateArr) + ") {"
                + String.join(" ", generateItems(this::generateCaseNode, stateArr, numArgs) + "}");
    }

    private String generateTryNode(String[] stateArr) {
        return "try " + generateBlock(stateArr) + generateCatchNode(stateArr);

    }

    private String generateCatchNode(String[] stateArr) {
        return "catch (" + generateIdentNode(stateArr) + ") " +
                generateBlock(stateArr);
    }

    private String generateObjectProperty(String[] stateArr) {
        return generateIdentNode(stateArr) + ": " + generateExpression(stateArr);
    }

    private String generateCaseNode(String[] stateArr) {
        return "case " + generateExpression(stateArr) + ": " + generateBlock(stateArr);
    }

    private <T> List<T> generateItems(Function<String[], T> generator, String[] stateArr, int len) {
        List<T> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(generator.apply(stateArr));
        }
        return items;
    }

    /* Update and return new state. Removes items if too long */
    private void updateState(String[] stateArr, String stateX) {
        int end = stateSize - 1;
        System.arraycopy(stateArr, 1, stateArr, 0, end);
        stateArr[end] = stateX;
    }
}

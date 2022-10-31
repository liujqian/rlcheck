package edu.berkeley.cs.jqf.fuzz.rl.xml;


import edu.berkeley.cs.jqf.fuzz.rl.RLGenerator;
import edu.berkeley.cs.jqf.fuzz.rl.RLGuide;
import edu.berkeley.cs.jqf.fuzz.rl.RLParams;
import edu.berkeley.cs.jqf.fuzz.rl.experiments.TrieBasedMonteCarloLearner;
import edu.berkeley.cs.jqf.fuzz.rl.experiments.TrieBasedSarsaLearner;
import org.junit.Assume;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class XmlRLGeneratorWithTrieSarsaGuide implements RLGenerator {
    TrieBasedSarsaLearner learner;
    private static DocumentBuilderFactory documentBuilderFactory =
            DocumentBuilderFactory.newInstance();


    /**
     * Max number of child nodes for each XML element.
     */
    private static final int MAX_NUM_CHILDREN = 4;

    /**
     * Max number of attributes for each XML element.
     */
    private static final int MAX_NUM_ATTRIBUTES = 2;

    /**
     * Minimum size of XML tree.
     */
    private int minDepth = 0;

    /**
     * Maximum size of XML tree.
     */
    private int maxDepth = 5;

    /**
     * Terminal action output
     */
//    public static final String terminal = "END";
    private final List<Object> BOOLEANS = Arrays.asList(new Boolean[]{true, false});
    private final List<Object> NUM_C = Arrays.asList(RLGuide.range(0, MAX_NUM_CHILDREN));
    private final List<Object> NUM_A = Arrays.asList(RLGuide.range(0, MAX_NUM_ATTRIBUTES));
    private List<Object> TAGS = null;

    /* Need to initialize with parameters using init method after constructor is called. */
    public XmlRLGeneratorWithTrieSarsaGuide() {
    }

    /**
     * Initialize models and generator parameters
     *
     * @param params: stateSize,
     *                tags,
     *                numc,
     *                defaultEpsilon
     */
    @Override
    public void init(RLParams params) {
        System.out.println("The \"init\" method of SequentialStateXmlRLGeneratorWithTrieGuide is called!");
        double e = (double) params.get("defaultEpsilon", true);
        if (params.exists("seed")) {
            learner = new TrieBasedSarsaLearner(e, 0.5, 1, new Random((long) params.get("seed")));
        } else {
            learner = new TrieBasedSarsaLearner(e, 0.5, 1, null);
        }
        TAGS = (List<Object>) params.get("tags", true);
    }

    /**
     * Generators a random XML document.
     *
     * @return a randomly-generated XML document
     */
    @Override
    public String generate() {
        DocumentBuilder builder;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        Document document = builder.newDocument();
        try {
            populateDocument(document);
        } catch (DOMException e) {
            Assume.assumeNoException(e);
        }
        return XMLDocumentUtils.documentToString(document);
    }

    /**
     * Update using reward r
     */
    @Override
    public void update(int r) {
        learner.update(r);
    }

    private Document populateDocument(Document document) {
        Element root = generateXmlTree(document, 0);
        if (root != null) {
            document.appendChild(root);
        }
        return document;
    }

    /**
     * Recursively generate XML
     */
    private Element generateXmlTree(Document document, int depth) {
        String rootTag = (String) learner.select(TAGS);

        Element root = document.createElement(rootTag);
        // Add attributes
        int numAttributes = (Integer) learner.select(NUM_A);

        for (int i = 0; i < numAttributes; i++) {
            String attrKey = (String) learner.select(TAGS);
            String attrValue = (String) learner.select(TAGS);
            root.setAttribute(attrKey, attrValue);
        }

        boolean lessThanMinDepth = depth < minDepth;
        boolean lessThanMaxDepth = depth < maxDepth;
        boolean guideSelectHaveChild = false;

        if ((!lessThanMinDepth) && lessThanMaxDepth) {
            guideSelectHaveChild = (boolean) learner.select(BOOLEANS);
        }
        boolean haveChild = lessThanMinDepth || (lessThanMaxDepth && guideSelectHaveChild);
        boolean guideSelectHaveText = false;
        if (!haveChild) {
            guideSelectHaveText = (boolean) learner.select(BOOLEANS);
        }
        boolean guideSelectHaveCDATA = false;
        if (!haveChild && !guideSelectHaveText) {
            guideSelectHaveCDATA = (boolean) learner.select(BOOLEANS);
        }

        if (haveChild) {
            int numChildren = (Integer) learner.select(NUM_C);
            for (int i = 0; i < numChildren; i++) {
                Element child = generateXmlTree(document, depth + 1);
                if (child != null) {
                    root.appendChild(child);
                }
            }
        } else if (guideSelectHaveText) {
            String textVal = (String) learner.select(TAGS);
            Text text = document.createTextNode(textVal);
            root.appendChild(text);
        } else if (guideSelectHaveCDATA) {
            String textVal = (String) learner.select(TAGS);
            Text text = document.createCDATASection(textVal);
            root.appendChild(text);
        }
        return root;
    }
}
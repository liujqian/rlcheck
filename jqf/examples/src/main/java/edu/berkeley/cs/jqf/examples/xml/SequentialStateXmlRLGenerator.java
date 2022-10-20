package edu.berkeley.cs.jqf.examples.xml;

import edu.berkeley.cs.jqf.fuzz.rl.RLGenerator;
import edu.berkeley.cs.jqf.fuzz.rl.RLGuide;
import edu.berkeley.cs.jqf.fuzz.rl.RLParams;
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


/**
 * Created by clemieux on 6/17/19.
 */
public class SequentialStateXmlRLGenerator implements RLGenerator {

    private RLGuide guide;


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


    private int stateSize;

    private int haveChildBoolID;
    private int haveTextBoolID;
    private int haveCDATABoolID;
    private int tagTextID;
    private int attrKeyTextID;
    private int attrValTextID;
    private int textTextID;
    private int cdataTextID;
    private int numcId;
    private int numaId;

    /* Need to initialize with parameters using init method after constructor is called. */
    public SequentialStateXmlRLGenerator() {
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
        System.out.println("init of XMLRLGenerator is called!");
        if (params.exists("seed")) {
            guide = new RLGuide((long) params.get("seed"));
        } else {
            guide = new RLGuide();
        }
        double e = (double) params.get("defaultEpsilon", true);
        List<Object> text = (List<Object>) params.get("tags", true);

        this.stateSize = (int) params.get("stateSize", true);
        System.out.println("The state size for this run is " + stateSize);
        this.tagTextID = guide.addLearner(text, e);
        this.attrKeyTextID = guide.addLearner(text, e);
        this.attrValTextID = guide.addLearner(text, e);
        this.textTextID = guide.addLearner(text, e);
        this.cdataTextID = guide.addLearner(text, e);


        this.haveCDATABoolID = guide.addLearner(BOOLEANS, e);
        this.haveChildBoolID = guide.addLearner(BOOLEANS, e);
        this.haveTextBoolID = guide.addLearner(BOOLEANS, e);

        this.numcId = guide.addLearner(NUM_C, e); // num children
        this.numaId = guide.addLearner(NUM_A, e); // num attributes
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
        guide.update(r);
    }


    private Document populateDocument(Document document) {
        String[] stateArr = new String[stateSize];
        Element root = generateXmlTree(stateArr, document, 0);
        if (root != null) {
            document.appendChild(root);
        }
        return document;
    }

    /**
     * Recursively generate XML
     */
    private Element generateXmlTree(String[] stateArr, Document document, int depth) {
        String rootTag = (String) guide.select(stateArr, tagTextID);
        updateState(stateArr, "tag=" + rootTag);

        Element root = document.createElement(rootTag);
        // Add attributes
        int numAttributes = (Integer) guide.select(stateArr, numaId);
        updateState(stateArr, "numAttr=" + rootTag);

        for (int i = 0; i < numAttributes; i++) {
            String attrKey = (String) guide.select(stateArr, attrKeyTextID);
            updateState(stateArr, "attrKey=" + attrKey);
            String attrValue = (String) guide.select(stateArr, attrValTextID);
            updateState(stateArr, "attrVal=" + attrKey);
            root.setAttribute(attrKey, attrValue);
        }

        boolean lessThanMinDepth = depth < minDepth;
        boolean lessThanMaxDepth = depth < maxDepth;
        boolean guideSelectHaveChild = false;

        if (!lessThanMinDepth && lessThanMaxDepth) {
            guideSelectHaveChild = (boolean) guide.select(stateArr, haveChildBoolID);
            updateState(stateArr, "haveChild=" + guideSelectHaveChild);
        }
        boolean haveChild = lessThanMinDepth || (lessThanMaxDepth && guideSelectHaveChild);
        boolean guideSelectHaveText = false;
        if (!haveChild) {
            guideSelectHaveText = (boolean) guide.select(stateArr, haveTextBoolID);
            updateState(stateArr, "haveText=" + guideSelectHaveText);
        }
        boolean guideSelectHaveCDATA = false;
        if (!haveChild && !guideSelectHaveText) {
            guideSelectHaveCDATA = (boolean) guide.select(stateArr, haveCDATABoolID);
            updateState(stateArr, "haveCDATA=" + guideSelectHaveCDATA);
        }

        if (haveChild) {
            int numChildren = (Integer) guide.select(stateArr, numcId);
            updateState(stateArr, "numChild=" + numChildren);
            for (int i = 0; i < numChildren; i++) {
                Element child = generateXmlTree(stateArr, document, depth + 1);
                if (child != null) {
                    root.appendChild(child);
                }
            }
        } else if (guideSelectHaveText) {
            String textVal = (String) guide.select(stateArr, textTextID);
            updateState(stateArr, "text=" + textVal);
            Text text = document.createTextNode(textVal);
            root.appendChild(text);
        } else if (guideSelectHaveCDATA) {
            String textVal = (String) guide.select(stateArr, cdataTextID);
            updateState(stateArr, "CDATA=" + textVal);
            Text text = document.createCDATASection(textVal);
            root.appendChild(text);
        }
        return root;
    }

    /* Update the state array in the original array! */
    private void updateState(String[] stateArr, String stateX) {
        int end = stateSize - 1;
        stateArr[end] = stateX;
        System.arraycopy(stateArr, 1, stateArr, 0, end);
    }
}






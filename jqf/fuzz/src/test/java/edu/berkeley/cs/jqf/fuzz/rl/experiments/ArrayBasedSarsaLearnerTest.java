package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RunWith(JUnit4.class)
public class ArrayBasedSarsaLearnerTest {
    Long seed = (long) 69;
    private static List<Object> bools = Arrays.asList(true, false);
    private static List<Object> ints = Arrays.asList(1, 2, 3, 4);
    private static List<Object> strs = Arrays.asList("Amy", "Bob", "Charlie");

    @Test
    public void maxTest() {
        ArrayBasedSarsaLearner l = new ArrayBasedSarsaLearner(0.0, 1, 1, new Random(seed), 2);
        Assert.assertArrayEquals(new String[]{null, null}, l.currentState);
        Assert.assertEquals(1, l.qTable.size());
        Assert.assertEquals(1, l.qTable.get("").size());
        Assert.assertTrue(l.qTable.get("").containsKey("BEGIN"));
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("", "BEGIN"), l.episode.get(0));
        int b = (int) l.select(ints);
        Assert.assertEquals(2, b);
        l.update(10);
        Assert.assertEquals(10, l.qTable.get("BEGIN").get(2)[l.NEW_Q_INDEX], 1e-9);

        l.qTable.get("BEGIN").put(1, new Double[]{0.1, 0.2});
        l.qTable.get("BEGIN").put(3, new Double[]{0.4, 0.5});
        l.qTable.get("BEGIN").put(4, new Double[]{0.9, 0.8});
        for (int i = 0; i < 1000; i++) {
            l.currentState = new String[]{null, null};
            int s = (int) l.select(ints);
            Assert.assertEquals(2, s);
            l.episode.remove(1);
        }
    }

    @Test
    public void episilonTest() {
        ArrayBasedSarsaLearner l = new ArrayBasedSarsaLearner(0.5, 0.5, 1, new Random(seed), 2);
        Assert.assertArrayEquals(new String[]{null, null}, l.currentState);
        Assert.assertEquals(1, l.qTable.size());
        Assert.assertEquals(1, l.qTable.get("").size());
        Assert.assertTrue(l.qTable.get("").containsKey("BEGIN"));
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("", "BEGIN"), l.episode.get(0));
        boolean b = (boolean) l.select(bools);
        Assert.assertFalse(b);

        Assert.assertArrayEquals(new String[]{null, "BEGIN"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN", false), l.episode.get(1));
        l.update(10);

        b = (boolean) l.select(bools);
        Assert.assertFalse(b);
        l.update(1);
        Assert.assertEquals(3, l.qTable.get("BEGIN").get(false)[l.NEW_Q_INDEX], 1e-9);

        double falseCnt = 0;
        double trueCnt = 0;
        for (int i = 0; i < 10000000; i++) {
            b = (boolean) l.select(bools);
            if (b) {
                trueCnt += 1;
            } else {
                falseCnt += 1;
            }
            l.update(0);
        }
        System.out.println("The ratio of trues to total tries is " + (trueCnt / (trueCnt + falseCnt)));
        Assert.assertEquals(0.25, trueCnt / (trueCnt + falseCnt), 0.01);
    }

    @Test
    public void selectTest1() {
        ArrayBasedSarsaLearner l = new ArrayBasedSarsaLearner(0.0, 1, 1, new Random(seed), 3);
        Assert.assertArrayEquals(new String[]{null, null, null}, l.currentState);
        Assert.assertEquals(1, l.qTable.size());
        Assert.assertEquals(1, l.qTable.get("").size());
        Assert.assertTrue(l.qTable.get("").containsKey("BEGIN"));
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("", "BEGIN"), l.episode.get(0));

        String s = (String) l.select(strs);
        Assert.assertEquals("Amy", s);
        Assert.assertArrayEquals(new String[]{null, null, "BEGIN"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN", "Amy"), l.episode.get(1));
        Assert.assertTrue(l.qTable.get("BEGIN").containsKey("Amy"));

        int i = (int) l.select(ints);
        Assert.assertEquals(1, i);
        Assert.assertArrayEquals(new String[]{null, "BEGIN", "Amy"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN | Amy", 1), l.episode.get(2));
        Assert.assertTrue(l.qTable.get("BEGIN | Amy").containsKey(1));

        boolean b1 = (boolean) l.select(bools);
        boolean b2 = (boolean) l.select(bools);
        Assert.assertFalse(b1);
        Assert.assertFalse(b2);
        Assert.assertArrayEquals(new String[]{"Amy", "1", "false"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN | Amy | 1", false), l.episode.get(3));
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("Amy | 1 | false", false), l.episode.get(4));

        l.update(20);
        Assert.assertArrayEquals(new String[]{null, null, null}, l.currentState);
        Assert.assertEquals(1, l.episode.size());
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("", "BEGIN"), l.episode.get(0));
        Assert.assertEquals(20, l.qTable.get("Amy | 1 | false").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("Amy | 1 | false").get(false)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy | 1").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy | 1").get(false)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy").get(1)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy").get(1)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN").get("Amy")[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN").get("Amy")[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("").get("BEGIN")[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("").get("BEGIN")[l.OLD_Q_INDEX], 1e-9);

        s = (String) l.select(strs);
        Assert.assertEquals("Amy", s);
        Assert.assertArrayEquals(new String[]{null, null, "BEGIN"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN", "Amy"), l.episode.get(1));
        Assert.assertTrue(l.qTable.get("BEGIN").containsKey("Amy"));
        i = (int) l.select(ints);
        Assert.assertEquals(1, i);
        b1 = (boolean) l.select(bools);
        b2 = (boolean) l.select(bools);
        Assert.assertFalse(b1);
        Assert.assertFalse(b2);
        l.update(1);
        Assert.assertEquals(1, l.qTable.get("Amy | 1 | false").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(20, l.qTable.get("BEGIN | Amy | 1").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy | 1").get(false)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy").get(1)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN | Amy").get(1)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN").get("Amy")[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("BEGIN").get("Amy")[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("").get("BEGIN")[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(0, l.qTable.get("").get("BEGIN")[l.OLD_Q_INDEX], 1e-9);

        s = (String) l.select(strs);
        Assert.assertEquals("Amy", s);
        Assert.assertArrayEquals(new String[]{null, null, "BEGIN"}, l.currentState);
        Assert.assertEquals(new AbstractMap.SimpleEntry<String, Object>("BEGIN", "Amy"), l.episode.get(1));
        Assert.assertTrue(l.qTable.get("BEGIN").containsKey("Amy"));

        i = (int) l.select(ints);
        Assert.assertEquals(1, i);
        b1 = (boolean) l.select(bools);
        b2 = (boolean) l.select(bools);
        Assert.assertFalse(b1);
        Assert.assertFalse(b2);
        l.update(3);
        Assert.assertEquals(3, l.qTable.get("Amy | 1 | false").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(1, l.qTable.get("BEGIN | Amy | 1").get(false)[l.NEW_Q_INDEX], 1e-9);
        Assert.assertEquals(20, l.qTable.get("BEGIN | Amy | 1").get(false)[l.OLD_Q_INDEX], 1e-9);
        Assert.assertEquals(20, l.qTable.get("BEGIN | Amy").get(1)[l.NEW_Q_INDEX], 1e-9);
    }

    @Test
    public void selectTest2() {
        ArrayBasedSarsaLearner l = new ArrayBasedSarsaLearner(0.5, 0.5, 1, new Random(seed), 2);
        String s = (String) l.select(strs);
        int i = (int) l.select(ints);
        Assert.assertEquals("Amy", s);
        Assert.assertEquals(1, i);
        Assert.assertArrayEquals(new String[]{"BEGIN", "Amy"}, l.currentState);
        l.update(20);
        Assert.assertEquals(10, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);

        s = (String) l.select(strs);
        i = (int) l.select(ints);
        Assert.assertEquals("Amy", s);
        Assert.assertEquals(3, i);
        l.update(-2);
        Assert.assertEquals(10, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        Assert.assertEquals(0, l.getQ(l.stateToString(new String[]{null, "BEGIN",}), "Amy"), 1e-9);
        Assert.assertEquals(-1, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 3), 1e-9);

        s = (String) l.select(strs);
        i = (int) l.select(ints);
        Assert.assertEquals("Amy", s);
        Assert.assertEquals(1, i);
        Assert.assertEquals(10, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{null, "BEGIN",}), "Amy"), 1e-9);
        boolean b = (boolean) l.select(bools);
        Assert.assertTrue(b);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        Assert.assertEquals(0, l.getQ(l.stateToString(new String[]{ "Amy","1"}), true), 1e-9);
        l.update(-2);
        Assert.assertEquals(-1, l.getQ(l.stateToString(new String[]{ "Amy","1"}), true), 1e-9);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{null, "BEGIN",}), "Amy"), 1e-9);

        s = (String) l.select(strs);
        i = (int) l.select(ints);
        Assert.assertEquals("Amy", s);
        Assert.assertEquals(1, i);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{null, "BEGIN",}), "Amy"), 1e-9);
        b = (boolean) l.select(bools);
        Assert.assertTrue(b);
        Assert.assertEquals(2, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        l.update(4);
        Assert.assertEquals(1.5, l.getQ(l.stateToString(new String[]{ "Amy","1"}), true), 1e-9);
        Assert.assertEquals(2, l.getQ(l.stateToString(new String[]{"BEGIN", "Amy"}), 1), 1e-9);
        Assert.assertEquals(5, l.getQ(l.stateToString(new String[]{null, "BEGIN",}), "Amy"), 1e-9);
    }
}

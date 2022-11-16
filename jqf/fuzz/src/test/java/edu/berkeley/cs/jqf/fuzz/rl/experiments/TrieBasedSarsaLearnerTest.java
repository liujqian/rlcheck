package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RunWith(JUnit4.class)
public class TrieBasedSarsaLearnerTest {
    private static List<Object> bools = Arrays.asList(true, false);
    private static List<Object> ints = Arrays.asList(1, 2, 3, 4);
    private static List<Object> strs = Arrays.asList("Amy", "Bob", "Charlie");
    private static long seed = 69;


    @Test
    public void testMax() {
        Random r = new Random(seed);
        for (int i = 0; i < 1000; i++) {
            TrieBasedSarsaLearner l = new TrieBasedSarsaLearner(0.0, 0.5, 1, r);
            TrieBasedSarsaLearner.State firstState = l.rootState.getSelectedAction().getNextState();
            Assert.assertEquals(0, firstState.getActions().size());
            TrieBasedSarsaLearner.Action a1 = new TrieBasedSarsaLearner.Action(1, 0.5, firstState, r);
            a1.updateQ(1e5);
            firstState.getActions().put(1, a1);
            TrieBasedSarsaLearner.Action a2 = new TrieBasedSarsaLearner.Action(2, 0.5, firstState, r);
            a2.updateQ(1e4);
            firstState.getActions().put(2, a2);
            TrieBasedSarsaLearner.Action a3 = new TrieBasedSarsaLearner.Action(3, 0.5, firstState, r);
            a3.updateQ(1e6);
            firstState.getActions().put(3, a3);
            TrieBasedSarsaLearner.Action a4 = new TrieBasedSarsaLearner.Action(4, 0.5, firstState, r);
            a4.updateQ(1e2);
            firstState.getActions().put(4, a4);
            Assert.assertEquals(3, (int) l.select(ints));
        }
    }

    @Test
    public void testEpisilon() {
        TrieBasedSarsaLearner l = new TrieBasedSarsaLearner(0.5, 0.5, 1, new Random(seed));
        Assert.assertEquals(l.currentState, l.rootState);
        Assert.assertEquals(1, l.currentState.getActions().size());
        Assert.assertTrue(l.currentState.getActions().containsKey(TrieBasedSarsaLearner.BEGIN_STATE));
        boolean b = (boolean) l.select(bools);
        Assert.assertFalse(b);
        l.update(20);

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
    public void testSelect1() {
        TrieBasedSarsaLearner l = new TrieBasedSarsaLearner(0, 0.5, 1, new Random(seed));
        Assert.assertEquals(l.currentState, l.rootState);
        Assert.assertEquals(1, l.currentState.getActions().size());
        Assert.assertTrue(l.currentState.getActions().containsKey(TrieBasedSarsaLearner.BEGIN_STATE));

        String s = (String) l.select(strs);
        Assert.assertEquals("Charlie", s);
        Assert.assertEquals("Charlie", l.currentState.getSelectedAction().toString());
        Assert.assertEquals(0, l.currentState.getSelectedAction().getQ(), 0);
        Assert.assertTrue(l.currentState.getActions().containsKey("Charlie"));
        Assert.assertEquals(l.rootState, l.currentState.getPreviousAction().getParentState());

        int i = (int) l.select(ints);
        Assert.assertEquals(4, i);
        Assert.assertEquals("* | begin | Charlie ", l.currentState.toString());
        Assert.assertEquals("4", l.currentState.getSelectedAction().toString());
        Assert.assertEquals("Charlie", l.currentState.getPreviousAction().toString());

        l.update(20);
        Assert.assertEquals(l.currentState, l.rootState);
        Assert.assertEquals(TrieBasedSarsaLearner.BEGIN_STATE, l.currentState.getSelectedAction().toString());
        Assert.assertEquals(10, l.currentState.getSelectedAction().getNextState().getActions().get("Charlie").getNextState().getActions().get(4).getQ(), 1e-8);
        Assert.assertEquals(0, l.currentState.getSelectedAction().getNextState().getActions().get("Charlie").getQ(), 1e-8);
        Assert.assertEquals(0, l.currentState.getSelectedAction().getNextState().getActions().get("Charlie").getNextState().getActions().get(4).getNextState().getActions().size());

        s = (String) l.select(strs);
        i = (int) l.select(ints);
        Assert.assertEquals(4, i);
        Assert.assertEquals("Charlie", s);
        Assert.assertEquals(10, l.rootState.getSelectedAction().getNextState().getActions().get("Charlie").getNextState().getActions().get(4).getQ(), 1e-8);
        Assert.assertEquals(5, l.rootState.getSelectedAction().getNextState().getActions().get("Charlie").getQ(), 1e-8);
        boolean bool = (boolean) l.select(bools);
        Assert.assertFalse(bool);
        Assert.assertEquals(0, l.currentState.getSelectedAction().getQ(), 1e-9);
        Assert.assertEquals("false", l.currentState.getSelectedAction().toString());
        l.update(-8);
        Assert.assertEquals(5, l.rootState.getSelectedAction().getNextState().getActions().get("Charlie").getNextState().getActions().get(4).getQ(), 1e-8);
        Assert.assertEquals(5, l.rootState.getSelectedAction().getNextState().getActions().get("Charlie").getQ(), 1e-8);
        Assert.assertEquals(
                -4,
                l.rootState.getSelectedAction().getNextState().getActions().get("Charlie").getNextState().getActions().get(4).getNextState().getActions().get(false).getQ(),
                1e-8
        );
    }

    @Test
    public void testSelect2() {
        TrieBasedSarsaLearner l = new TrieBasedSarsaLearner(0.5, 0.5, 1, new Random(seed));
        int i = (int) l.select(ints);
        Assert.assertEquals(4, i);
        boolean b = (boolean) l.select(bools);
        Assert.assertFalse(b);
        String s = (String) l.select(strs);
        Assert.assertEquals("Charlie", s);
        l.update(-10);
        Assert.assertEquals(
                -5,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getNextState().getActions().get(false).getNextState().getActions().get("Charlie").getQ(),
                1e-9
        );

        i = (int) l.select(ints);
        Assert.assertEquals(4, i);
        b = (boolean) l.select(bools);
        Assert.assertFalse(b);
        s = (String) l.select(strs);
        Assert.assertEquals("Charlie", s);
        String s2 = (String) l.select(strs);
        Assert.assertEquals("Amy", s2);
        l.update(30);
        Assert.assertEquals(
                15,
                l.rootState.
                        getSelectedAction().
                        getNextState().
                        getActions().
                        get(4).
                        getNextState().
                        getActions().
                        get(false).
                        getNextState().
                        getActions().
                        get("Charlie").
                        getNextState().getActions().get("Amy").getQ(),
                1e-9
        );
        Assert.assertEquals(
                -2.5,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getNextState().getActions().get(false).getNextState().getActions().get("Charlie").getQ(),
                1e-9
        );
        Assert.assertEquals(
                -2.5,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getNextState().getActions().get(false).getQ(),
                1e-9
        );
        Assert.assertEquals(
                0,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getQ(),
                1e-9
        );

        i = (int) l.select(ints);
        Assert.assertEquals(4, i);
        b = (boolean) l.select(bools);
        Assert.assertFalse(b);
        s = (String) l.select(strs);
        Assert.assertEquals("Charlie", s);
         s2 = (String) l.select(strs);
        Assert.assertEquals("Charlie", s2);
        l.update(20);
        Assert.assertEquals(
                15,
                l.rootState.
                        getSelectedAction().
                        getNextState().
                        getActions().
                        get(4).
                        getNextState().
                        getActions().
                        get(false).
                        getNextState().
                        getActions().
                        get("Charlie").
                        getNextState().getActions().get("Amy").getQ(),
                1e-9
        );
        Assert.assertEquals(
                10,
                l.rootState.
                        getSelectedAction().
                        getNextState().
                        getActions().
                        get(4).
                        getNextState().
                        getActions().
                        get(false).
                        getNextState().
                        getActions().
                        get("Charlie").
                        getNextState().getActions().get("Charlie").getQ(),
                1e-9
        );
        Assert.assertEquals(
                -1.25,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getNextState().getActions().get(false).getNextState().getActions().get("Charlie").getQ(),
                1e-9
        );
        Assert.assertEquals(
                -2.5,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getNextState().getActions().get(false).getQ(),
                1e-9
        );
        Assert.assertEquals(
                -1.25,
                l.rootState.getSelectedAction().getNextState().getActions().get(4).getQ(),
                1e-9
        );
    }
}

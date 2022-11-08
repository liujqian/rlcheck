package edu.berkeley.cs.jqf.fuzz.rl.experiments;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@RunWith(JUnit4.class)
public class TrieBasedMonteCarloLearnerTest {
    private static List<Object> bools = Arrays.asList(true, false);
    private static List<Object> ints = Arrays.asList(1, 2, 3, 4);
    private static List<Object> strs = Arrays.asList("Amy", "Bob", "Charlie");
    private static long SEED = 69;

    @Test
    public void episilonTest() {
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.0, new Random(SEED), false);
        boolean b = (boolean) learner.select(bools);
        Assert.assertFalse(b);
        learner.update(10);
        Assert.assertEquals(learner.getCurrentState(), learner.getRootState());
        Assert.assertEquals(1, learner.getRootState().getActions().size());
        Assert.assertEquals(10, learner.getRootState().getActions().get(false).getQ(), 1e-9);
        Assert.assertEquals(1, learner.getRootState().getActions().get(false).getCount());
        for (int i = 0; i < 10000; i++) {
            b = (boolean) learner.select(bools);
            Assert.assertFalse(b);
            learner.update(0);
        }

        learner = new TrieBasedMonteCarloLearner(0.5, new Random(SEED), false);
        b = (boolean) learner.select(bools);
        Assert.assertFalse(b);
        learner.update(10);
        double falseCnt = 0;
        double trueCnt = 0;
        for (int i = 0; i < 10000000; i++) {
            b = (boolean) learner.select(bools);
            if (b) {
                trueCnt += 1;
            } else {
                falseCnt += 1;
            }
            learner.update(0);
        }
        System.out.println("The ratio of trues to total tries is " + (trueCnt / (trueCnt + falseCnt)));
        Assert.assertEquals(0.25, trueCnt / (trueCnt + falseCnt), 0.01);
    }

    @Test
    public void explorationBonusTest1() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random, true);
        Object bool = learner.select(bools);
        Assert.assertFalse((boolean) bool);
        Object integer = learner.select(ints);
        Assert.assertEquals(4, (int) integer);
        Object str = learner.select(strs);
        Assert.assertEquals("Bob", str.toString());
        learner.update(20);
        TrieBasedMonteCarloLearner.Action firstAction = learner.getRootState().getActions().get(false);
        Assert.assertTrue(firstAction.isHasMetValid());
        Assert.assertEquals(30, firstAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action secondAction = firstAction.getNextState().getActions().get(4);
        Assert.assertTrue(secondAction.isHasMetValid());
        Assert.assertEquals(27, secondAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action thirdAction = secondAction.getNextState().getActions().get("Bob");
        Assert.assertTrue(thirdAction.isHasMetValid());
        Assert.assertEquals(20 + 10 * 0.7 * 0.7, thirdAction.getQ(), 1e-9);
    }

    @Test
    public void explorationBonusTest2() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random, true);
        Object bool = learner.select(bools);
        Assert.assertFalse((boolean) bool);
        Object integer = learner.select(ints);
        Assert.assertEquals(4, (int) integer);
        Object str = learner.select(strs);
        Assert.assertEquals("Bob", str.toString());
        learner.update(0);
        TrieBasedMonteCarloLearner.Action firstAction = learner.getRootState().getActions().get(false);
        Assert.assertFalse(firstAction.isHasMetValid());
        Assert.assertEquals(0, firstAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action secondAction = firstAction.getNextState().getActions().get(4);
        Assert.assertFalse(secondAction.isHasMetValid());
        Assert.assertEquals(0, secondAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action thirdAction = secondAction.getNextState().getActions().get("Bob");
        Assert.assertFalse(thirdAction.isHasMetValid());
        Assert.assertEquals(0, thirdAction.getQ(), 1e-9);
    }

    @Test
    public void explorationBonusTest3() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random, true);
        Object bool = learner.select(bools);
        Assert.assertFalse((boolean) bool);
        Object integer = learner.select(ints);
        Assert.assertEquals(4, (int) integer);
        Object str = learner.select(strs);
        Assert.assertEquals("Bob", str.toString());
        learner.update(-1);
        TrieBasedMonteCarloLearner.Action firstAction = learner.getRootState().getActions().get(false);
        Assert.assertFalse(firstAction.isHasMetValid());
        Assert.assertEquals(-1, firstAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action secondAction = firstAction.getNextState().getActions().get(4);
        Assert.assertFalse(secondAction.isHasMetValid());
        Assert.assertEquals(-1, secondAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action thirdAction = secondAction.getNextState().getActions().get("Bob");
        Assert.assertFalse(thirdAction.isHasMetValid());
        Assert.assertEquals(-1, thirdAction.getQ(), 1e-9);
    }

    @Test
    public void explorationBonusTest4() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random, true);
        Object bool = learner.select(bools);
        Assert.assertFalse((boolean) bool);
        Object integer = learner.select(ints);
        Assert.assertEquals(4, (int) integer);
        Object str = learner.select(strs);
        Assert.assertEquals("Bob", str.toString());
        learner.update(10);
        TrieBasedMonteCarloLearner.Action firstAction = learner.getRootState().getActions().get(false);
        Assert.assertFalse(firstAction.isHasMetValid());
        Assert.assertEquals(10, firstAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action secondAction = firstAction.getNextState().getActions().get(4);
        Assert.assertFalse(secondAction.isHasMetValid());
        Assert.assertEquals(10, secondAction.getQ(), 1e-9);
        TrieBasedMonteCarloLearner.Action thirdAction = secondAction.getNextState().getActions().get("Bob");
        Assert.assertFalse(thirdAction.isHasMetValid());
        Assert.assertEquals(10, thirdAction.getQ(), 1e-9);

        bool = learner.select(bools);
        Assert.assertFalse((boolean) bool);
        integer = learner.select(ints);
        Assert.assertEquals(4, (int) integer);
        str = learner.select(strs);
        Assert.assertEquals("Bob", str.toString());
        learner.update(20);
        firstAction = learner.getRootState().getActions().get(false);
        Assert.assertTrue(firstAction.isHasMetValid());
        Assert.assertEquals(20, firstAction.getQ(), 1e-9);
        secondAction = firstAction.getNextState().getActions().get(4);
        Assert.assertTrue(secondAction.isHasMetValid());
        Assert.assertEquals(37 / 2.0, secondAction.getQ(), 1e-9);
        thirdAction = secondAction.getNextState().getActions().get("Bob");
        Assert.assertTrue(thirdAction.isHasMetValid());
        Assert.assertEquals((30 + 10 * 0.7 * 0.7) / 2, thirdAction.getQ(), 1e-9);
    }

    @Test
    public void selectAndUpdateTest() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random, false);
        Assert.assertFalse(learner.isUseExplorationBonus());
        Assert.assertEquals(learner.getCurrentState().getDepth(), 1);

        Object bool = learner.select(bools);
        Assert.assertFalse((Boolean) bool);
        String stateAfterFirstSelection = learner.getCurrentState().toString();
        Assert.assertEquals(learner.getCurrentState().getPreviousAction().getCurrentState(), learner.getRootState());
        Assert.assertEquals(learner.getCurrentState().getDepth(), 2);

        Object integer = learner.select(ints);
        Assert.assertEquals(4, (int) (Integer) integer);
        Assert.assertEquals(learner.getCurrentState().getPreviousAction().getCurrentState().toString(), stateAfterFirstSelection);
        TrieBasedMonteCarloLearner.State stateAfterSecondSelection = learner.getCurrentState();
        Assert.assertEquals(learner.getCurrentState().getDepth(), 3);

        Object str = learner.select(strs);
        Assert.assertEquals("Bob", str);
        Assert.assertEquals(learner.getCurrentState().getPreviousAction().getCurrentState(), stateAfterSecondSelection);
        Assert.assertEquals(learner.getCurrentState().getDepth(), 4);

        Assert.assertEquals(1, learner.getCurrentState().getPreviousAction().getCurrentState().getActions().size());
        Assert.assertEquals("Bob", learner.getCurrentState().getPreviousAction().getCurrentState().getActions().get("Bob").toString());
        Assert.assertTrue("* | false | 4 | Bob ".equals(learner.getCurrentState().toString()));

        for (TrieBasedMonteCarloLearner.State s = learner.getRootState(); !Objects.equals(s.toString(), "* | false | 4 | Bob "); ) {
            Assert.assertEquals(s.getActions().size(), 1);
            TrieBasedMonteCarloLearner.Action a = (TrieBasedMonteCarloLearner.Action) s.getActions().values().toArray()[0];
            Assert.assertEquals(a.getQ(), 0, 0.0001);
            Assert.assertEquals(a.getCount(), 0);
            s = a.getNextState();
        }

        learner.update(100);
        Assert.assertEquals(learner.getRootState(), learner.getCurrentState());

        for (TrieBasedMonteCarloLearner.State s = learner.getRootState(); !Objects.equals(s.toString(), "* | false | 4 | Bob "); ) {
            Assert.assertEquals(s.getActions().size(), 1);
            TrieBasedMonteCarloLearner.Action a = (TrieBasedMonteCarloLearner.Action) s.getActions().values().toArray()[0];
            Assert.assertEquals(a.getQ(), 100, 0.0001);
            Assert.assertEquals(a.getCount(), 1);
            s = a.getNextState();
        }

        bool = learner.select(bools);
        Assert.assertEquals(false, bool);
        integer = learner.select(ints);
        Assert.assertEquals(4, integer);
        str = learner.select(strs);
        Assert.assertEquals("Bob", str);
        learner.update(0);
        Assert.assertEquals(learner.getRootState(), learner.getCurrentState());
        for (TrieBasedMonteCarloLearner.State s = learner.getRootState(); !Objects.equals(s.toString(), "* | false | 4 | Bob "); ) {
            Assert.assertEquals(s.getActions().size(), 1);
            TrieBasedMonteCarloLearner.Action a = (TrieBasedMonteCarloLearner.Action) s.getActions().values().toArray()[0];
            Assert.assertEquals(a.getQ(), 50, 0.0001);
            Assert.assertEquals(a.getCount(), 2);
            s = a.getNextState();
        }

        bool = learner.select(bools);
        Assert.assertEquals(false, bool);
        integer = learner.select(ints);
        Assert.assertEquals(4, integer);
        str = learner.select(strs);
        Assert.assertEquals("Amy", str);
        learner.update(200);
        TrieBasedMonteCarloLearner.State s;
        for (s = learner.getRootState(); !Objects.equals(s.toString(), "* | false | 4 "); ) {
            Assert.assertEquals(s.getActions().size(), 1);
            TrieBasedMonteCarloLearner.Action a = (TrieBasedMonteCarloLearner.Action) s.getActions().values().toArray()[0];
            Assert.assertEquals(a.getQ(), 100, 0.0001);
            Assert.assertEquals(a.getCount(), 3);
            s = a.getNextState();
        }
        Assert.assertEquals(2, s.getActions().size());
        Assert.assertEquals(200, s.getActions().get("Amy").getQ(), 0.000001);
        Assert.assertEquals(1, s.getActions().get("Amy").getCount());
        Assert.assertEquals(2, s.getActions().get("Bob").getCount());
        Assert.assertEquals(50, s.getActions().get("Bob").getQ(), 0.000001);

        bool = learner.select(bools);
        Assert.assertEquals(false, bool);
        integer = learner.select(ints);
        Assert.assertEquals(4, integer);
        str = learner.select(strs);
        Assert.assertEquals("Amy", str);
        str = learner.select(strs);
        Assert.assertEquals("Charlie", str);
        learner.update(200);
        for (s = learner.getRootState(); !Objects.equals(s.toString(), "* | false | 4 "); ) {
            Assert.assertEquals(s.getActions().size(), 1);
            TrieBasedMonteCarloLearner.Action a = (TrieBasedMonteCarloLearner.Action) s.getActions().values().toArray()[0];
            Assert.assertEquals(a.getQ(), 125, 0.0001);
            Assert.assertEquals(a.getCount(), 4);
            s = a.getNextState();
        }
        Assert.assertEquals(2, s.getActions().size());
        Assert.assertEquals(200, s.getActions().get("Amy").getQ(), 0.000001);
        Assert.assertEquals(2, s.getActions().get("Amy").getCount());
        Assert.assertEquals(2, s.getActions().get("Bob").getCount());
        Assert.assertEquals(50, s.getActions().get("Bob").getQ(), 0.000001);
        s = s.getActions().get("Amy").getNextState();
        Assert.assertEquals("* | false | 4 | Amy | Charlie ", s.getActions().get("Charlie").getNextState().toString());
        Assert.assertEquals(1, s.getActions().get("Charlie").getCount());
        Assert.assertEquals(1, s.getActions().size());
        Assert.assertEquals(200, s.getActions().get("Charlie").getQ(), 1e-8);
        Assert.assertEquals(0, s.getActions().get("Charlie").getNextState().getActions().size());

        bool = learner.select(bools);
        Assert.assertEquals(true, bool);
        integer = learner.select(ints);
        Assert.assertEquals(1, integer);
        learner.update(100);
        Assert.assertEquals(learner.getCurrentState(), learner.getRootState());
        Assert.assertEquals(2, learner.getCurrentState().getActions().size());
        Assert.assertEquals(1, learner.getRootState().getActions().get(true).getCount());
        Assert.assertEquals(100, learner.getRootState().getActions().get(true).getQ(), 1e-8);
        Assert.assertEquals(1, learner.getRootState().getActions().get(true).getNextState().getActions().get(1).getCount());
        Assert.assertEquals(100, learner.getRootState().getActions().get(true).getNextState().getActions().get(1).getQ(), 1e-8);
        Assert.assertEquals(0, learner.getRootState().getActions().get(true).getNextState().getActions().get(1).getNextState().getActions().size());

        bool = learner.select(bools);
        Assert.assertEquals(false, bool);
        learner.update(-100);
        Assert.assertEquals(learner.getCurrentState(), learner.getRootState());
        Assert.assertEquals(5, learner.getCurrentState().getActions().get(false).getCount());
        Assert.assertEquals(80, learner.getCurrentState().getActions().get(false).getQ(), 1e-8);
        s = learner.getRootState().getActions().get(false).getNextState();
        Assert.assertEquals(1, s.getActions().size());
        Assert.assertEquals(4, s.getActions().get(4).getCount());
        Assert.assertEquals(125, s.getActions().get(4).getQ(), 1e-7);
    }
}

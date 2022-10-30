package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class TrieBasedSarsaLearnerTest {
    private static List<Object> bools = Arrays.asList(true, false);
    private static List<Object> ints = Arrays.asList(1, 2, 3, 4);
    private static List<Object> strs = Arrays.asList("Amy", "Bob", "Charlie");
    private static long SEED = 69;
    @Test
    public void test(){
        TrieBasedSarsaLearner learner = new TrieBasedSarsaLearner(1,1,1);
        learner.chooseAction(bools,0);
        learner.chooseAction(ints,0);
        learner.chooseAction(strs,0);
        learner.update(10);
        System.out.println("");
    }
}

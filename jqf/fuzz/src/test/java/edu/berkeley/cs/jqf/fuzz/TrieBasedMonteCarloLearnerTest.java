package edu.berkeley.cs.jqf.fuzz;


import edu.berkeley.cs.jqf.fuzz.rl.TrieBasedMonteCarloLearner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RunWith(JUnit4.class)
public class TrieBasedMonteCarloLearnerTest {
    private static List<Object> bools = Arrays.asList(true, false);
    private static List<Object> ints = Arrays.asList(1, 2, 3, 4);
    private static List<Object> strs = Arrays.asList("Amy", "Bob", "Charlie");
    private static long SEED = 69;

    @Test
    public void test() {
        Random random = new Random();
        random.setSeed(SEED);
        TrieBasedMonteCarloLearner learner = new TrieBasedMonteCarloLearner(0.25, random);
        Object bool = learner.select(bools);
        System.out.println("The guide first selected " + bool + " from " + bools);
        Object integer = learner.select(ints);
        System.out.println("The guide second selected " + integer + " from " + ints);
        Object str = learner.select(strs);
        System.out.println("The guide third selected " + str + " from " + strs);
        learner.update(100);

        bool = learner.select(bools);
        System.out.println("The guide first selected " + bool + " from " + bools);
        integer = learner.select(ints);
        System.out.println("The guide second selected " + integer + " from " + ints);
        str = learner.select(strs);
        System.out.println("The guide third selected " + str + " from " + strs);
        learner.update(0);

        bool = learner.select(bools);
        System.out.println("The guide first selected " + bool + " from " + bools);
        integer = learner.select(ints);
        System.out.println("The guide second selected " + integer + " from " + ints);
        str = learner.select(strs);
        System.out.println("The guide third selected " + str + " from " + strs);
        learner.update(200);

        bool = learner.select(bools);
        System.out.println("The guide first selected " + bool + " from " + bools);
        integer = learner.select(ints);
        System.out.println("The guide second selected " + integer + " from " + ints);
        str = learner.select(strs);
        System.out.println("The guide third selected " + str + " from " + strs);
        learner.update(200);


        System.out.println("");
    }
}

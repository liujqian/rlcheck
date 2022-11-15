package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import java.util.*;

public class ArrayBasedSarsaLearner {
    Random random;
    double alpha;
    double gamma;
    final String BEGIN_STATE = "BEGIN";
    String[] currentState;

    public ArrayBasedSarsaLearner(
            double episilon,
            double alpha,
            double gamma,
            Random random,
            int stateSize
    ) {
        if (random == null) {
            random = new Random();
        }
        this.alpha = alpha;
        this.gamma = gamma;
        this.random = random;
        assert stateSize > 0;
        this.currentState = new String[stateSize];
        this.currentState[0] = BEGIN_STATE;
    }

    public Object select(List<Object> options) {
        return this.chooseAction(options, null);
    }

    public Object chooseAction(List<Object> options, Integer forceSelectIndex) {
        return null;
    }

    public void update(double r) {

    }

    /* Update and return new state. Removes items if too long */
    private void updateState(String[] stateArr, String stateX) {
        int stateSize = this.currentState.length;
        int end = stateSize - 1;
        this.currentState[end] = stateX;
        System.arraycopy(stateArr, 1, this.currentState, 0, end);
    }
}

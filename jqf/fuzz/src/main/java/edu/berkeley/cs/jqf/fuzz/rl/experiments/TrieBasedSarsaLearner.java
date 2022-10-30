package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrieBasedSarsaLearner {
    State rootState;
    State currentState;
    double alpha;

    double gamma;

    public TrieBasedSarsaLearner(double episilon, double alpha, double gamma) {
        this.rootState = new TrieBasedSarsaLearner.State(null, episilon);
        this.currentState = rootState;
        this.alpha = alpha;
        this.gamma = gamma;
    }

    public Object chooseAction(List<Object> options) {
        return this.chooseAction(options, null);
    }

    public Object chooseAction(List<Object> options, Integer forceSelectIndex) {
        if (currentState == rootState) {
            Object selected = currentState.select(options, forceSelectIndex);
        }
        Action a = currentState.selectedAction;
        State sapostrophy = a.nextState;
        Action aapostrophy = sapostrophy.select(options, forceSelectIndex);
        double newQForCurSA = a.getQ() + this.alpha * (0 + gamma * aapostrophy.getQ() - a.getQ());
        a.updateQ(newQForCurSA);
        this.currentState = sapostrophy;
        return aapostrophy.action;
    }

    public void update(double r) {
        this.currentState.selectedAction = null;
        Action actionLedToFinalState = this.currentState.previousAction;
        actionLedToFinalState.updateQ(actionLedToFinalState.getOldQ() + alpha * (r + 0 - actionLedToFinalState.getOldQ()));
        this.currentState = rootState; // reset
    }

    static class Action {
        private double Q;
        private double oldQ;
        private TrieBasedSarsaLearner.State nextState;
        private TrieBasedSarsaLearner.State currentState;
        private Object action;

        public void updateQ(double newQ) {
            oldQ = Q;
            Q = newQ;
        }

        public double getOldQ() {
            return oldQ;
        }

        public Action(Object action, double episilon, TrieBasedSarsaLearner.State currentState, Random random) {
            this.Q = 0;
            this.action = action;
            this.nextState = new TrieBasedSarsaLearner.State(this, episilon, random);
            this.currentState = currentState;
        }

        public double getQ() {
            return Q;
        }
    }

    public static class State {
        private double episilon;
        private Map<Object, Action> actions;
        private Random random;
        private final Action previousAction; // if I am a root state, this field is null.
        private Action selectedAction;

        public State(Action previousAction, double episilon) {
            this.previousAction = previousAction;
            this.actions = new HashMap<>();
            this.episilon = episilon;
            this.random = new Random();
        }

        public State(Action action, double episilon, Random random) {
            this(action, episilon);
            this.random = random;
        }

        public Action select(List<Object> options, Integer forceSelectIndex) {
            Object choice = null;
            if (forceSelectIndex != null) {
                choice = options.get(forceSelectIndex);
            }
            if (random.nextDouble() < this.episilon || this.actions.isEmpty()) { // choose random
                int index = ((int) (random.nextDouble() * 1000.0)) % options.size();
                choice = choice == null ? options.get(index) : choice;
                if (this.actions.containsKey(choice)) {
                    this.selectedAction = this.actions.get(choice);
                } else {
                    Action newAction = new Action(choice, episilon, this, this.random);
                    this.actions.put(choice, newAction);
                    this.selectedAction = newAction;
                }
            } else {
                this.selectedAction = this.actions.values().stream().reduce((a, b) -> a.getQ() > b.getQ() ? a : b).get();
            }
            return this.selectedAction;
        }
    }
}

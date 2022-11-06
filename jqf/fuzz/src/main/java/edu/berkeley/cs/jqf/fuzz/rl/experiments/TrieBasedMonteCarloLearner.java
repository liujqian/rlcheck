package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrieBasedMonteCarloLearner {
    private State rootState;
    private State currentState;
    private boolean useExplorationBonus;

    public TrieBasedMonteCarloLearner(double episilon, boolean useExplorationBonus) {
        this.useExplorationBonus = useExplorationBonus;
        this.rootState = new State(null, episilon, 1);
        this.currentState = rootState;
    }

    public TrieBasedMonteCarloLearner(double episilon, Random random, boolean useExplorationBonus) {
        this.useExplorationBonus = useExplorationBonus;
        this.rootState = new State(null, episilon, random, 1);
        this.currentState = rootState;
    }

    public Object select(List<Object> choices) {
        this.currentState = currentState.select(choices);
        return this.currentState.getPreviousAction().getAction();
    }

    public void update(int r) {
        boolean metNewValid = r == 20;
        while (currentState.getPreviousAction() != null) {
            Action previousAction = currentState.getPreviousAction();

            double updateVal = r;
            if (useExplorationBonus && metNewValid && !previousAction.hasMetValid) {
                previousAction.hasMetValid = true;
                updateVal = updateVal + 10 * Math.pow(0.7, previousAction.getCurrentState().depth);
            }

            previousAction.update(updateVal);
            currentState = previousAction.getCurrentState();
        }
    }

    private void resetCurrentStateToRoot() {
        this.currentState = rootState;
    }

    static class Action {
        private int count;
        private double Q;
        private State nextState;
        private State currentState;
        private Object action;

        private boolean hasMetValid;

        public Action(Object action, double episilon, State currentState, Random random) {
            this.count = 0;
            this.Q = 0;
            this.action = action;
            this.nextState = new State(this, episilon, random, currentState.depth + 1);
            this.currentState = currentState;
            this.hasMetValid =false;
        }

        public State getNextState() {
            return nextState;
        }

        public double getQ() {
            return Q;
        }

        public State getCurrentState() {
            return currentState;
        }

        public Object getAction() {
            return action;
        }

        @Override
        public String toString() {
            return action.toString();
        }

        public void update(double r) {
            this.count += 1;
            this.Q = this.Q + (1.0 / this.count) * (r - this.Q);
        }
    }

    public static class State {
        private double episilon;
        private int depth;
        private Map<Object, Action> actions;
        private Random random;
        private final Action previousAction; // if I am a root state, this field is null.

        public State(Action previousAction, double episilon, int depth) {
            this.previousAction = previousAction;
            this.actions = new HashMap<>();
            this.episilon = episilon;
            this.random = new Random();
            this.depth = depth;
        }

        public State(Action action, double episilon, Random random, int depth) {
            this(action, episilon, depth);
            this.random = random;
        }

        @Override
        public String toString() {
            return previousAction == null ? "* " : previousAction.getCurrentState().toString() + "| " + previousAction + " ";
        }

        public State select(List<Object> options) {
            if (options.isEmpty()) {
                System.err.println("An empty options list is passed into the select method!");
                System.exit(1);
            }
            if (random.nextDouble() < this.episilon || this.actions.isEmpty()) {
                int index = ((int) (random.nextDouble() * 1000.0)) % options.size();
                Object choice = options.get(index);
                if (this.actions.containsKey(choice)) {
                    return this.actions.get(choice).getNextState();
                } else {
                    Action newAction = new Action(choice, episilon, this, random);
                    this.actions.put(choice, newAction);
                    return newAction.getNextState();
                }
            } else {
                Action curMaxAction = this.actions.values().stream().reduce((a, b) -> a.getQ() > b.getQ() ? a : b).get();
                if (!curMaxAction.getAction().getClass().equals(options.get(0).getClass())) {
                    System.err.println("The passed in options have a different type from the selected action: " + curMaxAction.getAction().getClass().toString() + " " + curMaxAction.getAction().getClass().toString());
                    System.exit(1);
                }
                return curMaxAction.getNextState();
            }
        }

        public Action getPreviousAction() {
            return previousAction;
        }
    }
}


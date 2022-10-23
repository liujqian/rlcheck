package edu.berkeley.cs.jqf.fuzz.rl;

import java.util.*;
import java.util.function.BinaryOperator;

public class TrieBasedMonteCarloLearner {
    private State rootState;
    private State currentState;

    public TrieBasedMonteCarloLearner(double episilon) {
        this.rootState = new State(null, episilon);
        this.currentState = rootState;
    }

    public State getCurrentState() {
        return currentState;
    }

    public TrieBasedMonteCarloLearner(double episilon, Random random) {
        this.rootState = new State(null, episilon, random);
        this.currentState = rootState;
    }

    public Object select(List<Object> choices) {
        this.currentState = currentState.select(choices);
        return this.currentState.getPreviousAction().getAction();
    }

    public void update(int r) {
        while (currentState.getPreviousAction() != null) {
            Action previousAction = currentState.getPreviousAction();
            previousAction.update(r);
            currentState = previousAction.getCurrentState();
        }
    }

    private void resetCurrentStateToRoot() {
        this.currentState = rootState;
    }

}

class State {
    private double episilon;
    private Map<Object, Action> actions;
    //    private List<Object> highestValuedActions;
    private Random random;
    //    private double curMaxQ;
    private final Action previousAction; // if I am a root state, this field is null.

    public State(Action previousAction, double episilon) {
        this.previousAction = previousAction;
        this.actions = new HashMap<>();
//        this.highestValuedActions = new ArrayList<>();
        this.episilon = episilon;
//        this.curMaxQ = 0;
        this.random = new Random();
    }

    public State(Action action, double episilon, Random random) {
        this(action, episilon);
        this.random = random;
    }

    @Override
    public String toString() {
        return previousAction == null ? "* " : previousAction.getCurrentState().toString() + "| " + previousAction + " ";
    }

    public State select(List<Object> options) {
        if (random.nextDouble() < this.episilon || this.actions.isEmpty() /*|| highestValuedActions.size() == 0*/) {
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
            return curMaxAction.getNextState();
        }
    }

    public Action getPreviousAction() {
        return previousAction;
    }

//    public double getCurMaxQ() {
//        return curMaxQ;
//    }
//
//    public void setCurMaxQ(double curMaxQ) {
//        this.curMaxQ = curMaxQ;
//    }
//
//    public void setHighestValuedActions(List highestValuedActions) {
//        this.highestValuedActions = highestValuedActions;
//    }
//
//    public List getHighestValuedActions() {
//        return highestValuedActions;
//    }
}

class Action {
    private int count;
    private double Q;
    private State nextState;
    private State currentState;
    private Object action;

    public Action(Object action, double episilon, State currentState, Random random) {
        this.count = 0;
        this.Q = 0;
        this.action = action;
        this.nextState = new State(this, episilon, random);
        this.currentState = currentState;
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

    public void update(int r) {
        this.count += 1;
        this.Q = this.Q + (1.0 / this.count) * (r - this.Q);
//        double currentStateMaxQ = this.currentState.getCurMaxQ();
//        if (this.Q >= currentStateMaxQ) {
//            if (this.Q > currentStateMaxQ) {
//                this.currentState.setHighestValuedActions(new ArrayList<>());
//                this.currentState.setCurMaxQ(this.Q);
//            }
//            this.currentState.getHighestValuedActions().add(this.action);
//        }
    }
}
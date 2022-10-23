package edu.berkeley.cs.jqf.fuzz.rl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrieBasedMonteCarloLearner {
    private State<?> rootState;
    private State currentState;

    public TrieBasedMonteCarloLearner() {
        this.rootState = new State<>(null);
        this.currentState = rootState;
    }

    public <T> T select(List<T> choices) {
        Object chosenAction = currentState.select(choices);
        return (T) chosenAction;
    }

    public void update(int r) {
        // TODO: Implement this method
    }

    private void resetCurrentStateToRoot() {
        this.currentState = rootState;
    }

}

class State<T> {
    private Map<T, Action<T>> actions;
    private final Action<?> previousAction; // if I am a root state, this field is null.

    public State(Action<?> previousAction) {
        this.previousAction = previousAction;
    }

    @Override
    public String toString() {
        return previousAction == null ? " " : previousAction.getCurrentState().toString() + "| " + previousAction + " ";
    }

    public T select(List<T> options) {
        return null; // TODO: Implement this method.
    }
}

class Action<T> {
    private final T action;
    private int count;
    private double Q;

    private State<?> nextState;

    private State<T> currentState;

    public Action(T action) {
        this.action = action;
        this.count = 0;
        this.Q = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;
        Action<?> action1 = (Action<?>) o;
        return action.equals(action1.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action);
    }

    @Override
    public String toString() {
        return action.toString();
    }

    public State<T> getCurrentState() {
        return currentState;
    }
}
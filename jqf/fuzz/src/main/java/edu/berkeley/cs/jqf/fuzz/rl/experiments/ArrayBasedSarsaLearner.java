package edu.berkeley.cs.jqf.fuzz.rl.experiments;

import java.util.*;

public class ArrayBasedSarsaLearner {
    Random random;
    double alpha;
    double gamma;
    final String BEGIN_ACTION = "BEGIN";
    String[] currentState;

    HashMap<
            String, // states
            Map<
                    Object, // actions
                    Double[] // Q-value, count pairs
                    >
            > qTable;
    final int OLD_Q_INDEX = 1;
    final int NEW_Q_INDEX = 0;
    double episilon;
    List<AbstractMap.SimpleEntry<String, Object>> episode;

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
        this.episilon = episilon;
        this.alpha = alpha;
        this.gamma = gamma;
        this.random = random;
        assert stateSize > 0;

        this.currentState = new String[stateSize];

        qTable = new HashMap<>();
        Map<Object, Double[]> initialStateEntry = new HashMap<>();
        initialStateEntry.put(BEGIN_ACTION, new Double[]{0.0, 0.0});
        qTable.put("", initialStateEntry);

        episode = new ArrayList<>();
        episode.add(new AbstractMap.SimpleEntry<>("", BEGIN_ACTION));
    }

    public Object select(List<Object> options) {
        return this.chooseAction(options, null);
    }

    private Object chooseAction(List<Object> options, Integer forceSelectIndex) {
        Object a = episode.get(episode.size() - 1).getValue();
        String[] sapostrophy = updateState(currentState, a.toString());

        Object aapostrophy = selectActionByState(options, sapostrophy);
        double newQForCurSA = getQ(stateToString(currentState), a) +
                this.alpha * (0 + gamma * getQ(stateToString(sapostrophy), aapostrophy) - getQ(stateToString(currentState), a));
        updateQ(stateToString(currentState), a, newQForCurSA);
        currentState = sapostrophy;
        return aapostrophy;
    }

    protected double getQ(String state, Object a) {
        return qTable.get((state)).get(a)[NEW_Q_INDEX];
    }

    private void updateQ(String state, Object a, double newQ) {
        qTable.get((state)).get(a)[OLD_Q_INDEX] = qTable.get((state)).get(a)[NEW_Q_INDEX];
        qTable.get((state)).get(a)[NEW_Q_INDEX] = newQ;
    }

    private Object selectActionByState(List<Object> options, String[] state) {
        boolean curStateNoActionRecord = qTable.get(stateToString(state)).keySet().size() == 0;
        boolean lessThanepisilon = this.random.nextDouble() < episilon;
        Object selected;
        if (curStateNoActionRecord || lessThanepisilon) {
            int randomIndex = this.random.nextInt(Integer.MAX_VALUE) % options.size();
            selected = options.get(randomIndex);
            if (!qTable.get(stateToString(state)).containsKey(selected)) {
                qTable.get(stateToString(state)).put(selected, new Double[]{0.0, 0.0});
            }
        } else {
            selected = qTable.get(stateToString(state)).
                    keySet().
                    stream().
                    reduce(
                            (a, b) ->
                                    qTable.get(stateToString(state)).get(a)[NEW_Q_INDEX] > qTable.get(stateToString(state)).get(b)[NEW_Q_INDEX] ? a : b)
                    .get();
        }
        episode.add(new AbstractMap.SimpleEntry<>(stateToString(state), selected));
        return selected;
    }


    public void update(double r) {
        Object actionToFinalState = episode.get(episode.size() - 1).getValue();
        String secondLastState = episode.get(episode.size() - 1).getKey();
        double q = qTable.get((secondLastState)).get(actionToFinalState)[NEW_Q_INDEX];
        double updatedQ = q + alpha * (r + 0 - q);
        updateQ(secondLastState, actionToFinalState, updatedQ);
        episode.clear();
        episode.add(new AbstractMap.SimpleEntry<>("", BEGIN_ACTION));
        currentState = new String[currentState.length];
    }

    /* Update and return new state. Removes items if too long */
    private String[] updateState(String[] stateArr, String stateX) {
        int stateSize = currentState.length;
        String[] newState = new String[stateSize];
        int end = stateSize - 1;
        newState[end] = stateX;
        System.arraycopy(stateArr, 1, newState, 0, end);
        if (!qTable.containsKey(stateToString(newState))) {
            qTable.put(stateToString(newState), new HashMap<>());
        }
        return newState;
    }

    protected String stateToString(String[] stateArr) {
        String state = "";
        String stateDelim = " | ";
        for (int i = 0; i < stateArr.length; i++) {
            String s = stateArr[i];
            if (s != null) {
                if (i != stateArr.length - 1) {
                    s += stateDelim;
                }
                state += s;
            }
        }
        return state;
    }
}

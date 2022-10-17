package edu.berkeley.cs.jqf.examples.str;

import edu.berkeley.cs.jqf.fuzz.rl.RLGenerator;
import edu.berkeley.cs.jqf.fuzz.rl.RLGuide;
import edu.berkeley.cs.jqf.fuzz.rl.RLParams;

import java.util.Arrays;
import java.util.List;

public class SimpleStringGenerator implements RLGenerator {
    private RLGuide guide;
    private final int MAX_STR_LENGTH = 64;
    private final List<Object> alphbet = Arrays.asList("abcdefghijklmnopqrstuvwxyz123456789".toCharArray());
    private int hasNextLearnerID;
    private int charPickingLearnerID;
    private int stateSize;

    public SimpleStringGenerator() {
    }

    @Override
    public void init(RLParams params) {
        System.out.println("The init method of SimpleStrGenerator is called!");
        if (params.exists("seed")) {
            guide = new RLGuide((long) params.get("seed"));
        } else {
            guide = new RLGuide();
        }
        double e = (double) params.get("defaultEpsilon", true);
        this.stateSize = (int) params.get("stateSize", true);
        this.hasNextLearnerID = guide.addLearner(Arrays.asList(new boolean[]{true, false}), e);
        this.charPickingLearnerID = guide.addLearner(alphbet, e);
    }

    @Override
    public String generate() {
        String[] states = new String[stateSize];
        StringBuilder res = new StringBuilder();
        while (res.length() < MAX_STR_LENGTH && (Boolean) guide.select(states, hasNextLearnerID)) {
            String[] updatedStates = updateState(states, "hasNext=true");
            Character picked = (Character) guide.select(updatedStates, charPickingLearnerID);
            states = updateState(updatedStates, "pickedChar=" + picked.toString());
            res.append(picked);
        }
        return res.toString();
    }

    @Override
    public void update(int r) {
        this.guide.update(r);
    }

    private String[] updateState(String[] stateArr, String stateX) {
        String[] newState = new String[stateSize];
        int end = stateSize - 1;
        newState[end] = stateX;
        System.arraycopy(stateArr, 1, newState, 0, end);
        return newState;
    }
}
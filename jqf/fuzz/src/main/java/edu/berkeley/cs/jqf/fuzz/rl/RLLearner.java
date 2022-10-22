package edu.berkeley.cs.jqf.fuzz.rl;

public interface RLLearner {
    void update(int reward);
    Object select(String state);
}

package edu.berkeley.cs.jqf.examples.str;

public class SimpleString {
    private final String str;
    public SimpleString(String str){
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}

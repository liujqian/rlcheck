package edu.berkeley.cs.jqf.examples.str;

import java.io.Console;

public class Playground {
    public static void main(String[] args) throws InterruptedException {
        Console console = System.console();
        console.printf("Nihao");
        Thread.sleep(1000);
        console.printf("\033[2J");
        console.printf("\033[H");
        Thread.sleep(1000);
        console.printf("Nihao again");
    }
}

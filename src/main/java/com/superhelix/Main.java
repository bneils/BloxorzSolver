package com.superhelix;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        Level level;
        try {
            level = LevelUtils.loadFromFile("levels/level1.txt");
        } catch (FileNotFoundException | LevelParserException e) {
            System.out.println("error: " + e.getMessage());
            level = null; // quiet error
            e.printStackTrace();
            System.exit(1);
        }
        //down,right,right,right,right,right,right,down
        System.out.println(StateGraph.generateMinimalMovePattern(level));
    }
}
package com.superhelix;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("error: insufficient arguments");
            return;
        }

        Level level;
        try {
            level = LevelUtils.loadFromFile(args[1]);
        } catch (FileNotFoundException | LevelParserException e) {
            System.out.println("error: " + e.getMessage());
            level = null; // quiet error
            e.printStackTrace();
            System.exit(1);
        }

        ArrayList<String> moves = (ArrayList<String>) StateGraph.generateMinimalMovePattern(level);
        for (int i = 0; i < moves.size(); ++i) {
            System.out.printf("%3d. %s\n", i, moves.get(i));
        }
    }
}
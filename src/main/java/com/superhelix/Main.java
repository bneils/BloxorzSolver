package com.superhelix;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: prog <level_path> [level_info_path]");
            return;
        }
        String levelFilename = args[0];
        String infoFilename = (args.length == 2) ? args[1] : null;

        Level level;
        try {
            level = LevelUtils.loadFromFile(levelFilename, infoFilename);
        } catch (IOException | LevelParserException e) {
            System.out.println("error: " + e.getMessage());
            level = null; // quiet error
            //e.printStackTrace();
            System.exit(1);
        }

        ArrayList<String> moves = (ArrayList<String>) StateGraph.generateMinimalMovePattern(level);
        if (moves.size() > 0) {
            System.out.println("Here's the solution to: " + args[0]);
            for (int i = 0; i < moves.size();) {
                int j = i + 1;
                while (j < moves.size() && moves.get(i).equals(moves.get(j)))
                    ++j;
                int count = j - i;
                if (count != 1)
                    System.out.printf("%3d. %s x %d\n", i + 1, moves.get(i), j - i);
                else
                    System.out.printf("%3d. %s\n", i + 1, moves.get(i));
                i = j;
            }
        } else {
            System.out.println("Sorry, I can't find a solution to that. Have you rechecked the tile matrix and info file?");
        }
    }
}
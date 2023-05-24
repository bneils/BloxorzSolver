package com.superhelix;

import java.util.*;
import org.javatuples.Pair;

public class StateGraph {
    /**
     * Generates a minimal player move pattern from a Level.
     * @param level The level to solve
     * @return The keys to be pressed to complete the level.
     */
    public static String generateMinimalMovePattern(Level level) {
        // visitedBy will hold a mapping from the StateNode ID to StateNode parent
        HashSet<String> visited = new HashSet<>();
        Queue<StateNode> workQueue = new LinkedList<>();

        // Gather all the starting bridge states and use it for the first state node
        Map<Character, Boolean> bridgeStates = new TreeMap<>();
        for (Map.Entry<Character, TileMetadata> entry : level.tilesMetadata().entrySet())
            if (entry.getValue().isBridge())
                bridgeStates.put(entry.getKey(), entry.getValue().getStartingBridgeState());

        StateNode startingNode = new StateNode(new Player(level.playerPos(), level.playerPos()),
                null, "", bridgeStates);

        workQueue.add(startingNode);
        StateNode backtrackingNode = null;

        while (!workQueue.isEmpty()) {
            StateNode node = workQueue.remove();

            // Skip if we've already visited this node (property of BFS)
            if (visited.contains(node.getIdentifier()))
                continue;
            visited.add(node.getIdentifier());

            // Have we reached the goal
            if (node.getPlayer().isVertical() && node.getPlayer().getFirst().equals(level.goalPos())) {
                backtrackingNode = node;
                break;
            }

            // Last thing is to add the children to the queue
            workQueue.addAll(node.generateChildren(level));
        }

        if (backtrackingNode == null)
            return "";

        // Assume the goal was found
        StringBuilder movePattern = new StringBuilder();
        while (backtrackingNode.getParent() != null) {
            movePattern.insert(0, backtrackingNode.getMoveDescription() + "\n");
            backtrackingNode = backtrackingNode.getParent();
        }

        return movePattern.toString();
    }
}

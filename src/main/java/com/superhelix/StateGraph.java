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
        HashMap<String, StateNode> visitedBy = new HashMap<>();
        Queue<StateNode> workQueue = new LinkedList<>();

        // Gather all the starting bridge states and use it for the first state node
        Map<Character, Boolean> bridgeStates = new TreeMap<>();
        for (Map.Entry<Character, TileMetadata> entry : level.tilesMetadata().entrySet())
            bridgeStates.put(entry.getKey(), entry.getValue().getStartingBridgeState());

        StateNode startingNode = new StateNode(
                0, level.playerPos(), level.playerPos(), null, "", bridgeStates);

        workQueue.add(startingNode);
        StateNode backtrackingNode = null;

        while (!workQueue.isEmpty()) {
            StateNode node = workQueue.remove();

            // Skip if we've already visited this node (property of BFS)
            if (visitedBy.containsKey(node.getIdentifier()))
                continue;

            // If not, this vertex belongs to the first vertex that put it here
            visitedBy.put(node.getIdentifier(), node.getParent());

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
        while (true) {
            StateNode parent = visitedBy.get(backtrackingNode.getIdentifier());
            if (parent == null)
                break;
            movePattern.insert(0, backtrackingNode.getMoveDescription() + "\n");
            backtrackingNode = parent;
        }

        return movePattern.toString();
    }
}

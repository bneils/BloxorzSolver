package com.superhelix;

import java.util.*;

public class StateGraph {
    /**
     * Generates a minimal player move pattern from a Level.
     * @param level The level to solve
     * @return The keys to be pressed to complete the level.
     */
    public static List<String> generateMinimalMovePattern(Level level) {
        // visitedBy will hold a mapping from the StateNode ID to StateNode parent
        HashSet<String> visited = new HashSet<>();
        Queue<StateNode> workQueue = new LinkedList<>();

        // Gather all the starting bridge states and use it for the first state node
        Map<Character, Boolean> states = new TreeMap<>();
        for (Map.Entry<Character, TileMetadata> entry : level.tilesMetadata().entrySet())
            states.put(entry.getKey(), entry.getValue().getStartingBridgeState());
        Position firstPlayerPos = level.tilesMetadata().get('$').getPositions().get(0);
        Position goalPos = level.tilesMetadata().get('^').getPositions().get(0);
        StateNode startingNode = new StateNode(new Player(firstPlayerPos, firstPlayerPos),
                null, "", states);

        workQueue.add(startingNode);
        StateNode backtrackingNode = null;

        while (!workQueue.isEmpty()) {
            StateNode node = workQueue.remove();

            // Skip if we've already visited this node (property of BFS)
            if (visited.contains(node.getIdentifier()))
                continue;
            visited.add(node.getIdentifier());

            // Have we reached the goal
            if (node.getPlayer().isVertical() && node.getPlayer().getFirst().equals(goalPos)) {
                backtrackingNode = node;
                break;
            }

            // Last thing is to add the children to the queue
            workQueue.addAll(node.generateChildren(level));
        }

        if (backtrackingNode == null)
            return new ArrayList<>();

        // Assume the goal was found
        List<String> movePattern = new ArrayList<>();
        while (backtrackingNode.getParent() != null) {
            movePattern.add(0, backtrackingNode.getMoveDescription());
            backtrackingNode = backtrackingNode.getParent();
        }

        return movePattern;
    }
}

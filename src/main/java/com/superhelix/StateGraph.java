package com.superhelix;

import java.util.*;
import org.javatuples.Pair;

public class StateGraph {
    Level level;
    HashMap<String, StateNode> visitedBy;

    List<Pair<StateNode, Pair<StateNode, String>>> queue;

    public StateGraph(Level level) {
        this.level = level;

        StateNode startingNode = new StateNode(
                0, level.playerPos().x(), level.playerPos().y(),
                level.playerPos().x(), level.playerPos().y(), level.bridgeInitialStatuses(), null);
        visitedBy = new HashMap<>();
        queue = new LinkedList<>();
        queue.addFirst(new Pair<>(null, new Pair<>(startingNode, "")));
    }

    public String generateMinimalMovePattern() {
        Pair<StateNode, String> goalNode = null;

        while (!queue.isEmpty()) {
            Pair<StateNode, Pair<StateNode, String>> edge = queue.removeLast();

            // We've already been to this node from another node
            if (visitedBy.containsKey(edge.getValue1().getValue0().getIdentifier()))
                continue;

            // If not, this vertex belongs to the first vertex that put it here
            visitedBy.put(edge.getValue1().getValue0().getIdentifier(), edge.getValue0());

            // Test if we've reached the goal
            int[] playerPos = edge.getValue1().getValue0().getPlayerPosition();
            if (playerPos[0] == level.goalPos.getValue0() &&
                playerPos[2] == level.goalPos.getValue0() &&
                playerPos[1] == level.goalPos.getValue1() &&
                playerPos[3] == level.goalPos.getValue1()) {
                goalNode = edge.getValue1().getValue0();
                break;
            }

            // Last thing is to add the children to the queue
            for (Pair<StateNode, String> childStateMove : edge.getValue1().generateChildren(level)) {
                queue.addFirst(new Pair<>(edge.getValue1(), childStateMove));
            }
        }

        // Assume the goal was found
        StateNode node = goalNode;
        StringBuilder moves = new StringBuilder();
        while (true) {
            StateNode parent = visitedBy.get(node.getIdentifier());
            if (parent == null)
                break;

            node = parent;
        }

        return "";
    }
}

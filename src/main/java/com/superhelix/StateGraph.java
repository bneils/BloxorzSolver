package com.superhelix;

import java.util.*;
import org.javatuples.Pair;

public class StateGraph {
    Level level;
    HashMap<String, StateNode> visitedBy;

    LinkedList<Pair<StateNode, StateNode>> queue;

    public StateGraph(Level level) {
        this.level = level;

        StateNode startingNode = new StateNode(
                0, level.playerPos.getValue0(), level.playerPos.getValue1(),
                level.playerPos.getValue0(), level.playerPos.getValue1(), level.bridgeInitialStatuses, null);
        visitedBy = new HashMap<>();
        queue = new LinkedList<>();
        queue.addFirst(new Pair<>(null, startingNode));
    }

    public String generateMinimalMovePattern() {
        StateNode goalNode = null;

        while (!queue.isEmpty()) {
            Pair<StateNode, StateNode> edge = queue.removeLast();

            // We've already been to this node from another node
            if (visitedBy.containsKey(edge.getValue1().getIdentifier()))
                continue;

            // If not, this vertex belongs to the first vertex that put it here
            visitedBy.put(edge.getValue1().getIdentifier(), edge.getValue0());

            // Test if we've reached the goal
            int[] playerPos = edge.getValue1().getPlayerPosition();
            if (playerPos[0] == level.goalPos.getValue0() &&
                playerPos[2] == level.goalPos.getValue0() &&
                playerPos[1] == level.goalPos.getValue1() &&
                playerPos[3] == level.goalPos.getValue1()) {
                goalNode = edge.getValue1();
                break;
            }

            // Last thing is to add the children to the queue
            for (StateNode childState : edge.getValue1().generateChildren(level)) {
                queue.addFirst(new Pair<>(edge.getValue1(), childState));
            }
        }

        // Assume the goal was found
        StateNode node = goalNode;
        while (node != null) {
            System.out.println(node.getIdentifier());
            node = visitedBy.get(node.getIdentifier());
        }

        return "";
    }
}

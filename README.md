# BloxorzSolver
This program was inspired by a Jerma clip at 12 AM where he was playing this.
It reminded me of playing it in elementary school on https://coolmathgames.com.
When I revisited the game, I thought of the problem of finding a minimal move pattern to
complete a level. It seems hard at first, but if you frame the problem as a massive graph of states,
then you can apply graph traversal algorithms like Breadth First Search (BFS) to it.

## How is it a graph?
It can become a graph if you make every node in the graph a possible state.
Each state would be the player's position(s), orientation, and the state of all bridges.
As you can imagine, there are a ridiculous number of states a level can be in, but it is
nonetheless possible for a computer to try every state as a human might.
The edges between nodes (states) are formed when the player performs a move.
For example, if a player moves onto a switch, the next node has a different position and
a different level state (with the activated bridge).

## In layman terms
Imagine you are a 200 IQ genius who can look at every possibly branching move simultaneously.
If you could, it would be trivial to draw a line between them all that solved the level.
That's what this program does.

## Optimizations from this approach
Since it is being conceptualized as a graph, each node can have 1 incoming edge and multiple outgoing edges.
Despite there being multiple ways to reach a state, each node will be marked visited as soon as the first state reaches
it. Therefore, the next unvisited state will be visited in the least number of moves. Once the graph has been completely formed, any path from
A to C through B also contains the shortest path from A to B and B to C.

## A worse approach to this
Another much worse approach to solving this would be to generate Cartesian products of the string "WASD",
until you reach the goal. The glaring problem with this is that you:
- Create a lot of invalid moves that fall off the platform
- Create cycles that revisit states you've already been to
- Take the same path more than once since you can't be sure what's the best.
- Have no idea how many patterns to make 

Also, the asymptotic complexity of this is abysmal: O(4^m) where m is the minimal number of moves.

## Breadth-first search
Since breadth means width, we prioritize nodes that are closest to us before we explore other ones.
This is why explored nodes are on the shortest path. With depth-first search, that is not guaranteed because our order
of exploration is arbitrary and prioritizes depth. The main difference between the two is that BFS uses a queue (FIFO)
while DFS uses a stack (FILO).

## Why Java?
I haven't used it for any projects and I think I should get familiar with it.
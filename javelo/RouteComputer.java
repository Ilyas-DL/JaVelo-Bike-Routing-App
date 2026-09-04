package ch.epfl.javelo.routing;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class contains the algorithm that finds the shortest point between two points using A* algorithm.
 * @author Eden Kahane (346481).
 */
public final class RouteComputer {
  private final static int LENGTH_NODE_BITS = 28;
  private final static int LENGTH_EDGEID_BITS = 4;
  private final Graph graph;
  private final CostFunction costFunction;

  /**
   * Public constructor for RouteComputer that allows to find the shortest point between two points
   * in a Graph using a CostFunction to modify the weight depending on the parameters we want.
   * @param graph a graph containing nodes and edges.
   * @param costFunction a function to modify the cost of each edge depending on the characteristics of the edge.
   */
  public RouteComputer(Graph graph, CostFunction costFunction) {
    this.graph = graph;
    this.costFunction = costFunction;
  }

  /**
   * Finds the best path between two nodes using A* implementation.
   * @param startNodeId the nodeId of the starting node.
   * @param endNodeId the nodeId of the end node.
   * @return a SingleRoute with all the edges linking these two paths.
   */
  public Route bestRouteBetween(int startNodeId, int endNodeId) {
    Preconditions.checkArgument(startNodeId != endNodeId);
    PointCh endPointCh = graph.nodePoint(endNodeId);

    record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {
        @Override
        public int compareTo(WeightedNode that) {
          return Float.compare(this.distance, that.distance);
        }
    }
    float[] distance = new float[graph.nodeCount()]; //Actual found distance to a node.

    //predecessor, each node will know the previousNode with the
    //shortest path to it, first 4 bits for the edgeIndex, next 28 bits for nodeId.
    int[] predecessor = new int[graph.nodeCount()];
    PriorityQueue<WeightedNode> inExploration = new PriorityQueue<>(); //Node currently being explored

    //At first, we don't know the distance to a node, so the distance is Infinity.
    Arrays.fill(distance, Float.POSITIVE_INFINITY);

    //Initialize the starting node.
    distance[startNodeId] = 0;
    inExploration.add(new WeightedNode(startNodeId, 0));

    while(!inExploration.isEmpty()) {
      WeightedNode smallestNode = inExploration.remove();

      if(smallestNode.nodeId == endNodeId) break; //If the end node is found, stop there.

      //For the current nodeId, try every edge coming out of it.
      for (int i = 0; i < graph.nodeOutDegree(smallestNode.nodeId()); i++) {
        int edgeId = graph.nodeOutEdgeId(smallestNode.nodeId(), i);
        int nextNodeId = graph.edgeTargetNodeId(edgeId);

        float distanceByThisPath = (float) (
                distance[smallestNode.nodeId()]
                        + graph.edgeLength(edgeId) * costFunction.costFactor(smallestNode.nodeId(), edgeId)
        ); //Distance by this path = distance to the current node + length of edge * the cost of this path.


        if(distanceByThisPath < distance[nextNodeId]) { //If a new better path is found.
          distance[nextNodeId] = distanceByThisPath;  //Change the distance.

          //(Re)explore every sub-node for this node, the distance for the priority queue is also impacted by the remaining distance
          //between the current node and the end node (by bird flight).
          inExploration.add(new WeightedNode(
                  nextNodeId, (float) (distanceByThisPath + graph.nodePoint(smallestNode.nodeId()).distanceTo(endPointCh))
          ));
          //Set the new optimal predecessor.
          predecessor[nextNodeId] = storeEdgeIndexAndNodeId(i, smallestNode.nodeId());
        }
      }
    }

    //There we should have a complete predecessor list.
    if(inExploration.isEmpty()) return null;
    List<Edge> edges = new ArrayList<>();

    //Retrace the path from the endNode.
    int currentNodeId = endNodeId;
    while (currentNodeId != startNodeId) {
      //Extract the 4 last bits.
      int indexEdge = Bits.extractUnsigned(predecessor[currentNodeId], LENGTH_NODE_BITS, LENGTH_EDGEID_BITS);
      //Extract the 28 first bits.
      int previousNodeId = Bits.extractUnsigned(predecessor[currentNodeId], 0, LENGTH_NODE_BITS);
      edges.add(Edge.of(graph, graph.nodeOutEdgeId(previousNodeId, indexEdge) ,previousNodeId, currentNodeId));
      currentNodeId = previousNodeId;
    }
    Collections.reverse(edges);
    return new SingleRoute(edges);
  }

  /**
   * Takes two value : the edgeIndex and the nodeId and store them both in an int.
   * @param edgeIndex the index of the edge based on nodeId.
   * @param nodeId a nodeId of a node with edges.
   * @return an int containing both variable : edgeIndex (first 4 bits), nodeId (next 28 bits).
   */
  private int storeEdgeIndexAndNodeId(int edgeIndex, int nodeId) {
    int value = edgeIndex << LENGTH_NODE_BITS;
    value = value | nodeId;
    return value;
  }
}

package ch.epfl.javelo.routing;

/**
 * Represents the function of cost.
 * @author Eden Kahane (346481).
 */
public interface CostFunction {

  /**
   * Returns the factor by which the edge coming from
   * the node (nodeId) must be multiplied by.
   * @param nodeId id of the node where the edge come from.
   * @param edgeId the id of the edge.
   * @return a multiplayer factor between 1 and positive infinity.
   */
  double costFactor(int nodeId, int edgeId);
}

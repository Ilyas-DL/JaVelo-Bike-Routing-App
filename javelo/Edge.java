package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * This class represents an edge of an Itinerary.
 * @param fromNodeId the nodeId it's coming from.
 * @param toNodeId the nodeId of the destination.
 * @param fromPoint the point it's coming from.
 * @param toPoint the point of destination.
 * @param length the length of the edge.
 * @param profile a function returning the height at a position in the edge.
 * @author Eden Kahane (346481).
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length, DoubleUnaryOperator profile) {

  /**
   * Builds an Edge using Graph.
   * @param graph a Graph that is used to find data.
   * @param edgeId the id of this edge.
   * @param fromNodeId the nodeId where the edge is coming from.
   * @param toNodeId the nodeId of the destination.
   * @return a new Edge with all the correct data based on the inputs.
   */
  public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
    PointCh fromPoint = graph.nodePoint(fromNodeId);
    PointCh toPoint = graph.nodePoint(toNodeId);
    return new Edge(fromNodeId, toNodeId, fromPoint, toPoint, graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
  }

  /**
   * Finds the distance from the start of the closest point on this edge which is the closest
   * to the given point.
   * @param point from which will be searched the closest final point of the edge.
   * @return the distance in the edge of the point closest to the required point.
   */
  public double positionClosestTo(PointCh point) {
    return Math2.projectionLength(fromPoint.e(), fromPoint().n(), toPoint.e(), toPoint.n(), point.e(), point.n());
  }

  /**
   * Finds the coordinate in the swiss system of a point on the edge based on its distance
   * from the origin of the edge.
   * @param position the distance to the origin of the edge.
   * @return a point in the swiss system.
   */
  public PointCh pointAt(double position) {
    double ratio = position / length;
    double eDelta = (toPoint.e() - fromPoint().e()) * ratio;
    double nDelta = (toPoint.n() - fromPoint().n()) * ratio;
    return new PointCh(fromPoint.e() + eDelta, fromPoint().n() + nDelta);
  }

  /**
   * Finds the elevation of a point of the edge based on it's distance.
   * @param position the distance from the origin of the edge.
   * @return the elevation of this point.
   */
  public double elevationAt(double position) {
    return profile.applyAsDouble(position);
  }
}

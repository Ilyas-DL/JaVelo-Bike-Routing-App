package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Interface that represents an itinerary.
 * @author Eden Kahane (346481).
 */
public interface Route {

  /**
   * Returns the index of the segment at a position on an itinerary.
   * @param position the position on the itinerary in meters.
   * @return the index of a segment.
   */
  int indexOfSegmentAt(double position);

  /**
   * Returns the length of the itinerary.
   * @return the length in meters.
   */
  double length();

  /**
   * Returns all the edges part of the itinerary.
   * @return a list of edges.
   */
  List<Edge> edges();

  /**
   * Returns all the points on each end of each edges of the itinerary.
   * @return a list of points in the swiss system.
   */
  List<PointCh> points();

  /**
   * Returns the point at the given position on the itinerary.
   * @param position a position on the itinerary.
   * @return a point in the swiss system.
   */
  PointCh pointAt(double position);

  /**
   * Returns the elevation of a point on the itinerary.
   * @param position a position on the itinerary.
   * @return the elevation in meters.
   */
  double elevationAt(double position);

  /**
   * Returns the nodeId of the closest node from this position.
   * @param position a position on the itinerary.
   * @return a nodeId of a node.
   */
  int nodeClosestTo(double position);

  /**
   * Finds the closest point on the itinerary close to the point given.
   * @param point where to search for the closest point on the itinerary.
   * @return a point on the itinerary.
   */
  RoutePoint pointClosestTo(PointCh point);
}

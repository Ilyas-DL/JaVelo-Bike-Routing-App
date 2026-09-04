package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * This class represents the point on an itinerary closest to another
 * point given (that can be outside of the itinerary).
 *  @author Eden Kahane (346481).
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

  /**
   * Public instantiation for a RoutePoint as null point, Double.Nan position and
   * Double.POSITIVE_INFINITY for future comparisons.
   */
  public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

  /**
   * Returns a new RoutePoint with a new position found with the positionDifference.
   * @param positionDifference delta value of the new position.
   * @return a new RoutePoint shifted by the positionDifference.
   */
  public RoutePoint withPositionShiftedBy(double positionDifference) {
    return new RoutePoint(point, position + positionDifference, distanceToReference);
  }

  /**
   * Compares this RoutePoint to another RoutePoint and return the one with the shortest distanceToReference.
   * @param that another RoutePoint to compare.
   * @return the RoutePoint with the shortest distanceToReference.
   */
  public RoutePoint min(RoutePoint that) {
    return distanceToReference <= that.distanceToReference() ? this : that;
  }

  /**
   * Checks this RoutePoint and another point and return the one with the lowest distanceToReference.
   * @param thatPoint the point of the given RoutePoint.
   * @param thatPosition the position of the given RoutePoint.
   * @param thatDistanceToReference the distanceToReference of the given RoutePoint.
   * @return the RoutePoint with the lowest distanceToReference.
   */
  public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
    return distanceToReference <= thatDistanceToReference ? this :
            new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
  }
}
package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Store the coordinates using the swiss system.
 * @author Eden Kahane (346481).
 */
public record PointCh(double e, double n) {
  /**
   * Stores the coordinate of a point in Switzerland based in the swiss system.
   * @param e east coordinate.
   * @param n north coordinate.
   */
  public PointCh {
    Preconditions.checkArgument(SwissBounds.containsEN(e, n));
  }

  /**
   * Returns the distance in squared meters between this point and the given one.
   * @param that another point in the swiss system.
   * @return the distance in squared meters.
   */
  public double squaredDistanceTo(PointCh that) {
    return Math2.squaredNorm(this.e - that.e, this.n - that.n);
  }

  /**
   * Gives the distance in meters between this point and the given one.
   * @param that another point in the swiss system.
   * @return a positive distance in meter.
   */
  public double distanceTo(PointCh that) {
    return Math2.norm(this.e - that.e, this.n - that.n);
  }

  /**
   * Gives the longitude of this point.
   * @return the longitude.
   */
  public double lon() {
    return Ch1903.lon(e, n);
  }

  /**
   * Gives the latitude of this point.
   * @return the latitude.
   */
  public double lat() {
    return Ch1903.lat(e, n);
  }
}

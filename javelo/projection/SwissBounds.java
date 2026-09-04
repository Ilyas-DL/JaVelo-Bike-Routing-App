package ch.epfl.javelo.projection;

/**
 * This class is used to validate if the coordinates are indeed in Switzerland map.
 * @author Eden Kahane (346481).
 */
public final class SwissBounds {
  private SwissBounds() {}
  public static final double MIN_E = 2_485_000;
  public static final double MAX_E = 2_834_000;
  public static final double MIN_N = 1_075_000;
  public static final double MAX_N = 1_296_000;

  /**
   * The width and height depending on minimal and maximal east and north coordinates.
   */
  public static final double WIDTH = MAX_E - MIN_E;
  public static final double HEIGHT = MAX_N - MIN_N;

  /**
   * Checks if the coordinate is in Swiss boundary.
   * @param e the east coordinate of the point.
   * @param n the north coordinate of the point.
   * @return true if the value is in the boundaries.
   */
  public static boolean containsEN(double e, double n) {
    return e >= MIN_E && e <= MAX_E &&  n >= MIN_N && n <= MAX_N;
  }
}

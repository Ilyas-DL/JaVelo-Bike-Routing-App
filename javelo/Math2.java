package ch.epfl.javelo;

/**
 * class that adds additional methods for mathematical methods.
 * @author Ilyas Hawazine (326815).
 */
public final class Math2 {
  private Math2() {}

  /**
   * Returns the result of the division of x by y rounded to the superior.
   * @param x number divided.
   * @param y number divided by.
   * @return result of a division as an int.
   */
  public static int ceilDiv(int x, int y) {
    Preconditions.checkArgument(x >= 0 && y > 0);
    return (x + y - 1) / y;
  }

  /**
   * Found the coordinates of a point on a line passing by (0, y0) and (1, y1).
   * @param y0 the y coordinates of the point at (0, y0), used to form the line.
   * @param y1 the y coordinates of the point at (1, y1), used to form the line.
   * @param x the x coordinates of the point for which you want to find the y.
   * @return the y coordinate of the point on the line.
   */
  public static double interpolate(double y0, double y1, double x) {
    return Math.fma(y1 - y0, x, y0);
  }

  /**
   * Limit a value between two interval, if the value is not in the interval, find the closest value
   * in it.
   * @param min min value of the interval (included).
   * @param v value to limit.
   * @param max max value of the interval (included).
   * @return a value between min and max (included).
   */
  public static int clamp(int min, int v, int max) {
    Preconditions.checkArgument(min <= max);
    return v > max ? max : Math.max(v, min);
  }

  /**
   * Limit a value between two interval, if the value is not in the interval, find the closest value
   * in it.
   * @param min min value of the interval (included).
   * @param v value to limit.
   * @param max max value of the interval (included).
   * @return a value between min and max (included).
   */
  public static double clamp(double min, double v, double max) {
    Preconditions.checkArgument(min <= max);
    return v > max ? max : Math.max(v, min);
  }

  /**
   * Return the hyperbolic arcsin value of x.
   * @param x the value whose hyperbolic asin is to be returned.
   * @return the hyperbolic arcsin value of x.
   */
  public static double asinh(double x) {
    return Math.log(x + Math.hypot(1, x));
  }

  /**
   * For two vectors, return the dot value of the two vectors
   * @param uX X value of the u vector
   * @param uY Y value of the u vector
   * @param vX x value of the v vector
   * @param vY y value of the v vector
   * @return the dot product
   */
  public static double dotProduct(double uX, double uY, double vX, double vY) {
    return Math.fma(uX, vX, uY * vY);
  }

  /**
   * Return the squared-up norm of a vector.
   * @param uX X value of a vector.
   * @param uY Y value of a vector.
   * @return the squared norm.
   */
  public static double squaredNorm(double uX, double uY) {
    return Math.pow(uX, 2) + Math.pow(uY, 2);
  }

  /**
   * Return the norm of a vector.
   * @param uX X value of a vector.
   * @param uY Y value of a vector.
   * @return the norm of the vector.
   */
  public static double norm(double uX, double uY) {
    return Math.sqrt(squaredNorm(uX, uY));
  }

  /**
   * Find the length between the point A and the projection of the P on the line
   * formed by the point A and the point B.
   * @param aX X value of A.
   * @param aY Y value of A.
   * @param bX X value of B.
   * @param bY Y value of B.
   * @param pX X value of P.
   * @param pY Y value of P.
   * @return the length between A and the projection of P.
   */
  public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY) {
    return dotProduct(pX - aX,pY - aY,bX - aX,bY - aY) / norm(bX - aX,bY - aY);
  }
}

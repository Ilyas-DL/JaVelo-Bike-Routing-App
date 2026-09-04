package ch.epfl.javelo;

/**
 * Allows storing double value on a single int using Q28.4 format.
 * @author Eden Kahane (346481).
 */
public final class Q28_4 {
  private final static int Q28_4_OFFSET = 4;

  private Q28_4() {}

  /**
   * Takes an int and return the value stored as Q28.4.
   * @param i the integer value to transform.
   * @return a value as Q28.4 stored in an int variable.
   */
  public static int ofInt(int i) {
    return i << Q28_4_OFFSET;
  }

  /**
   * Take a value stored as Q28.4 inside an int variable
   * and return the value as a double.
   * @param q28_4 a Q28.4 value stored in an int.
   * @return the value as double.
   */
  public static double asDouble(int q28_4) {
    return Math.scalb((double) q28_4, -Q28_4_OFFSET);
  }

  /**
   * Take a value stored as Q28.4 inside an int variable
   * and return the value as a float.
   * @param q28_4 a Q28.4 value stored in an int.
   * @return the value as float.
   */
  public static float asFloat(int q28_4) {
    return Math.scalb(q28_4, -Q28_4_OFFSET);
  }
}

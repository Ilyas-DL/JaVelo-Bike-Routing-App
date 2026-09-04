package ch.epfl.javelo;

/**
 * Simple condition check for correct arguments
 * @author Eden Kahane (346481)
 */
public final class Preconditions {
  private Preconditions() {}

  /**
   * method for checking initial conditions.
   * @param shouldBeTrue : the argument we want to test.
   * @throws IllegalArgumentException if the condition is not true.
   */
  public static void checkArgument(boolean shouldBeTrue) {
    if(!shouldBeTrue) throw new IllegalArgumentException();
  }
}

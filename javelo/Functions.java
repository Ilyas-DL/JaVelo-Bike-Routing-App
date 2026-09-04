package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * this class works as a function that associates x with y depending on their values.
 * @author Eden Kahane (346481).
 */
public final class Functions {
  private Functions() {}

  /**
   * Returns a constant method.
   * @param y sets the constant y.
   * @return gives back the constant y.
   */
  public static DoubleUnaryOperator constant(double y) {
    return operand -> y;
  }

  /**
   * Returns the y value of a point x, the y value is on the segment created by the samples.
   * @param samples a list of points that form segments.
   * @param xMax the maximum x value of the samples.
   * @return the y value on a segment created by the samples.
   */
  public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
    Preconditions.checkArgument(samples.length >= 2 & xMax > 0);
    final float[] copySampled = samples.clone();
    return operand -> {
      double clampedOperand = Math2.clamp(0d, operand, xMax);
      if(clampedOperand == xMax) return copySampled[copySampled.length - 1];

      double step = xMax / (copySampled.length - 1);
      int sampleIndex = (int) Math.floor(clampedOperand / step);

      return Math.fma((copySampled[sampleIndex + 1] - copySampled[sampleIndex]) / step,
              clampedOperand - step * sampleIndex,
              samples[sampleIndex]);
    };
  }
}

package ch.epfl.javelo;

/**
 * This class manages bit manipulations.
 * @author Eden Kahane (346481).
 */
public final class Bits {
  private Bits() {}

  /**
   * Extract a certain numbers of bite from a value at a certain position.
   * while keeping the correct sign.
   * @param value a value in int(32 bites) from which to extract some bites.
   * @param start a number between 0 and 31 indicating where to start extracting bites.
   * @param length the length of the bites to extract, between 1 and 32.
   * @return a value in int of the extracted bite.
   */
  public static int extractSigned(int value, int start, int length) {
    Preconditions.checkArgument(start + length <= Integer.SIZE && start >= 0 && length > 0);
    int shiftBy = Integer.SIZE - start - length;
    return  (value << shiftBy) >> Integer.SIZE - length;
  }

  /**
   * Extract certain numbers of bite from a value at a certain position,
   * it does not keep the correct sign (put 0 while shifting to the right).
   * @param value a value in int(32 bites) from which to extract some bits.
   * @param start a number between 0 and 31 indicating where to start extracting bits.
   * @param length the length of the bits to extract, between 1 and 32.
   * @return a value in int of the extracted bite.
   */
  public static int extractUnsigned(int value, int start, int length)  {
    Preconditions.checkArgument(start + length <= Integer.SIZE && start >= 0 && length > 0 && length < Integer.SIZE);
    int shiftBy = Integer.SIZE - start - length;
    return  (value << shiftBy) >>> Integer.SIZE - length;
  }
}

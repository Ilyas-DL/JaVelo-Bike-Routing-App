package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;

/**
 * A function that generates an elevationProfile for a Route.
 * @author Eden Kahane (346481).
 */
public final class ElevationProfileComputer {
  private ElevationProfileComputer() {}

  /**
   * For a given route, generate the ElevationProfile that is linked to this
   * route. A maxStepLength can be given to specify the maximum distance between two points to
   * obtain a ElevationProfile as precise as needed.
   * This method accept values of Double.NaN
   * @param route Route for which generate an associated ElevationProfile
   * @param maxStepLength the max distance between two points of data created for
   * ElevationProfile, this value allows you to make your elevationProfile as precise as needed
   * @return an ElevationProfile associating a distance to an elevation, ElevationProfile always return 0 if it was filled with NaN.
   */
  public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
    Preconditions.checkArgument(maxStepLength > 0);
    int samplesNumber = (int) Math.ceil(route.length() / maxStepLength) + 1;
    double lengthOfSample = route.length() / (samplesNumber - 1);
    double currentPosition = 0;

    //Initial fill with value
    float[] samples = new float[samplesNumber];
    for(int i = 0; i < samplesNumber; i++) {
      samples[i] = (float) route.elevationAt(currentPosition);
      currentPosition += lengthOfSample;
    }

    //Fill start of the array with NaN.
    if(Double.isNaN(samples[0])) {
      for(int i = 1; i < samples.length; i++) {
        if(!Double.isNaN(samples[i])) {
          Arrays.fill(samples, 0, i, samples[i]);
          break;
        }
      }
    }
    if(Double.isNaN(samples[0])) Arrays.fill(samples, 0); //If the array is made only of NaN fill it with 0.

    //Fill end of array in case of NaN.
    if(Double.isNaN(samples[samples.length - 1])) {
      for(int i = samples.length - 1; i >= 0; i--) {
        if(!Double.isNaN(samples[i])) {
          Arrays.fill(samples, i + 1, samples.length, samples[i]);
          break;
        }
      }
    }

    //Fill hole of NaN in array.
    for(int NaNIndex = 0; NaNIndex < samples.length; NaNIndex++) {
      if (!Double.isNaN(samples[NaNIndex])) continue; //Search for the first NaN.
      for (int notNaNIndex = NaNIndex + 1; notNaNIndex < samples.length; notNaNIndex++) {
        if(!Double.isNaN(samples[notNaNIndex])) { //Search for the first not a NaN after the NaN found.

          for (int i = NaNIndex; i < notNaNIndex; i++) {
            //Generate value for NaN.
            samples[i] = (float) Math2.interpolate(samples[NaNIndex - 1], samples[notNaNIndex],
                    (i - NaNIndex + 1) / (double) (notNaNIndex - NaNIndex + 1));
          }
          break;
        }
      }
    }
    return new ElevationProfile(route.length(), samples);
  }
}

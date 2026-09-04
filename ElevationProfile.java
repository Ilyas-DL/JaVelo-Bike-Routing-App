package ch.epfl.javelo.routing;


import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

/**
 * Class that takes the size of the road and all the different
 * heights of the elevation samples that are within it.
 * @author Ilyas Hawazine (326815).
 */
public final class ElevationProfile {
    private final double length;
    private final float[] elevationSamples;
    private final double minElevation;
    private final double maxElevation;
    private final double totalAscentDistance;
    private final double totalDescentDistance;
    private final DoubleUnaryOperator elevationOperator;

    /**
     * Public constructor that takes the length of the road and the height of different elevation
     * samples that are within that length.
     * @param length the size of the road.
     * @param elevationSamples different heights of the nodes.
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument((length > 0) && (elevationSamples.length >= 2));
        this.length = length;
        this.elevationSamples = elevationSamples.clone();
        this.minElevation = listElevations(elevationSamples).getMin();
        this.maxElevation = listElevations(elevationSamples).getMax();
        this.totalAscentDistance = computeElevation(true);
        this.totalDescentDistance = computeElevation(false);
        this.elevationOperator = Functions.sampled(elevationSamples,length);
    }

    /**
     * Returns the size of the road.
     * @return the size of the road in double.
     */
    public double length(){
        return this.length;
    }

    /**
     * Returns the lowest height of the sample in the road.
     * @return the lowest height of all thr samples in double.
     */
    public double minElevation(){
        return minElevation;
    }

    /**
     * Returns the highest height of the sample in the road.
     * @return the highest height of all thr samples in double.
     */
    public double maxElevation(){
        return maxElevation;
    }

    /**
     * Returns the total ascent in meters contained in the length of the road.
     * That's to say : counts only the distance between samples that are going up.
     * @return the distance of total ascent of all the samples that go up.
     */
    public double totalAscent(){
        return totalAscentDistance;
    }

    /**
     * Returns the total descent in meters contained in the length of the road.
     * That's to say : counts only the distance between samples that are going down.
     * @return the distance of total descent of all the samples that go down.
     */
    public double totalDescent(){
        return totalDescentDistance;
    }

    /**
     * Returns the height at a specific position (in meters) of the road depending on
     * the different elevation samples.
     * @param position of the road we would like to know the height of it.
     * @return the height of the road at the "position" point.
     */
    public double elevationAt(double position){
        return elevationOperator.applyAsDouble(position);
    }

    /**
     * Takes a list of elevation tables and converts it to DoubleSummaryStatistics.
     * @param elevation a table of elevations.
     * @return a lift of DoubleSummaryStatistics elevations.
     */
    private static DoubleSummaryStatistics listElevations(float[] elevation){
        DoubleSummaryStatistics listElevations = new DoubleSummaryStatistics();
        for (float elevationSample : elevation) {
            listElevations.accept(elevationSample);
        }
        return listElevations;
    }

    /**
     * Computes the ascent or the descent one time.
     * @param ascension if false we calculate  the descent.
     * @return the total distance of ascend or descend.
     */
    private double computeElevation(boolean ascension){
        double totalDistance = 0;
        for (int a = 1; a < elevationSamples.length; a++) {
            double actualDistance = elevationSamples[a] - elevationSamples[a - 1];
            if (actualDistance <= 0 && !ascension || actualDistance>=0 && ascension) {
                totalDistance += actualDistance;
            }
        }
        if(ascension) return totalDistance;
        return totalDistance !=0 ? -totalDistance : 0;
    }
}
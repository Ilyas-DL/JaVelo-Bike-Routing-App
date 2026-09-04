package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;


/**
 * This class manages the system for converting from x and y coordinates
 * on screen to longitude et latitude and vice-versa.
 * @author Ilyas Hawazine (326815).
 */
public final class WebMercator {
    private WebMercator(){}

    /**
     * Takes longitude in radians and converts it into x coordinate.
     * @param lon longitude in radians.
     * @return x coordinate converted from longitude between 0 and 1.
     */
    public static double x(double lon){
     return (1/(2 * Math.PI)) * (lon + Math.PI);
    }

    /**
     * Takes latitude in radians and converts it into y coordinate.
     * @param lat : latitude radians.
     * @return y coordinate converted from latitude between 0 and 1
     */
    public static double y(double lat){
        return 1/(2 * (Math.PI)) * (Math.PI - Math2.asinh(Math.tan(lat)));
    }

    /**
     * Takes the x coordinate and converts it into longitude in radians
     * @param x : x coordinate between 0 and 1
     * @return longitude converted from x coordinate in radians
     */
    public static double lon(double x){
        return 2 * Math.PI * x - Math.PI;
    }

    /**
     * Takes the y coordinate and converts it into latitude in radians
     * @param y : y coordinate between 0 and 1
     * @return latitude converted from y coordinate in radians
     */
    public static double lat(double y){
        return Math.atan(Math.sinh(Math.PI-2 * Math.PI * y));
    }
}

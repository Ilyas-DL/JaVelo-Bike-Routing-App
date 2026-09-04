package ch.epfl.javelo.projection;

/**
 * Switzerland system converting E and N coordinates into longitude and latitude and vice versa.
 * @author Ilyas Hawazine (326815).
 */
public final class Ch1903 {
    private Ch1903(){}

    /**
     * Converts the coordinate of longitude et latitude into the E (est) coordinate.
     * @param lon the longitude coordinate of a point in radians.
     * @param lat the latitude coordinate of a point in radians.
     * @return returns E (est) coordinate in the swiss system.
     */
    public static double e(double lon, double lat){
        double lon1 = 1e-4 * (3600 * Math.toDegrees(lon) - 26782.5);
        double lat1 = 1e-4 * (3600 * Math.toDegrees(lat) - 169028.66);
        return 2600072.37
                + 211455.93 * lon1
                - 10938.51 * lon1 * lat1
                - 0.36 * lon1 * Math.pow(lat1, 2)
                - 44.54 * Math.pow(lon1, 3);
    }

    /**
     * Converts the coordinate of longitude et latitude into the N (north) coordinate.
     * @param lon the longitude coordinate of a point in radians.
     * @param lat the latitude coordinate of a point in radians.
     * @return returns N (north) coordinate in the swiss system.
     */
    public static double n(double lon, double lat){
        double lon1 = 1e-4 * (3600 * Math.toDegrees(lon) - 26782.5);
        double lat1 = 1e-4 * (3600 * Math.toDegrees(lat) -169028.66);
        return 1200147.07
                + 308807.95 * lat1
                + 3745.25 * Math.pow(lon1, 2)
                + 76.63 * Math.pow(lat1, 2)
                - 194.56 * Math.pow(lon1, 2) * lat1
                + 119.79 * Math.pow(lat1, 3);
    }

    /**
     * Converts the coordinate of E (est) and N(north) into longitude coordinate.
     * @param e the east coordinate of a point.
     * @param n the north coordinate of a point.
     * @return returns the longitude coordinate in radians.
     */
    public static double lon(double e, double n){
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double lon0= 2.6779094 + 4.728982 * x
                + 0.791484 * x * y
                + 0.1306 * x * Math.pow(y, 2)
                - 0.0436 * Math.pow(x, 3);
        return Math.toRadians(lon0 * 100d / 36);
    }

    /**
     * Converts the coordinate of E (est) and N(north) into latitude coordinate.
     * @param e the east coordinate of a point.
     * @param n the north coordinate of a point.
     * @return returns the latitude coordinate in radians.
     */
    public static double lat(double e, double n){
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double lat0 = 16.9023892 + 3.238272 * y
                - 0.270978 * Math.pow(x, 2)
                - 0.002528 * Math.pow(y, 2)
                - 0.0447 * Math.pow(x, 2) * y
                - 0.0140 * Math.pow(y, 3);
        return Math.toRadians(lat0 * (100d / 36));
    }
}
package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * Class takes as input x and y coordinates on the screen and transforms depending them on zoom level.
 * @author Ilyas Hawazine (326815).
 */
public record PointWebMercator(double x, double y) {

    private final static int DEFAULT_ZOOM_EXPONENT = 8;

    /**
     * Public constructor that takes x and y coordinates.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public PointWebMercator{
        Preconditions.checkArgument(x >= 0 && x <= 1 && y >= 0 && y <= 1);
    }

    /**
     * Transforms x and y coordinates in a new coordinates depending on the zoom.
     * @param zoomLevel how far we want to zoom.
     * @param x x coordinate.
     * @param y y coordinate.
     * @return returns PointWebMercator with new coordinates x and y coordinates zoomed in another level.
     */
    public static PointWebMercator of(int zoomLevel, double x, double y){
        return new PointWebMercator(Math.scalb(x, -zoomLevel - DEFAULT_ZOOM_EXPONENT), Math.scalb(y, -zoomLevel - DEFAULT_ZOOM_EXPONENT));
    }

    /**
     * Returns the PointWebMercator given by the swiss coordinates of PointCh.
     * @param pointCh containing x and y coordinate.
     * @return PointWebMercator with swiss coordinates from pointCh.
     */
    public static PointWebMercator ofPointCh(PointCh pointCh){
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }

    /**
     * Returns the x coordinate converted from the zoom level given.
     * @param zoomLevel how far the user wants to zoom in.
     * @return the x coordinate converted from the zoom level given.
     */
    public  double xAtZoomLevel(int zoomLevel){
        return Math.scalb(this.x, zoomLevel + DEFAULT_ZOOM_EXPONENT);
    }

    /**
     * Returns the y coordinate converted from the zoom level given.
     * @param zoomLevel how far the user wants to zoom in.
     * @return the y coordinate converted from the zoom level given.
     */
    public double yAtZoomLevel(int zoomLevel){
        return Math.scalb(this.y, zoomLevel + DEFAULT_ZOOM_EXPONENT);
    }

    /**
     * Gives the longitude of the point in radians.
     * @return the longitude of the point in radians.
     */
    public double lon(){
        return WebMercator.lon(this.x);
    }

    /**
     * Gives the latitude of the point in radians.
     * @return the latitude of the point in radians.
     */
    public double lat(){
        return WebMercator.lat(this.y);
    }

    /**
     * Gives the coordinates x and y of PointCh (for a giving position) by converting x and y into
     * longitude and latitude and then into e and n. In case x and y are out of boundaries, it returns null.
     * @return return the the coordinates x and y of PointCh or null.
     */
    public PointCh toPointCh(){
        double lon = lon();
        double lat = lat();
        double e =  Ch1903.e(lon, lat);
        double n = Ch1903.n(lon, lat);
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }
}

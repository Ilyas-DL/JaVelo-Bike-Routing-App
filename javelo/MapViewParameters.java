package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the background of the map for the
 * graphic interface.
 * @param zoomLevel a zoomLevel between 0 and 20.
 * @param cornerX the top-left x coordinate of the showed map.
 * @param cornerY the top-left y coordinate of the showed map.
 *
 * @author Eden Kahane (346481).
 */
public record MapViewParameters(int zoomLevel, double cornerX, double cornerY) {

  /**
   * Returns the top most coordinate in Point2D format.
   * @return the coordinate in Point2D format.
   */
  public Point2D topLeft() {
    return new Point2D(cornerX, cornerY);
  }

  /**
   * Returns new instance of MapViewParameters with new top-left
   * corner but keeping the same zoomLevel.
   * @param newCornerX a new coordinate for the top left x coordinate.
   * @param newCornerY a new coordinate fot the top left y coordinate.
   * @return a new MapViewParameters.
   */
  public MapViewParameters withMinXY(double newCornerX, double newCornerY) {
    return new MapViewParameters(zoomLevel, newCornerX, newCornerY);
  }

  /**
   * Returns a new instance of MapViewParameters shifted by x, y
   * but with the same zoomLevel.
   * @param x how much should it be shifted by x.
   * @param y how much should it be shifted by y.
   * @return a new MapViewParameters.
   */
  public MapViewParameters addXY(double x, double y) {
    return new MapViewParameters(zoomLevel, x + cornerX, y + cornerY);
  }

  /**
   * Takes coordinates based on the MapViewParameters.
   * Takes coordinates expressed base on the MapViewParameters
   * and return the PointWebMercator corresponding.
   * @param x x coordinated expressed according to cornerX.
   * @param y x coordinated expressed according to cornerY.
   * @return a new PointWebMercator.
   */
  public PointWebMercator pointAt(double x, double y) {
    return PointWebMercator.of(zoomLevel, cornerX + x, cornerY + y);
  }

  /**
   * Takes a PointWebMercator and return the x coordinate
   * expressed according to the top left corner.
   * @param pointWebMercator a point in PointWebMercator.
   * @return x coordinate expressed from the top left corner (cornerX).
   */
  public double viewX(PointWebMercator pointWebMercator) {
    return pointWebMercator.xAtZoomLevel(zoomLevel) - cornerX;
  }

  /**
   * Takes a PointWebMercator and return the y coordinate
   * expressed according to the top left corner.
   * @param pointWebMercator a point in PointWebMercator.
   * @return y coordinate expressed from the top left corner (cornerY).
   */
  public double viewY(PointWebMercator pointWebMercator) {
    return pointWebMercator.yAtZoomLevel(zoomLevel) - cornerY;
  }
}

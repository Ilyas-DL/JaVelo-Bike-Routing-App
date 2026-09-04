package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

/**
 * Manage the pane for the visualization
 * of the line of the road and the circle.
 * @author Eden Kahane (346481).
 */
public final class RouteManager {
    private final ReadOnlyProperty<MapViewParameters> mapViewParametersReadOnly;
    private final RouteBean routeBean;
    private final Pane pane;
    private final Polyline polyline;
    private final Circle circle;

    /**
     * Public constructor for RouteManager.
     * @param routeBean the actual routes of the itinerary.
     * @param mapViewParametersReadOnly mapViewParameters only to be read.
     */
    public RouteManager(RouteBean routeBean, ReadOnlyProperty<MapViewParameters> mapViewParametersReadOnly) {
        this.mapViewParametersReadOnly = mapViewParametersReadOnly;
        this.routeBean = routeBean;

        this.pane = new Pane();
        this.polyline = new Polyline();
        this.circle = new Circle(0, 0,5);

        setGraphicsAndListeners();
    }

    /**
     * Gets the pane managing the line of the road
     * and the circle of the highlighted position
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Setting listeners for different properties and graphical elements.
     */
    private void setGraphicsAndListeners() {

        //setting graphics
        polyline.setId("route");
        circle.setId("highlight");
        circle.setOnMouseClicked(this::mouseClickedEvent);
        circle.setVisible(false);

        //populating pane
        pane.getChildren().add(polyline);
        pane.getChildren().add(circle);
        pane.setPickOnBounds(false);
        //set listeners.
        routeBean.getRouteProperty().addListener((observable, oldValue, newValue) -> redrawRoad());
        mapViewParametersReadOnly.addListener((observable, oldValue, newValue) ->
                mapViewParameterChangeEvent(oldValue, newValue));
        routeBean.highlightedPositionProperty().addListener((observable, oldValue, newValue) -> redrawRoad());
    }

    /**
     * Method that it called to redraw all the road (when the road changes).
     */
    private void redrawRoad() {
        if(routeBean.getRoute() != null) {
            MapViewParameters mapViewParameters = mapViewParametersReadOnly.getValue();

            //Clear the previous polyline.
            polyline.getPoints().clear();
            polyline.relocate(0, 0);

            //Add each point.
            for (PointCh pointCh : routeBean.getRoute().points()) {
                PointWebMercator pointWebMercator = PointWebMercator.ofPointCh(pointCh);
                polyline.getPoints().addAll(mapViewParameters.viewX(pointWebMercator),
                        mapViewParameters.viewY(pointWebMercator));
            }

            //Add circle of the highlighted position.
            if( !Double.isNaN(routeBean.highlightedPosition()) ) {
                circle.setVisible(true);
                PointWebMercator circlePointWebMercator =
                        PointWebMercator.ofPointCh(routeBean.getRoute().pointAt(routeBean.highlightedPosition()));
                circle.relocate(mapViewParameters.viewX(circlePointWebMercator) -
                        circle.getRadius(), mapViewParameters.viewY(circlePointWebMercator) - circle.getRadius());
            }else {
                circle.setVisible(false);
            }
        }
        pane.setVisible(routeBean.getRoute() != null); //If the road doesn't exist, set everything to invisible.
    }

    /**
     * Method called when the mapViewParameter of the
     * baseMapManager change, so the position of the line/circle,
     * can be updated.
     * @param oldValue the previous value of the MapViewParameters.
     * @param newValue the new value of the MapViewParameters.
     * @see MapViewParameters
     * @see BaseMapManager
     */
    private void mapViewParameterChangeEvent(MapViewParameters oldValue, MapViewParameters newValue) {
        if(routeBean.getRoute() != null) {
            if(oldValue.zoomLevel() != newValue.zoomLevel()) redrawRoad();
            else {
                //Update position of the polyline and circle.
                Point2D point2D = newValue.topLeft().subtract(oldValue.topLeft());
                polyline.setLayoutX(polyline.getLayoutX() - point2D.getX());
                polyline.setLayoutY(polyline.getLayoutY() - point2D.getY());

                circle.setLayoutX(circle.getLayoutX() - point2D.getX());
                circle.setLayoutY(circle.getLayoutY() - point2D.getY());
            }
        }
    }

    /**
     * Method called when the circle is clicked
     * on. It creates a waypoint at the circle position.
     * @param event the MouseEvent of the click.
     */
    private void mouseClickedEvent(MouseEvent event) {
        if(event.isStillSincePress()) {
            int wayPointIndex = routeBean.indexOfNonEmptySegmentAt(routeBean.highlightedPosition());
            PointCh circlePointCh = routeBean.getRoute().pointAt(routeBean.highlightedPosition());
            int nodeId = routeBean.getRoute().nodeClosestTo(routeBean.highlightedPosition());

            routeBean.getWaypointList().add(wayPointIndex + 1, new Waypoint(circlePointCh, nodeId));
        }
    }
}
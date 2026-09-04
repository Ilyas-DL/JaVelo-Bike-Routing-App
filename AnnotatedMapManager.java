package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.function.Consumer;

/**
 * Manage the display of the map,
 * the route and the waypoints.
 * @author Eden Kahane (346481).
 */
public final class AnnotatedMapManager {
        private final StackPane pane;
        private final DoubleProperty positionMouseOnRouteProperty;
        private final ObjectProperty<Point2D> point2DProperty;

        /**
         * Generating an AnnotatedMapManager managing the pane containing the map, the route
         * and the waypoints.
         * @param graph the graph with all the nodes.
         * @param tileManager a tileManager from where which to get the tiles.
         * @param routeBean a routeBean managing the route.
         * @param errorConsumer error consumer.
         */
        public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean routeBean, Consumer<String> errorConsumer) {
                this.pane = new StackPane();
                this.pane.getStylesheets().add("map.css");

                this.positionMouseOnRouteProperty = new SimpleDoubleProperty(0d);
                this.point2DProperty = new SimpleObjectProperty<>(new Point2D(0,0));

                setEventsAndListeners(graph,routeBean,errorConsumer,tileManager);
        }

        /**
         * Setting events on the map and listeners for different properties.
         * @param graph the graph with all the nodes.
         * @param tileManager a tileManager from where to get the tiles.
         * @param routeBean a routeBean managing the route.
         * @param errorConsumer error consumer.
         */
        private void setEventsAndListeners(Graph graph, RouteBean routeBean,
                                           Consumer<String> errorConsumer, TileManager tileManager){
                final MapViewParameters defaultMapViewParameter = new MapViewParameters(12, 543200, 370650);

                ObjectProperty<MapViewParameters> mapViewParametersProperty = new SimpleObjectProperty<>(defaultMapViewParameter);

                WaypointsManager waypointsManager = new WaypointsManager(graph, mapViewParametersProperty,
                        routeBean.getWaypointList(), errorConsumer);
                BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapViewParametersProperty);
                RouteManager routeManager = new RouteManager(routeBean, mapViewParametersProperty);

                pane.getChildren().addAll(
                        baseMapManager.pane(),
                        routeManager.pane(),
                        waypointsManager.pane());
                //events.
                pane.setOnMouseExited(event -> positionMouseOnRouteProperty.set(Double.NaN));

                pane.setOnMouseMoved(event -> {
                        point2DProperty.set(new Point2D(event.getX(), event.getY()));
                        computeHighlightProperty(routeBean, mapViewParametersProperty);
                });

                //listeners.
                routeBean.getRouteProperty().addListener((observable, oldValue, newValue) ->
                        computeHighlightProperty(routeBean, mapViewParametersProperty));

                mapViewParametersProperty.addListener((observable, oldValue, newValue) ->
                        computeHighlightProperty(routeBean, mapViewParametersProperty));
        }

        /**
         * Recomputing the position of the mouse from the route
         * and update the positionMouseOnRouteProperty if the distance
         * of the mouse is less than PIXEL_DISTANCE.
         * @param routeBean the RouteBean containing the route.
         * @param mapViewParametersProperty the current mapViewParameter.
         */
        private void computeHighlightProperty(RouteBean routeBean, ObjectProperty<MapViewParameters> mapViewParametersProperty) {
                final int PIXEL_DISTANCE = 15;

                if(routeBean.getRoute() == null) return;

                //getting the mouse coordinate.
                Point2D mousePosition = point2DProperty.get();
                MapViewParameters mapViewParameters = mapViewParametersProperty.get();

                PointWebMercator mouseMercator = mapViewParameters.pointAt(mousePosition.getX(), mousePosition.getY());
                RoutePoint routePoint = routeBean.getRoute().pointClosestTo(mouseMercator.toPointCh());
                PointWebMercator routeMercator = PointWebMercator.ofPointCh(routePoint.point());

                double routeX = mapViewParameters.viewX(routeMercator);
                double routeY = mapViewParameters.viewY(routeMercator);
                double distance = mousePosition.distance(routeX, routeY);

                if(distance <= PIXEL_DISTANCE) {
                        positionMouseOnRouteProperty.set(routePoint.position());
                } else {
                        positionMouseOnRouteProperty.set(Double.NaN);
                }
        }

        /**
         * Returns the pane of the AnnotatedMapManager with
         * the map, the route and the waypoints.
         * @return a pane.
         */
        public Pane pane() {
                return pane;
        }

        /**
         * Returns the property with the position of the mouse on route.
         * @return a property where the double indicates the distance
         * of the position of the mouse on the route. Double.NAN
         * if the mouse is outside the map.
         */
        public DoubleProperty mousePositionOnRouteProperty() {
                return positionMouseOnRouteProperty;
        }
}

package ch.epfl.javelo.gui;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class that manages all the waypoints on the map.
 * @author Ilyas Hawazine (326815).
 */
public final class WaypointsManager {
    private final ReadOnlyObjectProperty<MapViewParameters> mapProperty;
    private final Map<Group,Integer> mapPoints = new HashMap<>();
    private final ObservableList<Waypoint> observableList;
    private final Consumer<String> consumer;
    private final Graph graph;
    private final Pane pane;

    /**
     * Constructing waypoints that will be associated to the pins on screen.
     * @param graph from where all the routes and paths information will be taken from.
     * @param observableList a list containing all the Waypoints.
     * @param mapProperty this is an objectProperty of MapViewParameters that contains
     *                    all the information about the mouse movements.
     * @param consumer contains a string for text error when there is no
     *                 route on the selected place by the mouse.
     */
    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> mapProperty,
                            ObservableList<Waypoint> observableList, Consumer<String> consumer){
        this.graph = graph;
        this.observableList = observableList;
        this.mapProperty = mapProperty;
        this.consumer  = consumer;

        this.pane = new Pane();
        pane.setPickOnBounds(false);
        drawUpdate();

        //setting the listeners.
        this.observableList.addListener((ListChangeListener<Waypoint>) c -> drawUpdate());
        this.mapProperty.addListener((observable, oldValue, newValue) -> drawUpdate());
    }

    /**
     * returning the pane of the updates pins on screen.
     * @return the pane of the updates pins on screen.
     */
    public Pane pane(){
    return pane;
    }

    /**
     * creating a new waypoint on the map that will be added to the observable list.
     * @param x coordinate of the Waypoint.
     * @param y coordinate of the Waypoint.
     */
    public void addWaypoint(double x, double y) {
        createWayPoint(x, y, null);
    }

    /**
     * method that updates the display of all the pins on the map.
     * Whenever there is a modification of a pin(it's been moved,
     * created a new one or suppressed an old one),
     * the map will automatically be updated and will
     * show the new position of all the left pins.
     */
    private void drawUpdate(){
        //clearing the old pins.
        pane.getChildren().clear();
        mapPoints.clear();

        if(observableList.size() > 0) {
            //creating the first pin.
            Group pin = createPin(observableList.get(0));
            mapPoints.put(pin, 0);
            pane.getChildren().add(pin);
            //setting the first style class for the first pin.
            pane.getChildren().get(0).getStyleClass().remove("middle");
            pane.getChildren().get(0).getStyleClass().add("first");
            //filling and creating the rest of the pins in the list.
            if (observableList.size() > 1) {
                for (int i = 1; i < observableList.size(); ++i) {
                    Group actualPin = createPin(observableList.get(i));
                    mapPoints.put(actualPin, i);
                    pane.getChildren().add(actualPin);
                }
                pane.getChildren().get(observableList.size() - 1).getStyleClass().remove("middle");
                pane.getChildren().get(observableList.size() - 1).getStyleClass().add("last");
            }
        }
    }

    /**
     * creating the graphics of the pin that will be displayed in the map.
     * @param waypoint the given waypoint.
     * @return return a group (made of inside and outside graphics) of the created pin.
     */
    private Group createPin(Waypoint waypoint){
        final String contourExtern = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
        final String contourIntern = "M0-23A1 1 0 000-29 1 1 0 000-23";

        //setting the graphics of the pins.
        //the outside counter and the insideCircle.
        SVGPath outsideCounter = new SVGPath();
        SVGPath insideCounter = new SVGPath();

        outsideCounter.getStyleClass().add("pin_outside");
        outsideCounter.setContent(contourExtern);

        insideCounter.getStyleClass().add("pin_inside");
        insideCounter.setContent(contourIntern);
        //grouping the two graphic object into one property.
        Group pin = new Group(outsideCounter, insideCounter);
        pin.getStyleClass().add("pin");
        pin.getStyleClass().add("middle");
        //setting the coordinates on the map of the new created pin.
        PointWebMercator waypointCh = PointWebMercator.ofPointCh(waypoint.pointCh());
        double x = mapProperty.getValue().viewX(waypointCh);
        double y = mapProperty.getValue().viewY(waypointCh);
        setPinCoordinates(pin, x, y);
        settingEvents(pin);
        return pin;
    }

    /**
     * Setting the three events : clicked, dragged and released for every created pin.
     * @param pin given pin.
     */
    private void settingEvents(Group pin){
        //event when we click to delete a pin
        pin.setOnMouseClicked(event ->{
            if(event.isStillSincePress()) {
                observableList.remove((int)mapPoints.get(pin));
            }
        });
        //event for dragging the pin.
        pin.setOnMouseDragged(event ->
                setPinCoordinates(pin, event.getSceneX(), event.getSceneY())
        );
        //event for releasing the pin once it has started moving.
        pin.setOnMouseReleased(event ->{
            if (event.isStillSincePress()){
                observableList.remove((int)mapPoints.get(pin));
            }else{
                createWayPoint(event.getSceneX(), event.getSceneY(), pin);
            }
            drawUpdate();
        });
    }

    /**
     * Creating a new waypoint with its new coordinates.
     * This method is used in two cases, when a new pin is created,
     * we take only the x and y coordinates, it takes a null pin.
     * The other case is when it will intend to modify an existing pin in the events,
     * it will take a non null pin.
     * @param x the x coordinates of a given pin.
     * @param y the y coordinates of a given pin.
     * @param pin will take a pin if it already exists.
     */
    private void createWayPoint(double x, double y, Group pin){
        final double SEARCH_DISTANCE = 500;
        PointCh actualPointCh = mapProperty.get().pointAt(x, y).toPointCh();
        int nodePoint = graph.nodeClosestTo(actualPointCh, SEARCH_DISTANCE);
        //if there is no closest node.
        if(nodePoint == -1) {
            consumer.accept("Aucune route à proximité !");
        }else {
            //setting the new Waypoint that is on an existing route.
            PointCh newPointCh = graph.nodePoint(nodePoint);
            Waypoint closestWaypoint = new Waypoint(newPointCh, nodePoint);

            //if there is already a point.
            if(pin == null){
                observableList.add(closestWaypoint);
            }else{
                //setting the given pin of an existing waypoint.
                observableList.set(mapPoints.get(pin), closestWaypoint);
                PointWebMercator pointOnMap = PointWebMercator.ofPointCh(newPointCh);
                setPinCoordinates(pin, mapProperty.get().viewX(pointOnMap), mapProperty.get().viewY(pointOnMap));
            }
        }
    }

    /**
     * setting the coordinates for the pin.
     * @param pin for a given pin.
     * @param xCoordinate x coordinate in the map.
     * @param yCoordinate y coordinate in the map.
     */
    private void setPinCoordinates(Group pin, double xCoordinate, double yCoordinate){
        pin.setLayoutX(xCoordinate);
        pin.setLayoutY(yCoordinate);
    }
}
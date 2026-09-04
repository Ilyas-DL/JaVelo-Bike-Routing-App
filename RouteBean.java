package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.MultiRoute;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all the different routes and their segments on map.
 * @author Ilyas Hawazine (326815).
 */
public final class RouteBean {
    private final RouteComputer routeComputer;
    private final ObservableList<Waypoint> waypointObservableList;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<Route> routeProperty;
    private final ObjectProperty<ElevationProfile> elevationProfileProperty;
    private final CacheMemory<Nodes, Route> mappingNodes = new CacheMemory<>(20);

    /**
     * Constructor that takes routeComputer to generate all the
     * routes on map.
     * @param routeComputer given for the map.
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        this.waypointObservableList = FXCollections.observableList(new ArrayList<>());
        this.highlightedPosition = new SimpleDoubleProperty(0d);
        this.routeProperty = new SimpleObjectProperty<>();
        this.elevationProfileProperty = new SimpleObjectProperty<>();
        this.waypointObservableList.addListener((ListChangeListener<Waypoint>) c -> updateList());
    }

    /**
     * Method that will be called every time there is a modification
     * in list that contains all the routes.
     * Will check several conditions on the map before adding in the list.
     */
    private void updateList(){
        int startNode, endNode;
        List<Route> listAllRoutes = new ArrayList<>();

        if(waypointObservableList.size() > 1){
            for (int i = 0; i < waypointObservableList.size() - 1; i++) {
                //Getting the actual and previous nodes if the waypoints.
                startNode = waypointObservableList.get(i).closestNodeId();
                endNode = waypointObservableList.get(i + 1).closestNodeId();

                //If the route doesn't exist in these specific nodes, we create it.
                if(mappingNodes.get(new Nodes(startNode, endNode)) == null) {
                    Route actualRoute = null;
                    if(startNode != endNode) actualRoute = routeComputer.bestRouteBetween(startNode, endNode);
                    //if it finds a route between the two segment we add it to the list.
                    if(actualRoute != null) {
                        mappingNodes.put(new Nodes(startNode, endNode), actualRoute);
                        listAllRoutes.add(actualRoute);
                        //if not, we set routeProperty at null for non-existing path.
                    }else{
                        routeProperty.set(null);
                    }
                    //if the route already exists, we add it to the list of all the routes.
                }else{
                    listAllRoutes.add(mappingNodes.get(new Nodes(startNode, endNode)));
                }
            }
            //will set the route  and elevationProfile on the actual entire segment route
            //if and only if there is already at least one segment.
            if(listAllRoutes.size() > 0){
                Route actualRoute = new MultiRoute(listAllRoutes);
                setHighlightedPosition(0d);
                routeProperty.set(actualRoute);
                elevationProfileProperty.set(ElevationProfileComputer.elevationProfile(actualRoute,5));
            }
        }
        //set the route null if there is no segment of the routes.
        if(listAllRoutes.size() == 0){
            routeProperty.set(null);
        }
    }

    /**
     * Allows to get the index to avoid
     * the infinite loops of itineraries.
     * @param position the position given.
     * @return the index of the right nodeId on the route.
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = routeProperty.get().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 =  waypointObservableList.get(i).closestNodeId();
            int n2 = waypointObservableList.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    /**
     * Returns DoubleProperty of the highlightedPosition.
     * @return DoubleProperty of the highlightedPosition.
     */
    public DoubleProperty highlightedPositionProperty(){
        return highlightedPosition;
    }

    /**
     * Returns the value in double of the highlightedPosition.
     * @return the value in double of the highlightedPosition.
     */
    public double highlightedPosition(){
        return highlightedPosition.doubleValue();
    }

    /**
     * Will set the value of the highlightedPosition property.
     * @param position takes the parameter that will set the highlightedPosition property.
     */
    public void setHighlightedPosition(double position){
        highlightedPosition.set(position);
    }

    /**
     * Returns the observableList of all waypoints.
     * @return the observableList of all waypoints.
     */
    public ObservableList<Waypoint> getWaypointList(){
        return waypointObservableList;
    }

    /**
     * Returns the objectProperty of the Route.
     * @return the objectProperty of the Route.
     */
    public ReadOnlyObjectProperty<Route> getRouteProperty(){
        return routeProperty;
    }

    /**
     * Returns the Elevation Profile property.
     * @return the Elevation Profile property.
     */
    public ObjectProperty<ElevationProfile> getElevationProfileProperty(){
        return elevationProfileProperty;
    }

    /**
     * Returns the route property.
     * @return the route property.
     */
    public Route getRoute(){
        return  routeProperty.get();
    }

    /**
     * record that stores the nodes in one entity.
     */
    private record Nodes(int startNode, int endNode){}
}
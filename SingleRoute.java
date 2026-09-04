package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that takes as input a list Of edges to form an itinerary for a SingleRoute.
 * @author Ilyas Hawazine (326815).
 */
public final class SingleRoute implements Route{
    private final List<Edge> listEdges;
    private final double[] tableEdgesLength;
    private final List<PointCh> allPoints = new ArrayList<>();

    /**
     * Public constructor that takes list of edges and builds
     * a table that gathers all the lengths summing one after another.
     * @param edges list of edges that will form the route.
     */
    public SingleRoute(List<Edge> edges){
        Preconditions.checkArgument(edges.size() > 0);
        listEdges = List.copyOf(edges);
        tableEdgesLength = new double[listEdges.size() + 1];
        tableEdgesLength[0] = 0;
        for (int i = 1; i < listEdges.size() + 1; i++) {
            tableEdgesLength[i] = listEdges.get(i - 1).length() + tableEdgesLength[i - 1];
        }
        computeAllPoints();
    }

    /**
     * It will return the index of the route, because this class works only
     * on one route, the index will always return 0.
     * @param position the position on the itinerary in meters.
     * @return 0 because there is only one route in this class.
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Returns the total length of the itinerary.
     * @return the total length in meters of the itinerary.
     */
    @Override
    public double length() {
        return tableEdgesLength[listEdges.size()];
    }

    /**
     * Returns the list of all the edges that make up the {@link SingleRoute}.
     * @return the list of all edges.
     */
    @Override
    public List<Edge> edges() {
        return listEdges;
    }

    /**
     * Returns all the points that are at the extremities of every edge
     * that makes up the single route.
     * @return all edges' possible points.
     */
    @Override
    public List<PointCh> points() {
        return new ArrayList<>(allPoints);
    }

    /**
     * Returns the point that turns out to be at the given position
     * on the SingleRoute.
     * @param position the position on the itinerary.
     * @return the point that is at the "position" distance from the first edge.
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position,length());
        int index = binarySearchIndex(position);
        double edgePosition = position - tableEdgesLength[index];
        return listEdges.get(index).pointAt(edgePosition);
    }

    /**
     * Returns the elevation (height) of the point that turns out to be at the given position
     * on the SingleRoute.
     * @param position the position on the itinerary.
     * @return the height on the position given.
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position,length());
        int index = binarySearchIndex(position);
        double edgePosition = position - tableEdgesLength[index];
        return listEdges.get(index).elevationAt(edgePosition);
    }

    /**
     * Returns the index of the closest node on the itinerary
     * with comparison to the given position.
     * @param position the position on the itinerary.
     * @return the index of the closest node.
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position,length());
        int index = binarySearchIndex(position);
        double edgePosition = position - tableEdgesLength[index];
        Edge actualEdge = listEdges.get(index);
        return edgePosition > actualEdge.length() / 2 ? actualEdge.toNodeId() : actualEdge.fromNodeId();
    }

    /**
     * Returns a RoutePoint that contains the closest point, its position on the
     * itinerary and the distanceReference with comparison to the given point.
     * @param point where to search for the closest point on the itinerary.
     * @return the RoutePoint of the closest point on the itinerary with comparison to the given point.
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double totalDistance, distanceOnEdge, distanceToReference;
        RoutePoint finalRoutePoint = RoutePoint.NONE;
        PointCh closestPoint;
        Edge edge;
        for (int i = 0; i < listEdges.size() ; i++) {
            edge = edges().get(i);
            distanceOnEdge  = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            closestPoint = edge.pointAt(distanceOnEdge);
            distanceToReference = point.distanceTo(closestPoint);
            totalDistance = distanceOnEdge + tableEdgesLength[i];
            finalRoutePoint = finalRoutePoint.min(closestPoint, totalDistance, distanceToReference);
        }
        return finalRoutePoint;
    }

    /**
     * Takes the position distance and gives back the index of the edge,
     * depending at which distance the position lies in the edges of tableEdgesLength.
     * @param position the position on the itinerary.
     * @return the index of the edge where the position lands.
     */
    private int binarySearchIndex(double position){
        int index = Arrays.binarySearch(tableEdgesLength, position);
        if(index<0) index = -(index + 2);
        return index == listEdges.size() ? index -1 : index;
    }

    /**
     * computing all the point from the list and adding them.
     */
    private void computeAllPoints(){
        allPoints.add(listEdges.get(0).fromPoint());
        for (Edge allEdge : listEdges) {
            allPoints.add(allEdge.toPoint());
        }
    }
}
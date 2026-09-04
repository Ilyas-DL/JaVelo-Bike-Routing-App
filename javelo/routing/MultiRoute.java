package ch.epfl.javelo.routing;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that takes as input a list Of several routes to form an itinerary for a single route, the route may be
 * a SingleRoute or a MultiRoute.
 * @author Ilyas Hawazine (326815).
 */
public final class MultiRoute implements Route{
    private final List<Route> segmentsList;

    /**
     * Public constructor that takes list of routes (SingleRoutes or MultiRoutes).
     * @param segments the list of routes.
     * @throws IllegalArgumentException if the list is empty.
     */
    public MultiRoute(List<Route> segments){
        Preconditions.checkArgument(segments.size()>0);
        this.segmentsList = List.copyOf(segments);
    }

    /**
     * It will return the index of the SingleRoute} on which the position lands.
     * @param position the position on the itinerary in meters.
     * @return the index of the SingleRoute.
     */
    @Override
    public int indexOfSegmentAt(double position) {
        position = Math2.clamp(0,position,length());
        int index = 0;
        int totalDistanceOfPosition = 0;
        double actualRouteLength;

        for (Route route : segmentsList){
            actualRouteLength = route.length();
            if(totalDistanceOfPosition + actualRouteLength < position){
                index += route.indexOfSegmentAt(actualRouteLength) + 1;
                totalDistanceOfPosition += actualRouteLength;
            }else{
                index += route.indexOfSegmentAt(position - totalDistanceOfPosition);
                break;
            }
        }
       return index;
    }

    /**
     * Returns the total length of the itinerary of all routes.
     * @return the total length of the itinerary.
     */
    @Override
    public double length() {
        double length = 0;
        for (Route route : segmentsList) {
            length += route.length();
        }
        return length;
    }

    /**
     * Returns the list of all the edges that make up the route.
     * @return the list of all the edges.
     */
    @Override
    public List<Edge> edges() {
        List<Edge> allEdges = new ArrayList<>();
        for (Route route : segmentsList) {
            allEdges.addAll(route.edges());
        }
        return allEdges;
    }

    /**
     * Returns all the points that are at the extremities of every edge
     * that makes every SingleRoute of the  whole route.
     * @return all the points of every edge.
     */
    @Override
    public List<PointCh> points() {
        List<PointCh> allPointCh = new ArrayList<>();
        List<PointCh> actualPoints;
        for (Route route : segmentsList) {
            actualPoints = route.points();
            allPointCh.addAll(actualPoints);
            allPointCh.remove(actualPoints.get(actualPoints.size() - 1));
        }
        List<PointCh> lastPoint = segmentsList
                .get(segmentsList.size() - 1)
                .points();
        allPointCh.add(lastPoint.get(lastPoint.size() - 1));
        return allPointCh;
    }

    /**
     * Returns the point that turns out to be at the given position
     * on the whole route.
     * @param position the position on the itinerary.
     * @return the point that is at the "position" distance from the beginning of the route.
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        PointCh pointChAt = null;
        Route route;
        double actualRouteLength;
        for (int i = 0; i < segmentsList.size(); i++) {
            route = segmentsList.get(i);
            actualRouteLength = route.length();
            if(actualRouteLength >= position) return route.pointAt(position);
            else {
                position = position - actualRouteLength;
                pointChAt = segmentsList.get(i + 1).pointAt(position);
            }
        }
        return pointChAt;
    }

    /**
     * Returns the elevation "height" of the point that turns out to be at the given position
     * on the route.
     * @param position the position on the itinerary
     * @return the height on the position given
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position,length());
        Route route;
        double height = 0;
        double actualRouteLength;

        for (int i = 0; i < segmentsList.size(); i++) {

            route = segmentsList.get(i);
            actualRouteLength = route.length();
            if(actualRouteLength >= position) return route.elevationAt(position);
            else {
                position = position - actualRouteLength;
                height = segmentsList.get(i + 1).elevationAt(position);
            }
        }
        return height;
    }

    /**
     * Returns the index of the closest node on the entire route
     * with comparison to the given position.
     * @param position the position on the itinerary.
     * @return the index of the closest node.
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position,length());
        int indexNode = 0;
        double actualRouteLength;
        for (Route route : segmentsList) {
            actualRouteLength = route.length();
            if (actualRouteLength >= position) return route.nodeClosestTo(position);
            else{
                position = position - actualRouteLength;
                indexNode = indexNode + route.nodeClosestTo(position);
            }
        }
        return indexNode;
    }

    /**
     * Returns a RoutePoint that contains the closest point, its position on the
     * itinerary (entire route) and the distanceReference with comparison to the given point.
     * @param point where to search for the closest point on the itinerary.
     * @return the RoutePoint of the closest point on the itinerary with comparison to the given point.
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint routePoint = RoutePoint.NONE;
        double totalPosition = 0;
        for (Route route : segmentsList) {
            routePoint = routePoint.min(route
                    .pointClosestTo(point)
                    .withPositionShiftedBy(totalPosition));
            totalPosition += route.length();
        }
        return routePoint;
    }
}

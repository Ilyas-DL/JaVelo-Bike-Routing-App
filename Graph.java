package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Class that gathers all the information about GraphNodes,GraphSectors,GraphEdges
 * and AttributeSet within a list of AttributeSets.
 * @author Ilyas Hawazine (326815).
 */
public final class Graph {
    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Generate a graph that manage all the information about the nodes, the sectors and the edges.
     * @param nodes takes a GraphNodes.
     * @param sectors takes a GraphSectors.
     * @param edges takes a GraphEdges.
     * @param attributeSets takes a list of AttributeSet.
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets){
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * Gathers methods for extracting information of a file that is made up of :
     * attributes,edges,elevations,nodes,profile_ids and sectors.
     * @param basePath the file that will be extracted from.
     * @throws IOException if extraction goes wrong.
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        ByteBuffer edges = extractFiles(basePath,"edges.bin");
        ShortBuffer elevations = extractFiles(basePath,"elevations.bin").asShortBuffer();
        IntBuffer nodes = extractFiles(basePath,"nodes.bin").asIntBuffer();
        IntBuffer  profile_ids = extractFiles(basePath,"profile_ids.bin").asIntBuffer();
        ByteBuffer sectors = extractFiles(basePath,"sectors.bin");
        LongBuffer attributes = extractFiles(basePath,"attributes.bin").asLongBuffer();

        List<AttributeSet> attributesList = new ArrayList<>();
        for(int a = 0; a < attributes.capacity(); a++){
            attributesList.add(new AttributeSet(attributes.get(a)));
        }

        GraphNodes graphNodes = new GraphNodes(nodes);
        GraphSectors graphSectors = new GraphSectors(sectors);
        GraphEdges graphEdges = new GraphEdges(edges,profile_ids,elevations);

        return new Graph(graphNodes,graphSectors,graphEdges,attributesList);
    }

    /**
     * Returns the number of nodes of the attribute nodes.
     * @return the number of nodes.
     */
    public int nodeCount(){
        return nodes.count();
    }

    /**
     * Returns PointCh of the required node (using E and N coordinates).
     * @param nodeId the index of the required node.
     * @return returns a PointCh of the required node.
     */
    public PointCh nodePoint(int nodeId){
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Returns the number of leaving edges leaving from this nodeId.
     * @param nodeId the index of the node to count the edges going out.
     * @return the number of leaving edges.
     */
    public int nodeOutDegree(int nodeId){
        return nodes.outDegree(nodeId);
    }

    /**
     * Returns the edgeId of from the index of an edge in a node.
     * @param nodeId Index of the node.
     * @param edgeIndex every node might have several edges, edgeIndex is the position of
     * the required edge.
     * @return the edgeId.
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex){
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * This is an algorithm that will gather all the nodes from the sector of the point,
     * then compare all of the extracted nodes to return the index of the closest node of the point.
     * @param point the point from which we will make all the comparisons.
     * @param searchDistance the area limit within the sector that will be searched in.
     * @return the index of the closest index node to the given point.
     */
    public int nodeClosestTo(PointCh point, double searchDistance){
        double minimalDistance = Math.pow(searchDistance,2);
        double actualDistance;
        int closestNodeIndex = -1;
        for(GraphSectors.Sector sector : sectors.sectorsInArea(point, searchDistance)) {
            for(int a = sector.startNodeId(); a < sector.endNodeId() - 1; a++){
                actualDistance = point.squaredDistanceTo(nodePoint(a));
                if(actualDistance <= minimalDistance){
                    minimalDistance = actualDistance;
                    closestNodeIndex = a;
                }
            }
        }
        return closestNodeIndex;
    }

    /**
     * Gets the id of the target of an edge from edges attribute.
     * @param edgeId the edge to get the target.
     * @return the id of the target of the edges attribute.
     */
    public int edgeTargetNodeId(int edgeId){
        return edges.targetNodeId(edgeId);
    }

    /**
     * Checks if this edge is going the other way around from edges attribute.
     * @param edgeId the edge to check.
     * @return true(boolean) if the edge is going in the other sens, otherwise false.
     */
    public boolean edgeIsInverted(int edgeId){
        return edges.isInverted(edgeId);
    }

    /**
     * Returns the AttributeSet from the list for the edgeId asked.
     * @param edgeId the index of AttributeSet from the list.
     * @return AttributeSet of the required index.
     */
    public AttributeSet edgeAttributes(int edgeId){
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Returns the length of the edge for the edgeId.
     * @param edgeId the edge to get the length.
     * @return the length of the edge.
     */
    public double edgeLength(int edgeId){
        return edges.length(edgeId);
    }

    /**
     * Gets the (positive) elevation of an edge from edges attribute.
     * @param edgeId the index of the required edge to get the elevation.
     * @return the elevation of the required edge.
     */
    public double edgeElevationGain(int edgeId){
        return edges.elevationGain(edgeId);
    }

    /**
     * Returns a sampled operator that will be accessed from the samples that take an edgeId index
     * from the edges attribute. Otherwise, it will return a constant function if there is no profile.
     * @param edgeId the index of the required edge.
     * @return sampled operator of the profile samples taken from the edges attribute,
     * or a constant function if there there is no profile.
     */
    public DoubleUnaryOperator edgeProfile(int edgeId){
        float [] edgesSamples = edges.profileSamples(edgeId);
        return edges.hasProfile(edgeId) ? Functions.sampled(edgesSamples, edgeLength(edgeId)) :
                Functions.constant(Double.NaN);
    }

    /**
     * Method for extracting the files from a given path.
     * @param basePath where to extract the files.
     * @param fileName the name of the file.
     * @return extracted ByteBuffer file.
     */
    private static ByteBuffer extractFiles(Path basePath, String fileName) throws IOException{
        ByteBuffer actualFile;
        try(FileChannel channel = FileChannel.open(basePath.resolve(fileName))){
            actualFile = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
        return actualFile;
    }
}
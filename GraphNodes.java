package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;

/**
 * This class takes as input an IntBuffer to generate all the graphNodes.
 * @author Ilyas Hawazine (326815).
 */
public record GraphNodes(IntBuffer buffer) {
    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;
    private static final int START_BIT = 28;

    /**
     * Returns the number of nodes dividing by 3 the capacity of the buffer. Because every node has 3 attributes that
     * characterize it.
     * @return returns the total number of nodes of the given buffer.
     */
    public int count(){
        return buffer.capacity() / NODE_INTS;
    }

    /**
     * Returns the bits that represent the E coordinate of the node at nodeId position.
     * @param nodeId the index of the node.
     * @return a double which represents the E coordinate.
     */
    public double nodeE(int nodeId){
        return Q28_4.asDouble(buffer.get(NODE_INTS * nodeId + OFFSET_E));
    }

    /**
     * Returns the bits that represents the N coordinate of the node at nodeId position.
     * @param nodeId the index of the node.
     * @return a double which represents the N coordinate.
     */
    public double nodeN(int nodeId){
        return Q28_4.asDouble(buffer.get(NODE_INTS * nodeId + OFFSET_N));
    }

    /**
     * Returns the number of leaving ridges of the node at nodeId position.
     * @param nodeId the index of the node.
     * @return the number of leaving ridges.
     */
    public int outDegree(int nodeId){
        return Bits.extractUnsigned(buffer.get((NODE_INTS * nodeId) + OFFSET_OUT_EDGES), START_BIT, Integer.SIZE - START_BIT);
    }

    /**
     * Returns the index of the ridge of the node at nodeId position in the buffer.
     * @param nodeId the node from which the ridge will be extracted.
     * @param edgeIndex every node might have several ridges, edgeIndex is the position of
     *                  the required ridge.
     * @return the id of the ridge.
     */
    public int edgeId(int nodeId, int edgeIndex){
        assert (0 <= edgeIndex) && (edgeIndex < outDegree(nodeId));
        return  Bits.extractUnsigned(buffer.get(NODE_INTS * nodeId + OFFSET_OUT_EDGES), 0, START_BIT) + edgeIndex;
    }
}
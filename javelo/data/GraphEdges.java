package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * This class represents all the edges of a graph inside numerous buffers that are :
 * ByteBuffer, IntBuffer and ShortBuffer.
 * @author Eden Kahane (346481).
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {
    private final static int OFFSET_TARGET_ID = 0;
    private final static int OFFSET_LENGTH = OFFSET_TARGET_ID + Integer.BYTES;
    private final static int OFFSET_STEEP = OFFSET_LENGTH + Short.BYTES;
    private final static int OFFSET_IDENTITY = OFFSET_STEEP + Short.BYTES;
    private final static int EDGES_BYTES = OFFSET_IDENTITY + Short.BYTES;
    private final static int Q28_4_OFFSET = -4;

    /**
     * Checks if this edge is going the other way around.
     * @param edgeId the edge to check.
     * @return true if the edge is going in the other sens, otherwise false.
     */
    public boolean isInverted(int edgeId){
        return edgesBuffer.getInt(EDGES_BYTES * edgeId + OFFSET_TARGET_ID) < 0;
    }

    /**
     * Gets the id of the target of an edge.
     * @param edgeId the edge from which the target will be extracted.
     * @return the id of the target.
     */
    public int targetNodeId(int edgeId){
        int edgesBufferInt = edgesBuffer.getInt(EDGES_BYTES * edgeId + OFFSET_TARGET_ID);
        int i = Bits.extractSigned(edgesBufferInt,0, Integer.SIZE);
        return isInverted(edgeId) ? ~i : i;
    }

    /**
     * Gets the length of an edge.
     * @param edgeId the edge from which the length will be got.
     * @return the length of the edge.
     */
     public double length(int edgeId){
         short edgesBufferShortened = edgesBuffer.getShort(EDGES_BYTES * edgeId + OFFSET_LENGTH);
         return Math.scalb((double) Short.toUnsignedInt(edgesBufferShortened), Q28_4_OFFSET);
     }

    /**
     * Gets the (positive) elevation of an edge.
     * @param edgeId the edge from which the elevation will be got.
     * @return the elevation of the edge
     */
     public double elevationGain(int edgeId){
         short edgesBufferShortened = edgesBuffer.getShort(EDGES_BYTES * edgeId + OFFSET_STEEP);
         return Math.scalb((double) Short.toUnsignedInt(edgesBufferShortened), Q28_4_OFFSET);
     }

    /**
     * Gets the index of the attributes from the edges.
     * @param edgeId the edge from which the   attributes index will be got.
     * @return an index.
     */
    public int attributesIndex(int edgeId){
        short edgesBufferShortened = edgesBuffer.getShort(EDGES_BYTES * edgeId + OFFSET_IDENTITY);
        return Short.toUnsignedInt(edgesBufferShortened);
    }

    /**
     * Checks if the edge has a profile (can't be of type 0 edge without information)
     * @param edgeId the edge to check.
     * @return true if it has a profile, false otherwise.
     */
     public boolean hasProfile(int edgeId){
        return Bits.extractUnsigned(profileIds.get(edgeId), Integer.SIZE - 2,2) != 0;
     }

    /**
     * Returns all the samples associated with this edge depending on the edge type.
     * This take into account the different compression type used for edges.
     * The distance between two samples can never be more than 2m.
     * @param edgeId the edge to get the samples.
     * @return an array of (float) samples, an empty array if there is no profile for this edge.
     */
     public float[] profileSamples(int edgeId) {
       int originalIndex = Bits.extractUnsigned(profileIds.get(edgeId),0, Integer.SIZE - 2);
       int sampleAmount = 1 + Math2.ceilDiv(edgesBuffer.getShort(EDGES_BYTES * edgeId + OFFSET_LENGTH), 2 << 4);
       int profileType = Bits.extractUnsigned(profileIds.get(edgeId),30,2);

       if(profileType == 0) return new float[0];

       float[] result = new float[sampleAmount];
       boolean isInverted = isInverted(edgeId);

       //Decide where to start (end or beginning)
       int firstIndex = isInverted ? sampleAmount - 1 : 0;

       //Takes the first value
       result[firstIndex] = Math.scalb(Short.toUnsignedInt(elevations.get(originalIndex)), Q28_4_OFFSET);

       if(profileType == 1) {
           for(int index= 1; index<sampleAmount;index++) {
               //Select the current index depending on the sens of the edge
               int newIndex = isInverted ? sampleAmount - 1 - index : index;
               result[newIndex] = readType1Value(originalIndex, index);
           }
       } else {

           int dataPart = profileType == 2 ? 2 : 4;
           for(int index=1; index<sampleAmount;index++) {
               //Select the current and previous index depending on the sens of the edge
               int newIndex = isInverted ? sampleAmount - 1 - index : index;
               int previousIndex = isInverted ? sampleAmount - index : index - 1;
               result[newIndex] = readType23Value(originalIndex, index, result[previousIndex], dataPart);
           }
       }

       return result;
     }

  /**
   * Read elevation at an index from and edge of type 1
   * @param originalIndex the index of the first elevation in elevations
   * @param sampleIndex the current index of elevation looked at
   * @return the elevation at this index
   */
  private float readType1Value(int originalIndex, int sampleIndex) {
      //Extract 1 value per Byte after the one at originalIndex
      return Math.scalb(Short.toUnsignedInt(elevations.get(originalIndex + sampleIndex)), Q28_4_OFFSET);
  }

    /**
     * Read elevation at an index from and edge of type 2 or 3
     * @param originalIndex the index of the first elevation in elevations
     * @param sampleIndex the current index of elevation looked at
     * @param lastValue the previous elevation
     * @param dataPart how many data there is to be extracted in each short
     * @return the elevation at this index
     */
  private float readType23Value(int originalIndex, int sampleIndex, double lastValue, int dataPart) {
      //Divide each short in dataPart parts
      int shortIndex = Math2.ceilDiv(sampleIndex, dataPart);
      int indexInShort = dataPart - (sampleIndex % dataPart);
      if(indexInShort == dataPart) indexInShort = 0;

      //Depending on the position of the index and the byte, take 1/dataPart of the short as the difference of elevation from the previous value
      int length = Short.SIZE / dataPart;
      int data = Bits.extractSigned(elevations.get(originalIndex + shortIndex), indexInShort * length, length);

      return (float)  lastValue + Math.scalb(data, Q28_4_OFFSET);
  }
}

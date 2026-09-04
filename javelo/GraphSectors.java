package ch.epfl.javelo.data;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class stores all the sectors of the graph.
 * @author Eden Kahane (346481).
 */
public record GraphSectors(ByteBuffer buffer) {
  private static final int OFFSET_FIRST_NODE_BYTES = 0;
  private static final int OFFSET_NODES_NUMBERS_BYTES = OFFSET_FIRST_NODE_BYTES + Integer.BYTES;
  private static final int SECTOR_BYTES = OFFSET_NODES_NUMBERS_BYTES + Short.BYTES;

  private static final int SECTORS_BY_ROW = 128;
  private static final double SECTOR_WIDTH = SwissBounds.WIDTH / SECTORS_BY_ROW;
  private static final double SECTOR_HEIGHT = SwissBounds.HEIGHT / SECTORS_BY_ROW;

  /**
   * Find all the sectors in a box around a PointCh.
   * @param center the point around whom will be searched the sectors.
   * @param distance the radius of the box around the center.
   * @return a list of sectors in the box created by the center and the distance.
   */
  public List<Sector> sectorsInArea(PointCh center, double distance) {
    int xMin = Math.max((int) ((center.e() - distance - SwissBounds.MIN_E) / SECTOR_WIDTH), 0);
    int xMax = Math.min((int) ((center.e() + distance - SwissBounds.MIN_E) / SECTOR_WIDTH), SECTORS_BY_ROW - 1);
    int yMin = Math.max((int) ((center.n() - distance - SwissBounds.MIN_N) / SECTOR_HEIGHT), 0);
    int yMax = Math.min((int) ((center.n() + distance - SwissBounds.MIN_N) / SECTOR_HEIGHT), SECTORS_BY_ROW - 1);

    ArrayList<Sector> result = new ArrayList<>();
    for(int y = yMin; y <= yMax; y++) {
      for(int x = xMin; x <= xMax; x++) {
        int index = SECTORS_BY_ROW * y + x;
        int startNodeId = buffer.getInt(index * SECTOR_BYTES + OFFSET_FIRST_NODE_BYTES);
        int nodesAmount = Short.toUnsignedInt(buffer.getShort(index * SECTOR_BYTES + OFFSET_NODES_NUMBERS_BYTES));

        result.add(new Sector(startNodeId, startNodeId + nodesAmount));
      }
    }
    return result;
  }

  /**
   * A sector with a certain amount of nodes
   * all nodes between startNodeId and endNodeId are present in this sector
   */
  public record Sector(int startNodeId, int endNodeId) {
  }
}

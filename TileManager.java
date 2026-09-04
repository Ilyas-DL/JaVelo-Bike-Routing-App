package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

/**
 * Manage the images of tiles for the map
 * @author Eden Kahane (346481).
 */
public final class TileManager {
  private final CacheMemory<TileId, Image> cacheMemory = new CacheMemory<>(100);
  private final Path cachePath;
  private final String tileServerIp;

  /**
   * Manage the images of the tiles of the map.
   * Automatically stores them in disk or memory for
   * easier access.
   * @param cachePath path where the disk cache should be.
   * @param tileServerIp ip of the website to download the tiles from.
   */
  public TileManager(Path cachePath, String tileServerIp) {
    this.cachePath = cachePath;
    this.tileServerIp = tileServerIp;
  }

  /**
   * Return the image from cache associated with a tileId.
   * If the image is not found in the cache, it's downloaded
   * and added to the cache.
   * @param tileId the id of a tile.
   * @return the image of the tile or null if the image could not be found/downloaded.
   * @throws IOException if the image could not be found.
   */
  public Image imageForTileAt(TileId tileId) throws IOException {
    Path path = cachePath.resolve(tileId.zoomLevel() + "/" + tileId.xIndex() + "/" + tileId.yIndex() + ".png");
    Image img;

    //Try to load image from memory cache.
    img = cacheMemory.get(tileId);
    if(img != null) return img;

    //Try to load image from disk cache.
    if(path.toFile().exists()) {
      img = new Image(new FileInputStream(path.toString()));
      if (img.getWidth() == 256) {
        cacheMemory.put(tileId, img); //Add image to memory cache for faster access next time.
        return img;
      }
    }

    //Try to download image from website.
    URL u = new URL("https://" + tileServerIp + "/" + tileId.zoomLevel() + "/"
            + tileId.xIndex() +"/" + tileId.yIndex() +".png");
    URLConnection c = u.openConnection();
    c.setRequestProperty("User-Agent", "JaVelo");
    File file = path.toFile();
    file.getParentFile().mkdirs(); //Create parent folders.
    try(InputStream i = c.getInputStream()) {
      i.transferTo(new FileOutputStream(file)); //Add image to disk cache.
    }
    try(InputStream i = new FileInputStream(file)) {
      img = new Image(i);
      cacheMemory.put(tileId, img); //Add image to memory cache.
      return img;
    }
  }

  /**
   * Id for a tile.
   * @param zoomLevel the level of zoom from which the tile belongs.
   * @param xIndex the index x of the tile.
   * @param yIndex the index y of the tile.
   */
  public record TileId(int zoomLevel, int xIndex, int yIndex) {

    /**
     * Id for a tile.
     * @param zoomLevel the level of zoom from which the tile belongs.
     * @param xIndex the index x of the tile.
     * @param yIndex the index y of the tile.
     */
    public TileId {
      Preconditions.checkArgument(isValid(zoomLevel, xIndex, yIndex));
    }

    /**
     * Check if the tileId is valid for the zoomLevel
     * @param zoomLevel the zoomLevel of the tile
     * @param xIndex the xIndex of the tile
     * @param yIndex the yIndex of the tile
     * @return true is the tileId is valid, false if one of the index is outside of the zoom
     */
    public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
      return xIndex >= 0 && yIndex >= 0  && xIndex < Math.pow(2, zoomLevel) && yIndex < Math.pow(2, zoomLevel);
    }
  }
}

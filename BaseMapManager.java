package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import java.io.IOException;

/**
 * Managing the pane for
 * the visualization of the map.
 * @author Eden Kahane (346481).
 */
public final class BaseMapManager {
    private static final int TILE_SIZE = 256;

    private final TileManager tileManager;
    private final Property<MapViewParameters> mapViewParametersProperty;
    private final Property<Point2D> lastMouseCoordinate;
    private final Pane pane;
    private final Canvas canvas;
    private boolean redrawNeeded;

    /**
     * Managing the display and the interaction of
     * the map on a {@link Pane}.
     * @param tileManager a timeManager from which the tile are queried.
     * @param wayPointManager manage the interaction with a waypoint.
     * @param mapViewParametersProperty the default property of the map (such as the location).
     */
    public BaseMapManager(TileManager tileManager, WaypointsManager wayPointManager,
                          Property<MapViewParameters> mapViewParametersProperty) {
        this.tileManager = tileManager;
        this.mapViewParametersProperty = mapViewParametersProperty;
        this.lastMouseCoordinate = new SimpleObjectProperty<>(new Point2D(0, 0));
        redrawNeeded = false;

        //Registers the pane and canvas and binds canvas to pane.
        this.pane = new Pane();
        this.canvas = new Canvas();

        setEventsAndListeners(pane, canvas, lastMouseCoordinate, wayPointManager);
    }

    /**
     * Setting events on the map and listeners for different properties.
     * @param pane actual pane.
     * @param canvas actual canvas.
     * @param wayPointManager manage the interaction with a waypoint.
     * @param lastMouseCoordinate actual lastMouseCoordinate.
     */
    private void setEventsAndListeners(Pane pane,Canvas canvas,
                                       Property<Point2D> lastMouseCoordinate,WaypointsManager wayPointManager){

        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        //Redraw the map when there is a change of property
        //but only every pulse.
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            if(newS != null) {
                newS.addPreLayoutPulseListener(this::redrawIfNeeded);
                redrawOnNextPulse();
            }
        });

        //Property change triggering the redrawing of the map (after pulse)
        canvas.widthProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        mapViewParametersProperty.addListener((p, oldS, newS) -> redrawOnNextPulse());

        //Manage the dragging and replacement in the map
        pane.setOnMousePressed(event -> lastMouseCoordinate.setValue(new Point2D(event.getX(), event.getY())));
        pane.setOnMouseDragged(this::mouseDraggedEvent);

        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            scrollChange(e, zoomDelta);
        });

        pane.setOnMouseReleased(event -> {
            if(event.isStillSincePress()) wayPointManager.addWaypoint(event.getX(), event.getY());
        });
    }

    /**
     * Return the pane of the map.
     * @return a pane with all the map and its interactions.
     */
    public Pane pane() { return pane;}

    /**
     * Called after each pulse, only drawn if
     * there was a {@link #redrawOnNextPulse()} before.
     * Redraws everything in the pane and display the
     * new map.
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        final GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        Point2D topLeftCorner = mapViewParametersProperty.getValue().topLeft();

        //By how much all the tiles should be shifted to the left.
        double xShift = topLeftCorner.getX() % TILE_SIZE;
        double yShift = topLeftCorner.getY() % TILE_SIZE;

        //Iterate over all the coordinates of the tiles in the screen.
        for(double x = 0; x < canvas.widthProperty().doubleValue() + TILE_SIZE; x += TILE_SIZE) {
            for(double y = 0; y <= canvas.heightProperty().doubleValue() + TILE_SIZE; y += TILE_SIZE) {

                //Find all the tile id based on the coordinate.
                TileManager.TileId tileId = new TileManager.TileId(
                        mapViewParametersProperty.getValue().zoomLevel(),
                        (int) Math.floor((topLeftCorner.getX() + x) / TILE_SIZE),
                        (int) Math.floor((topLeftCorner.getY() + y) / TILE_SIZE));

                try {
                    Image image = tileManager.imageForTileAt(tileId);
                    //Draw and shift tile to the left.
                    graphicsContext.drawImage(image, x - xShift, y - yShift);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * This method is called to redraw the map.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Event updated when the mouse is dragged.
     * @param event event information.
     */
    private void mouseDraggedEvent(MouseEvent event) {
        if(!event.isStillSincePress()) {
            //Gets by how much the mouse has shifted since last mouseDraggedEvent.
            Point2D deltaPoint = lastMouseCoordinate.getValue().subtract(event.getX(), event.getY());
            //Defines the new coordinates based on the delta.
            mapViewParametersProperty.setValue(
                    mapViewParametersProperty.getValue().addXY(deltaPoint.getX(), deltaPoint.getY()));

            //Sets again the last mouse coordinate for the next mouseDraggedEvent.
            lastMouseCoordinate.setValue(new Point2D(event.getX(), event.getY()));
        }
    }

    /**
     * Event updated when the mouse is scrolled.
     * @param event event information.
     */
    private void scrollChange(ScrollEvent event, int scrollDelta) {
        int newZoomLevel = mapViewParametersProperty.getValue().zoomLevel() + scrollDelta;
        if(newZoomLevel < 8 || newZoomLevel > 19) return;

        PointWebMercator mouseCoordinate = mapViewParametersProperty.getValue().pointAt(event.getX(), event.getY());

        MapViewParameters newMapViewParameters = new MapViewParameters(
                newZoomLevel,
                //Finds new coordinate with new zoomLevel in the top left corner.
                 mouseCoordinate.xAtZoomLevel(newZoomLevel) - event.getX(),
                mouseCoordinate.yAtZoomLevel(newZoomLevel) - event.getY()
        );
        mapViewParametersProperty.setValue(newMapViewParameters);
    }
}

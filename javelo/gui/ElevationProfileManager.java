package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

/**
 * Class that manages the profile of all the elevations on map.
 * @author Ilyas Hawazine (326815)
 * @author Eden Kahane (346481)
 */
public class ElevationProfileManager {

    private static final Insets RECTANGLE_INSETS = new Insets(10, 10, 20, 40);
    private static final Font FONT_TEXT = Font.font("Avenir", 10);
    private static final int[] POS_STEPS = { 1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000 };
    private static final int[] ELE_STEPS = { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };
    private static final int STEP_BETWEEN_POS = 25;
    private static final int STEP_BETWEEN_ELE = 50;

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty;
    private final ReadOnlyDoubleProperty positionProperty;
    private final DoubleProperty mousePositionOnProfileProperty;
    private final ObjectProperty<Rectangle2D> displayRectangleProperty;
    private final ObjectProperty<Transform> screenToWorld;
    private final ObjectProperty<Transform> worldToScreen;
    private final BorderPane borderPane;

    /**
     * Public constructor for managing the profiles displaying the elevation of the profile
     * @param elevationProfileProperty takes the elevation profile property only to be read.
     * @param positionProperty takes the position property only to be read.
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty,
                                   ReadOnlyDoubleProperty positionProperty) {
        this.elevationProfileProperty = elevationProfileProperty;
        this.positionProperty = positionProperty;

        this.mousePositionOnProfileProperty = new SimpleDoubleProperty();
        this.displayRectangleProperty = new SimpleObjectProperty<>(new Rectangle2D(0, 0, 0, 0));
        this.screenToWorld = new SimpleObjectProperty<>(new Affine());
        this.worldToScreen = new SimpleObjectProperty<>(new Affine());

        this.borderPane = new BorderPane();
        this.borderPane.getStylesheets().add("elevation_profile.css");

        SetEventsListenersGraphics();
    }

    /**
     * Setting events on the map and listeners to the properties and a bind on
     * rectangleDisplayProperty. Also setting the hierarchy for graphics.
     */
    private void SetEventsListenersGraphics(){
        //setting the pane and the roots for graphics.
        Pane centerPane = new Pane();
        borderPane.setCenter(centerPane);
        BorderPane.setMargin(centerPane, RECTANGLE_INSETS);

        Group groupText = new Group();

        Polygon polygon = new Polygon();
        polygon.setId("profile");

        Path path = new Path();
        path.setId("grid");

        centerPane.getChildren().setAll(groupText, path, polygon);

        VBox bottomVbox = new VBox();
        bottomVbox.setId("profile_data");
        borderPane.setBottom(bottomVbox);

        Text vBoxText = new Text();
        vBoxText.setFont(FONT_TEXT);
        bottomVbox.getChildren().add(vBoxText);

        Line line = new Line();
        settingLine(line);

        centerPane.getChildren().setAll(groupText, path, polygon,line);

        //setting the bind.
        displayRectangleProperty.bind(Bindings.createObjectBinding(() ->
                        new Rectangle2D(
                                0,
                                0,
                                Math.max(centerPane.getWidth(), 0),
                                Math.max(centerPane.getHeight(), 0)),
                centerPane.widthProperty(),
                centerPane.heightProperty()
        ));

        //setting listeners.
        elevationProfileProperty.addListener((observable, oldValue, newValue) -> {
            updateTextElevationInformation(vBoxText);
            redraw(path, groupText, polygon);
        });

        displayRectangleProperty.addListener((observable, oldValue, newValue) -> redraw(path, groupText, polygon));

        //setting events.
        centerPane.setOnMouseMoved(event -> {
            Point2D point2D = screenToWorld.get().transform(event.getX(), event.getY());
            mousePositionOnProfileProperty.set(point2D.getX());
        });
        centerPane.setOnMouseExited(event -> mousePositionOnProfileProperty.set(Double.NaN));
    }

    /**
     * Compute all the transform and redraw everything according to the new transform
     * @param grid Shape used for the grid
     * @param groupText Group for all the axesLabel
     * @param polygon Shape used to display the elevation
     */
    private void redraw(Path grid, Group groupText, Polygon polygon) {
        if(displayRectangleProperty.get().getWidth() > 0 && displayRectangleProperty.get().getHeight() > 0) {
            computeTransform();
            drawGrid(grid, groupText);
            drawElevation(polygon);
        }
    }

    /**
     * Setting all the different bindings of the line (x and y property)
     * as well as the end a visible property to display.
     * @param line that will be bound.
     */
    private void settingLine(Line line){
        line.layoutXProperty().bind(Bindings.createDoubleBinding( () ->
                worldToScreen.get().transform(positionProperty.doubleValue(), 0).getX(),
                positionProperty));
        line.startYProperty().bind(Bindings.select(displayRectangleProperty, "minY"));
        line.endYProperty().bind(Bindings.select(displayRectangleProperty, "maxY"));
        line.visibleProperty().bind(positionProperty.greaterThanOrEqualTo(0));
    }

    /**
     *
     */
    private void computeTransform() {
        ElevationProfile elevationProfile = elevationProfileProperty.get();
        Affine screenToWorldTransform = new Affine();
        //Remove origin
        screenToWorldTransform.prependTranslation(-displayRectangleProperty.get().getMinX(), -displayRectangleProperty.get().getMinY());
        //Scale
        screenToWorldTransform.prependScale(
                elevationProfile.length() / displayRectangleProperty.get().getWidth(),
                -(elevationProfile.maxElevation() - elevationProfile.minElevation()) / displayRectangleProperty.get().getHeight()
        );
        //Add new origin
        screenToWorldTransform.prependTranslation(0, elevationProfile.maxElevation());
        try {
            screenToWorld.set(screenToWorldTransform);
            worldToScreen.set(screenToWorldTransform.createInverse());
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * Redraw the grid and all the labels associated with the grid
     * base on elevation difference and the total distance of the profile
     * @param path Shape used to draw the grid
     * @param textGroup Group for all the axesLabel
     */
    private void drawGrid(Path path, Group textGroup) {
        //Compute the shortest world distance where the screen distance is bigger than STEP_BETWEEN_POS
        int worldPosDistance = POS_STEPS[POS_STEPS.length - 1];
        for (int posStep : POS_STEPS) {
            Point2D distanceInScreen = worldToScreen.get().deltaTransform(posStep, 1);
            if(distanceInScreen.getX() >= STEP_BETWEEN_POS) {
                worldPosDistance = posStep;
                break;
            }
        }

        //Compute the shortest world height where the screen height is bigger than STEP_BETWEEN_ELE
        int worldEleDistance = ELE_STEPS[ELE_STEPS.length - 1];
        for (int eleStep : ELE_STEPS) {
            Point2D distanceInScreen = worldToScreen.get().deltaTransform(1, eleStep);
            if(distanceInScreen.getY() <= -STEP_BETWEEN_ELE) {
                worldEleDistance = eleStep;
                break;
            }
        }

        //Remove previously generated grid
        path.getElements().clear();
        textGroup.getChildren().clear();

        //Compute screen distance
        final Rectangle2D rectangle2D = displayRectangleProperty.get();
        final double screenPosDistance = worldToScreen.get().deltaTransform(worldPosDistance, 1).getX();
        final double screenEleDistance = worldToScreen.get().deltaTransform(1, worldEleDistance).getY();


        //Draw all the vertical lines
        int xIndex = 0;
        for(double x = rectangle2D.getMinX(); x <= rectangle2D.getMaxX(); x += screenPosDistance) {
            Point2D startLineCoordinates = new Point2D(x, rectangle2D.getMinY());
            Point2D endLineCoordinates = new Point2D(x, rectangle2D.getMaxY());

            path.getElements().addAll(
                    new MoveTo(startLineCoordinates.getX(), startLineCoordinates.getY()),
                    new LineTo(endLineCoordinates.getX(), endLineCoordinates.getY())
            );

            setTextAroundRectangle(textGroup, xIndex, worldPosDistance, x, 0);
            xIndex++;
        }

        //Draw all horizontal lines
        int yIndex = 0;
        for(double y = rectangle2D.getMaxY(); y >= rectangle2D.getMinY(); y += screenEleDistance) {
            Point2D startLineCoordinates = new Point2D(rectangle2D.getMinX(), y);
            Point2D endLineCoordinates = new Point2D(rectangle2D.getMaxX(), y);

            path.getElements().addAll(
                    new MoveTo(startLineCoordinates.getX(), startLineCoordinates.getY()),
                    new LineTo(endLineCoordinates.getX(), endLineCoordinates.getY())
            );

            setTextAroundRectangle(textGroup, yIndex, worldEleDistance, 0, y);
            yIndex++;
        }
    }

    /**
     * Displays all the information on the x and y axes
     * of the elevation graphic.
     * @param textGroup the text group given
     * @param index at which position display it.
     * @param distanceDifference the distance between every information,
     *                          depends on the zoom level.
     * @param xCoordinate information that will be displayed on the x axes.
     * @param yCoordinate information that will be displayed on the y axes.
     */
    private void setTextAroundRectangle(Group textGroup, int index, int distanceDifference,
                                        double xCoordinate, double yCoordinate) {
        Text axesLabels = new Text();
        if(xCoordinate != 0) {
            //dividing per 1000 to transform into kilometer.
            axesLabels = new Text(String.valueOf((index * distanceDifference) / 1000));
            //setting all the characteristics of the axesLabels.
            axesLabels.getStyleClass().add("horizontal");
            axesLabels.textOriginProperty().set(VPos.TOP);
            axesLabels.setLayoutX(
                    xCoordinate - (axesLabels.prefWidth(0) / 2)
            );
            axesLabels.setLayoutY(displayRectangleProperty.get().getMaxY());
        } else if(yCoordinate != 0) {
            axesLabels = new Text(String.valueOf((int) ((index * distanceDifference)
                    + elevationProfileProperty.get().minElevation())));
            //setting all the characteristics of the axesLabels.
            axesLabels.getStyleClass().add("vertical");
            axesLabels.textOriginProperty().set(VPos.CENTER);
            axesLabels.setLayoutX(displayRectangleProperty.get().getMinX() -
                    axesLabels.prefWidth(0) + 2);
            axesLabels.setLayoutY(yCoordinate);
        }
        //setting the final characteristics common to x and y axes.
        axesLabels.setId("grid_label");
        axesLabels.setFont(FONT_TEXT);
        textGroup.getChildren().add(axesLabels);
    }

    /**
     * Displaying the main titles around the rectangle of the elevation graphics.
     * @param text to be displayed.
     */
    private void updateTextElevationInformation(Text text) {
        ElevationProfile elevationProfileInfo =  elevationProfileProperty.get();
        //dividing per 1000 to transform into kilometers.
        String newText = "Longueur : %.1f km".formatted(elevationProfileInfo.length() / 1000) +
                            "     Montée : %.0f m".formatted(elevationProfileInfo.totalAscent()) +
                            "     Descente : %.0f m".formatted(elevationProfileInfo.totalDescent()) +
                            "     Altitude : de %.0f m à %.0f m".formatted(elevationProfileInfo.minElevation(),
                                elevationProfileInfo.maxElevation());
        text.setText(newText);
    }

    /**
     * Draw the elevation profile on a polygon
     * based on the ElevationProfile
     * @param polygon Polygon on which to draw the elevation
     */
    private void drawElevation(Polygon polygon) {
        polygon.getPoints().clear();
        Rectangle2D rectangle2D = displayRectangleProperty.get();

        polygon.getPoints().addAll(rectangle2D.getMaxX(), rectangle2D.getMaxY());
        polygon.getPoints().addAll(rectangle2D.getMinX(), rectangle2D.getMaxY());

        ElevationProfile elevationProfile = elevationProfileProperty.get();
        for (int i = 0; i <= elevationProfile.length(); i++) {
            Point2D screenCoordinate = worldToScreen.get().transform(i, elevationProfile.elevationAt(i));
            polygon.getPoints().addAll(screenCoordinate.getX(), screenCoordinate.getY());
        }
    }

    /**
     * Returning the pane.
     * @return the pane.
     */
    public Pane pane () {
        return borderPane;
    }

    /**
     * Returning the property of the mouse only to be read.
     * @return the property of the mouse only to be read.
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty(){
        return mousePositionOnProfileProperty;
    }
}

package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Main class that runs the program that will display the map
 * with all the rest of the characteristics from other classes.
 * @author Eden Kahane (346481)
 * @author Ilyas Hawazine (326815)
 * */
public final class JaVelo extends Application {

    /**
     * Run the program with parameters.
     * @param args the list of parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //getting information for all the swiss routes.
        Graph graph = Graph.loadFrom(Path.of("javelo-data"));
        TileManager tileManager = new TileManager(Path.of("osm-cache"),
                "tile.openstreetmap.org");
        CostFunction costFunction = new CityBikeCF(graph);
        RouteBean routeBean = new RouteBean(new RouteComputer(graph, costFunction));
        ErrorManager errorManager = new ErrorManager();

        //setting the menu item.
        MenuItem menuItem = new MenuItem("Exporter GPX");
        menuItem.disableProperty().bind(routeBean.getRouteProperty().isNull());
        Menu menu = new Menu("Fichier");
        menu.getItems().add(menuItem);
        MenuBar menuBar = new MenuBar(menu);

        //setting the main pane.
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        //setting the classes for managing the visualization of the pane.
        AnnotatedMapManager mapManager =
                new AnnotatedMapManager(graph, tileManager, routeBean, errorManager::displayError);
        ElevationProfileManager elevationProfileManager =
                new ElevationProfileManager(routeBean.getElevationProfileProperty(),
                routeBean.highlightedPositionProperty());

        elevationProfileManager.mousePositionOnProfileProperty().addListener((observable, oldValue, newValue) ->
                routeBean.setHighlightedPosition(newValue.doubleValue()));
        mapManager.mousePositionOnRouteProperty().addListener((observable, oldValue, newValue) ->
                routeBean.setHighlightedPosition(newValue.doubleValue()));
        splitPane.getItems().add(mapManager.pane());

        //writing the actual saved itinerary.
        menuItem.setOnAction(event -> {
            try {
                GpxGenerator.writeGpx(
                        "javelo.gpx",
                        routeBean.getRoute(),
                        ElevationProfileComputer.elevationProfile(routeBean.getRoute(),5));
            } catch (IOException | TransformerException e) {
               e.printStackTrace();
            }
        });

        //setting the elements to clear on display.
        SplitPane.setResizableWithParent(elevationProfileManager.pane(), false);
        routeBean.getRouteProperty().addListener((observable, oldValue, newValue) -> {
            splitPane.getItems().clear();
            splitPane.getItems().add(mapManager.pane());
            if(newValue != null) {
                splitPane.getItems().add(elevationProfileManager.pane());
            }
        });

        //setting the panes for errorManger and menuBar.
        StackPane centerPane = new StackPane(splitPane, errorManager.pane());
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(centerPane);

        //setting the scene to display.
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Javelo");
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
    }
}

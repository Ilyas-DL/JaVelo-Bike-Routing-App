package ch.epfl.javelo.gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Class that manages the error to display on screen
 * when an illegal action is made on the map.
 * @author Ilyas Hawazine (326815).
 */
public final class ErrorManager {

    private final VBox vboxPane ;
    private final SequentialTransition sequentialTransition;
    private final Text textVbox;

    /**
     * ErrorManager's constructor that builds all the styles and text for the pane.
     */
    public ErrorManager() {
        final Duration FIRST_FADING_DURATION = Duration.seconds(0.2d);
        final Duration LAST_FADING_DURATION = Duration.seconds(0.5d);
        final Duration PAUSE_DURATION = Duration.seconds(2.0d);

        this.textVbox = new Text();
        this.vboxPane = new VBox(textVbox);

        //setting the vbox and the text for errors.
        this.vboxPane.getStylesheets().add("error.css");
        this.vboxPane.setMouseTransparent(true);

        //setting the first fading that will appear.
        FadeTransition firstFadeTransition = new FadeTransition(FIRST_FADING_DURATION, vboxPane);
        firstFadeTransition.setFromValue(0d);
        firstFadeTransition.setToValue(0.8d);
        //setting the second fading that will appear.
        FadeTransition secondFadeTransition = new FadeTransition(LAST_FADING_DURATION, vboxPane);
        secondFadeTransition.setFromValue(0.8d);
        secondFadeTransition.setToValue(0d);
        //setting the pause transition.
        PauseTransition pauseTransition = new PauseTransition(PAUSE_DURATION);
        this.sequentialTransition = new SequentialTransition(vboxPane, firstFadeTransition,
                secondFadeTransition, pauseTransition);

    }

    /**
     * Returns the pane that has been modified
     * for the transitions.
     * @return the pane.
     */
    public VBox pane(){
        return vboxPane;
    }

    /**
     * Sets all the transitions on the pane
     * and applies the errorMessage in the text.
     * @param errorMessage the error text to be displayed.
     */
    public void displayError(String errorMessage){
        java.awt.Toolkit.getDefaultToolkit().beep();
        textVbox.setText(errorMessage);
        //managing the status when to run the sequential transition.
        if(sequentialTransition.getStatus() == Animation.Status.RUNNING){
            sequentialTransition.stop();
        }
        sequentialTransition.play();
    }
}

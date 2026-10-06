package pt.up.fe.els2026.theatre;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import pt.up.fe.els2026.theatre.assets.CharacterObject;
import pt.up.fe.els2026.theatre.assets.Gender;
import pt.up.fe.els2026.theatre.assets.ShapeObject;


public class SimpleExample extends Application {

    @Override
    public void start(Stage stage) {
        //This is a simple example of what we could do in a scene
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);

        //we can add buttons (e.g. we could add a redo button)
        Button exitButton = new Button("✕");
        exitButton.setStyle("-fx-font-size: 16px;");
        exitButton.setOnAction(e -> Platform.exit());
        StackPane.setAlignment(exitButton, Pos.TOP_RIGHT);
        root.getChildren().add(exitButton);

        //Adding a shortcut key for quick exit (ESC)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
                event.consume();
            }
        });

        //Runtime should be the one doing all the work...
        SceneDSL runtime = new SceneDSL(root);

        //Creating objects and characters
        ShapeObject ball =
                runtime.createCircle(
                        -25,
                        -25,
                        25,
                        Color.DODGERBLUE);

        CharacterObject alice =
                runtime.createCharacter(
                        200,
                        300,
                        165,
                        Gender.FEMALE,
                        Color.HOTPINK);

        CharacterObject hero =
                runtime.createCharacter(
                        100,
                        300,
                        180,
                        Gender.MALE,
                        Color.BLUE);

        //Adding animations to objects and characters

        //Ball Animation
        TranslateTransition transition =
                new TranslateTransition(
                        Duration.seconds(2.8),
                        ball.getNode());

        transition.setInterpolator( Interpolator.LINEAR);
        transition.setToX(580);
        transition.setToY(285);

        //make ball disappear when its animation is finished
        transition.setOnFinished(e-> root.getChildren().remove(ball.getNode()));
        transition.play();

        //Hero Animation
        TranslateTransition transitionH =
                new TranslateTransition(
                        Duration.seconds(3),
                        hero.getGroup());

        transitionH.setToX(500);
        transitionH.setToY(0);
        transitionH.play();

        //waiting a few seconds before doing a flip
        PauseTransition wait = new PauseTransition(Duration.seconds(2));
        RotateTransition rotate =
                new RotateTransition(
                        Duration.seconds(1),
                        hero.getGroup());
        rotate.setByAngle(360);
        wait.setOnFinished(e-> rotate.play());
        wait.play();

        //Alice Animation
        var balloon = runtime.createBalloon("Nice one!");

        //waiting for the hero to do the flip!
        PauseTransition sayNice =
                new PauseTransition(Duration.seconds(3));

        sayNice.setOnFinished(e -> {
            alice.getGroup().getChildren().add(balloon);
            PauseTransition wait2 =
                    new PauseTransition(
                            Duration.seconds(2));


            wait2.setOnFinished(e2 -> {

                alice.getGroup().getChildren().remove(balloon);

            });
            wait2.play();
        });
        sayNice.play();

        //You can use available classes and methods in javafx
        //See also javafx.animation.SequentialTransition and javafx.animation.ParallelTransition

        stage.setTitle("DSL Animation Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
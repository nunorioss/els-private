package pt.up.fe.els2026.theatre;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import pt.up.fe.els2026.theatre.assets.CharacterObject;
import pt.up.fe.els2026.theatre.assets.Gender;
import pt.up.fe.els2026.theatre.assets.ShapeObject;

public class SceneDSL {

    private final Pane root;

    public SceneDSL(Pane root) {
        this.root = root;
    }

    /////// Entities Creator
    public ShapeObject createCircle(
            double x,
            double y,
            double radius,
            Color color) {

        Circle circle = new Circle(radius);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setFill(color);

        root.getChildren().add(circle);

        return new ShapeObject(circle);
    }

    public CharacterObject createCharacter(
            double x,
            double y,
            double height,
            Gender gender,
            Color clothesColor) {

        CharacterObject character = CharacterObject.create(x, y, height, gender, clothesColor);

        root.getChildren().add(character.getGroup());

        return character;
    }

    public StackPane createBalloon(String message){
        Text text = new Text(message);

        text.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;");

        double width =
                text.getLayoutBounds().getWidth();

        double height =
                text.getLayoutBounds().getHeight();

        Rectangle bubble =
                new Rectangle(
                        width + 20,
                        height + 10);

        bubble.setArcWidth(15);
        bubble.setArcHeight(15);

        bubble.setFill(Color.WHITE);
        bubble.setStroke(Color.BLACK);

        StackPane balloon = new StackPane();

        balloon.getChildren().addAll(
                bubble,
                text);


        balloon.setLayoutX(-20);
        balloon.setLayoutY(-80);
        return balloon;
    }

    /////// Entities Animations
}

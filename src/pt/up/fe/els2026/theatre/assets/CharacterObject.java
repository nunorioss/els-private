package pt.up.fe.els2026.theatre.assets;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class CharacterObject {

    private final Group group;

        private final Circle head;
        private final Line body;

        private final Group leftArmGroup;
        private final Group rightArmGroup;

        private final Group leftLegGroup;
        private final Group rightLegGroup;
        private final Group clothes;


        public CharacterObject(
                Group group,
                Circle head,
                Line body,
                Group leftArm,
                Group rightArm,
                Group leftLeg,
                Group rightLeg,
                Group clothes) {

            this.group = group;
            this.head = head;
            this.body = body;
            this.leftArmGroup = leftArm;
            this.rightArmGroup = rightArm;
            this.leftLegGroup = leftLeg;
            this.rightLegGroup = rightLeg;
            this.clothes = clothes;
        }

        public Group getGroup() {
            return group;
        }

        public Circle getHead() {
            return head;
        }

        public Line getBody() {
            return body;
        }

    public Group getLeftArmGroup() {
        return leftArmGroup;
    }
    public Group getRightArmGroup() {
            return rightArmGroup;
    }

    public Group getLeftLegGroup() {
            return leftLegGroup;
    }
    public Group getRightLegGroup() {
            return rightLegGroup;

    }

    public Group getClothes() {
            return clothes;
        }


    public static CharacterObject create(
            double x,
            double y,
            double height,
            Gender gender,
            Color clothesColor) {

        Group character = new Group();

        double scale = height / 180.0;

        Circle head =
                new Circle(15 * scale);

        head.setCenterX(0);
        head.setCenterY(0);

        Line body =
                new Line(
                        0,
                        15 * scale,
                        0,
                        60 * scale);

        Line leftArm =
                new Line(
                        0,
                        25 * scale,
                        -20 * scale,
                        45 * scale);
        Group leftArmGroup = new Group(leftArm);

        Line rightArm =
                new Line(
                        0,
                        25 * scale,
                        20 * scale,
                        45 * scale);
        Group rightArmGroup = new Group(rightArm);
        Line leftLeg =
                new Line(
                        0,
                        60 * scale,
                        -15 * scale,
                        95 * scale);
        Group leftLegGroup = new Group(leftLeg);
        Line rightLeg =
                new Line(
                        0,
                        60 * scale,
                        15 * scale,
                        95 * scale);
        Group rightLegGroup = new Group(rightLeg);
        Group clothes = new Group();

        if (gender == Gender.FEMALE) {

            Polygon dress = new Polygon();

            dress.getPoints().addAll(
                    -5.0 * scale, 20.0 * scale,
                    5.0 * scale, 20.0 * scale,
                    25.0 * scale, 80.0 * scale,
                    -25.0 * scale, 80.0 * scale
            );
            dress.setFill(clothesColor);

            clothes.getChildren().add(dress);

        } else {

            Polygon shirt
                    = new Polygon();

            shirt.getPoints().addAll(
                    -8.0 * scale, 26.0 * scale,
                    -6 * scale, 23 * scale,
                    6 * scale, 23 * scale,
                    8.0 * scale, 26.0 * scale,
                    10.0 * scale, 61 * scale,
                    8.0 * scale, 63 * scale,
                    -8.0 * scale, 63 * scale,
                    -10.0 * scale, 61.0 * scale
            );
            shirt.setFill(clothesColor);
            Rectangle lpant =
                    new Rectangle(
                            -9 * scale,
                            20 * scale+41 * scale,
                            8 * scale,
                            24 * scale);
            lpant.setRotate(15);
            Rectangle rpant =
                    new Rectangle(
                            -9 * scale+11 * scale,
                            20 * scale+41 * scale,
                            8 * scale,
                            24 * scale);
            rpant.setRotate(-15);

            lpant.setFill(Color.gray(0.4));
            rpant.setFill(Color.gray(0.4));

            clothes.getChildren().addAll(shirt,lpant,rpant);
        }


        character.getChildren().addAll(
                head,
                body,
                leftArmGroup,
                rightArmGroup,
                leftLegGroup,
                rightLegGroup,
                clothes
        );

        character.setLayoutX(x);
        character.setLayoutY(y);

        return new CharacterObject(character,
                head,
                body,
                leftArmGroup,
                rightArmGroup,
                leftLegGroup,
                rightLegGroup,
                clothes
        );
    }
}
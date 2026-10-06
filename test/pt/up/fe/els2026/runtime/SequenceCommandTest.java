package pt.up.fe.els2026.runtime;

import com.stickman.anim.Command;
import com.stickman.anim.ParallelCommand;
import com.stickman.anim.Stickman;
import org.junit.Test;
import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.animation.AnimationReference;
import pt.up.fe.els2026.animation.AnimationStep;
import pt.up.fe.els2026.animation.BodyPart;
import pt.up.fe.els2026.animation.RotationMovement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SequenceCommandTest {

    private static final double EPS = 1e-6;

    @Test
    public void advancesThroughChildrenBasedOnElapsedTime() {
        Stickman stickman = new Stickman();
        SequenceCommand sequence = new SequenceCommand(
                Command.rotate("left_knee", 40, 100),
                Command.rotate("left_knee", 0, 100));
        assertEquals(200, sequence.getDuration());

        sequence.start(stickman);
        sequence.update(stickman, 0.25); // 50 ms: halfway through the first rotation
        assertEquals(20, angle(stickman, "left_knee"), EPS);

        sequence.update(stickman, 0.75); // 150 ms: first finished at 40, second halfway back to 0
        assertEquals(20, angle(stickman, "left_knee"), EPS);

        sequence.update(stickman, 1.0);
        assertEquals(0, angle(stickman, "left_knee"), EPS);
        sequence.finish(stickman);
        assertEquals(0, angle(stickman, "left_knee"), EPS);
    }

    @Test
    public void finishAppliesRemainingChildrenExactly() {
        Stickman stickman = new Stickman();
        SequenceCommand sequence = new SequenceCommand(
                Command.rotate("neck", 10, 100),
                Command.moveBy(50, 0, 100));
        double startX = stickman.getX();

        sequence.start(stickman);
        sequence.update(stickman, 0.1);
        sequence.finish(stickman);

        assertEquals(10, angle(stickman, "neck"), EPS);
        assertEquals(startX + 50, stickman.getX(), EPS);
    }

    @Test
    public void multiStepAnimationRunsInParallelWithSingleStepOne() {
        // "dance": 2 steps of 300 ms each; "raise knee": 1 step of 200 ms
        Animation dance = new Animation("dance", List.of(
                new AnimationStep(RotationMovement.to(BodyPart.TORSO, -20, 300)),
                new AnimationStep(RotationMovement.to(BodyPart.TORSO, 20, 300))));
        Animation raiseKnee = new Animation("raise knee", List.of(
                new AnimationStep(RotationMovement.to(BodyPart.LEFT_KNEE, 60, 200))));
        Animation timeline = new Animation("timeline", List.of(
                new AnimationStep(new AnimationReference(dance), new AnimationReference(raiseKnee))));

        List<Command> commands = AnimationCompiler.compile(timeline);
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof ParallelCommand);
        ParallelCommand step = (ParallelCommand) commands.get(0);
        assertEquals("the step waits for the longest child (the whole dance)", 600, step.getDuration());
        assertTrue(step.getCommands().get(0) instanceof SequenceCommand);

        Stickman stickman = new Stickman();
        step.start(stickman);
        step.update(stickman, 0.5); // 300 ms: dance step 1 done, raise knee done
        assertEquals(-20, angle(stickman, "torso"), EPS);
        assertEquals(60, angle(stickman, "left_knee"), EPS);

        step.update(stickman, 0.75); // 450 ms: halfway through dance step 2 (-20 -> 20)
        assertEquals(0, angle(stickman, "torso"), EPS);
        assertEquals(60, angle(stickman, "left_knee"), EPS);

        step.finish(stickman);
        assertEquals(20, angle(stickman, "torso"), EPS);
    }

    @Test
    public void singleReferenceStepsAreSplicedAndRepeated() {
        Animation blink = new Animation("nod", List.of(
                new AnimationStep(RotationMovement.to(BodyPart.NECK, 10, 100)),
                new AnimationStep(RotationMovement.to(BodyPart.NECK, 0, 100))));
        Animation timeline = new Animation("timeline", List.of(
                new AnimationStep(new AnimationReference(blink, 3))));

        List<Command> commands = AnimationCompiler.compile(timeline);
        assertEquals(6, commands.size());
        assertEquals(600, commands.stream().mapToInt(Command::getDuration).sum());
    }

    private static double angle(Stickman stickman, String joint) {
        return stickman.getJoint(joint).getAngle();
    }
}

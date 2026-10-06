package pt.up.fe.els2026.runtime;

import com.stickman.anim.Command;
import com.stickman.anim.ParallelCommand;
import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.animation.AnimationReference;
import pt.up.fe.els2026.animation.AnimationStep;
import pt.up.fe.els2026.animation.Movement;
import pt.up.fe.els2026.animation.RotationMovement;
import pt.up.fe.els2026.animation.TranslationMovement;
import pt.up.fe.els2026.animation.WaitMovement;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates the semantic model into the skeleton's {@link Command}s.
 *
 * <ul>
 *   <li>an {@link Animation} becomes a list of commands (played sequentially by {@code Stickman.play});</li>
 *   <li>an {@link AnimationStep} becomes a {@link ParallelCommand};</li>
 *   <li>an {@link AnimationReference} inside a step becomes a {@link SequenceCommand} so that the
 *       referenced animation keeps its own step boundaries while running alongside other movements.</li>
 * </ul>
 *
 * Commands hold runtime state (start angle/position), so every call creates fresh instances; the same
 * animation referenced twice never shares command objects.
 */
public final class AnimationCompiler {

    private AnimationCompiler() {
    }

    public static List<Command> compile(Animation animation) {
        List<Command> commands = new ArrayList<>();
        for (AnimationStep step : animation.steps()) {
            compileStep(step, commands);
        }
        return commands;
    }

    private static void compileStep(AnimationStep step, List<Command> out) {
        List<Movement> movements = step.movements();

        // A step made of a single reference is just "play that animation here": splice its steps in
        // instead of nesting a sequence inside the top-level sequence.
        if (movements.size() == 1 && movements.get(0) instanceof AnimationReference reference) {
            for (int i = 0; i < reference.repetitions(); i++) {
                out.addAll(compile(reference.target()));
            }
            return;
        }

        if (movements.size() == 1) {
            out.add(compileMovement(movements.get(0)));
            return;
        }

        List<Command> parallel = new ArrayList<>(movements.size());
        for (Movement movement : movements) {
            parallel.add(compileMovement(movement));
        }
        out.add(new ParallelCommand(parallel));
    }

    public static Command compileMovement(Movement movement) {
        return switch (movement) {
            case RotationMovement rotation -> rotation.relative()
                    ? Command.rotateBy(rotation.bodyPart().jointName(), rotation.angle(), rotation.durationMs())
                    : Command.rotate(rotation.bodyPart().jointName(), rotation.angle(), rotation.durationMs());

            case TranslationMovement translation -> translation.relative()
                    ? Command.moveBy(translation.x(), translation.y(), translation.durationMs())
                    : Command.move(translation.x(), translation.y(), translation.durationMs());

            case WaitMovement wait -> Command.wait(wait.durationMs());

            case AnimationReference reference -> {
                List<Command> sequence = new ArrayList<>();
                for (int i = 0; i < reference.repetitions(); i++) {
                    sequence.addAll(compile(reference.target()));
                }
                yield sequence.size() == 1 ? sequence.get(0) : new SequenceCommand(sequence);
            }
        };
    }
}

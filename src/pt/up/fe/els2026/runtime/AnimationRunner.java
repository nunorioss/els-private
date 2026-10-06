package pt.up.fe.els2026.runtime;

import com.stickman.anim.Command;
import com.stickman.anim.Stickman;
import pt.up.fe.els2026.animation.Animation;

import java.util.List;

/**
 * Plays an {@link Animation} on a {@link Stickman} using the skeleton's own player.
 */
public final class AnimationRunner {

    private final Stickman stickman;

    public AnimationRunner(Stickman stickman) {
        this.stickman = stickman;
    }

    /** Creates a runner with a skeleton positioned like the professor's examples. */
    public static AnimationRunner withDefaultStickman() {
        return new AnimationRunner(new Stickman(300, 280, 1.3));
    }

    public Stickman stickman() {
        return stickman;
    }

    public void play(Animation animation, boolean loop) {
        play(animation, loop, null);
    }

    public void play(Animation animation, boolean loop, Runnable onComplete) {
        List<Command> commands = AnimationCompiler.compile(animation);
        stickman.showWindow(600, 600, "ELS 2026 - " + animation.name());
        stickman.play(commands, loop, onComplete);
    }
}

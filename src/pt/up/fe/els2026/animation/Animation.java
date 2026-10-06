package pt.up.fe.els2026.animation;

import java.util.List;
import java.util.Objects;

/**
 * A named sequence of {@link AnimationStep}s. Steps run one after the other.
 *
 * User-defined verbs ("walk", "dance", "raise knee", ...) and the top-level timeline are all Animations,
 * so anything can be reused inside anything else through an {@link AnimationReference}.
 *
 * The steps are set after construction so that the parser can declare every animation first and only
 * then resolve references between them (this is what allows forward references in the JSON file).
 */
public final class Animation {

    private final String name;
    private List<AnimationStep> steps = List.of();

    public Animation(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Animation(String name, List<AnimationStep> steps) {
        this(name);
        setSteps(steps);
    }

    public String name() {
        return name;
    }

    public List<AnimationStep> steps() {
        return steps;
    }

    public void setSteps(List<AnimationStep> steps) {
        this.steps = List.copyOf(steps);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /** Total duration in milliseconds: the sum of the duration of each step. */
    public int durationMs() {
        int total = 0;
        for (AnimationStep step : steps) {
            total += step.durationMs();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Animation[" + name + ", " + steps.size() + " step(s)]";
    }
}

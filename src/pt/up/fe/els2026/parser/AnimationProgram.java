package pt.up.fe.els2026.parser;

import pt.up.fe.els2026.animation.Animation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The result of parsing a file: every user-defined animation (in definition order) plus the timeline,
 * which is the animation that actually gets played.
 */
public record AnimationProgram(Map<String, Animation> animations, Animation timeline) {

    public AnimationProgram {
        animations = Collections.unmodifiableMap(new LinkedHashMap<>(animations));
        if (timeline == null) throw new IllegalArgumentException("timeline must not be null");
    }

    public Animation animation(String name) {
        return animations.get(name);
    }
}

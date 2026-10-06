package pt.up.fe.els2026.animation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The joints of the skeleton that can be rotated. Mirrors the rig and aliases of
 * {@code com.stickman.anim.Stickman} so that names can be validated at parse time.
 */
public enum BodyPart {
    ROOT("root", "hips", "pelvis"),
    TORSO("torso", "spine", "body"),
    NECK("neck"),
    HEAD("head"),

    LEFT_SHOULDER("left_shoulder", "left_arm", "l_shoulder", "l_arm", "shoulder_l"),
    LEFT_ELBOW("left_elbow", "left_forearm", "l_elbow", "l_forearm", "elbow_l"),
    LEFT_WRIST("left_wrist", "left_hand", "l_wrist", "l_hand", "wrist_l"),

    RIGHT_SHOULDER("right_shoulder", "right_arm", "r_shoulder", "r_arm", "shoulder_r"),
    RIGHT_ELBOW("right_elbow", "right_forearm", "r_elbow", "r_forearm", "elbow_r"),
    RIGHT_WRIST("right_wrist", "right_hand", "r_wrist", "r_hand", "wrist_r"),

    LEFT_HIP("left_hip", "left_leg", "l_hip", "l_leg", "left_thigh", "hip_l"),
    LEFT_KNEE("left_knee", "left_shin", "l_knee", "l_shin", "knee_l"),
    LEFT_ANKLE("left_ankle", "left_foot", "l_ankle", "l_foot", "ankle_l"),

    RIGHT_HIP("right_hip", "right_leg", "r_hip", "r_leg", "right_thigh", "hip_r"),
    RIGHT_KNEE("right_knee", "right_shin", "r_knee", "r_shin", "knee_r"),
    RIGHT_ANKLE("right_ankle", "right_foot", "r_ankle", "r_foot", "ankle_r");

    private static final Map<String, BodyPart> BY_NAME;

    static {
        Map<String, BodyPart> map = new HashMap<>();
        for (BodyPart part : values()) {
            for (String alias : part.aliases) {
                map.put(alias, part);
            }
        }
        BY_NAME = Collections.unmodifiableMap(map);
    }

    private final String jointName;
    private final List<String> aliases;

    BodyPart(String jointName, String... otherNames) {
        this.jointName = jointName;
        String[] all = new String[otherNames.length + 1];
        all[0] = jointName;
        System.arraycopy(otherNames, 0, all, 1, otherNames.length);
        this.aliases = List.of(all);
    }

    /** The canonical joint name understood by {@code Stickman.rotate}. */
    public String jointName() {
        return jointName;
    }

    /** All names accepted for this part (canonical name first). */
    public List<String> aliases() {
        return aliases;
    }

    /**
     * Resolves a user-written name (case-insensitive; spaces and dashes are treated as underscores,
     * exactly like the skeleton does).
     */
    public static Optional<BodyPart> fromName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(BY_NAME.get(normalize(name)));
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    /** Canonical names of every body part, in rig order. */
    public static List<String> jointNames() {
        return Arrays.stream(values()).map(BodyPart::jointName).toList();
    }
}

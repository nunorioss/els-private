package pt.up.fe.els2026.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import pt.up.fe.els2026.animation.Animation;
import pt.up.fe.els2026.animation.AnimationReference;
import pt.up.fe.els2026.animation.AnimationStep;
import pt.up.fe.els2026.animation.BodyPart;
import pt.up.fe.els2026.animation.Movement;
import pt.up.fe.els2026.animation.RotationMovement;
import pt.up.fe.els2026.animation.TranslationMovement;
import pt.up.fe.els2026.animation.WaitMovement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses the JSON animation format into the semantic model.
 *
 * <pre>
 * {
 *   "animations": {                              // optional: named, reusable animations ("verbs")
 *     "raise knee": [ STEP, STEP, ... ],
 *     ...
 *   },
 *   "timeline": [ STEP, STEP, ... ]              // required: what actually gets played
 * }
 *
 * STEP      := [ MOVEMENT, MOVEMENT, ... ]       // movements run in parallel
 *            | MOVEMENT                          // shorthand for a step with a single movement
 *            | "any string"                      // comment, ignored
 *
 * MOVEMENT  := ["rotate",   bodyPart, angle, ms] // rotate joint to an absolute angle (degrees)
 *            | ["rotateBy", bodyPart, delta, ms] // rotate joint by a delta
 *            | ["move",     dx, dy, ms]          // move the skeleton by an offset
 *            | ["moveTo",   x, y, ms]            // move the skeleton to a position
 *            | ["wait",     ms]                  // hold the pose
 *            | ["name"]                          // play animation "name"
 *            | ["name", times]                   // play animation "name" `times` times in a row
 * </pre>
 *
 * Every problem found is recorded in the {@link Diagnostics} passed to the constructor; parsing
 * continues after an error so that all problems are reported at once. If any error was recorded an
 * {@link AnimationParseException} is thrown at the end.
 */
public final class AnimationParser {

    private static final String ANIMATIONS = "animations";
    private static final String TIMELINE = "timeline";

    private static final Set<String> KEYWORDS = Set.of("rotate", "rotateby", "move", "moveto", "wait");

    private static final Pattern DUPLICATE_FIELD = Pattern.compile("Duplicate field '(.*?)'");

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .build();

    private final Diagnostics diagnostics;

    public AnimationParser() {
        this(new Diagnostics());
    }

    public AnimationParser(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Diagnostics diagnostics() {
        return diagnostics;
    }

    public AnimationProgram parse(Path file) throws IOException {
        return parse(Files.readString(file), file.toString());
    }

    public AnimationProgram parse(String json) {
        return parse(json, "<string>");
    }

    public AnimationProgram parse(String json, String sourceName) {
        JsonNode root;
        try {
            root = MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            diagnostics.error(locationOf(e, sourceName), describeSyntaxError(e));
            throw new AnimationParseException(diagnostics);
        }

        AnimationProgram program = new ProgramBuilder().build(root);
        if (diagnostics.hasErrors()) {
            throw new AnimationParseException(diagnostics);
        }
        return program;
    }

    // -------------------------------------------------------------------------------------------------
    // Syntax-level errors reported by Jackson
    // -------------------------------------------------------------------------------------------------

    private static String locationOf(JsonProcessingException e, String sourceName) {
        var loc = e.getLocation();
        if (loc == null) return sourceName;
        return sourceName + ":" + loc.getLineNr() + ":" + loc.getColumnNr();
    }

    private static String describeSyntaxError(JsonProcessingException e) {
        String message = e.getOriginalMessage();
        Matcher duplicate = DUPLICATE_FIELD.matcher(message);
        if (duplicate.find()) {
            String name = duplicate.group(1);
            if (name.equals(ANIMATIONS) || name.equals(TIMELINE)) {
                return "field \"" + name + "\" appears more than once";
            }
            return "animation '" + name + "' has already been defined";
        }
        return "invalid JSON: " + message;
    }

    // -------------------------------------------------------------------------------------------------
    // Tree walk
    // -------------------------------------------------------------------------------------------------

    private final class ProgramBuilder {

        private final Map<String, Animation> animations = new LinkedHashMap<>();

        AnimationProgram build(JsonNode root) {
            Animation timeline = new Animation(TIMELINE);

            if (!root.isObject()) {
                diagnostics.error("", "the document must be a JSON object with \"" + ANIMATIONS
                        + "\" and \"" + TIMELINE + "\", found " + typeName(root));
                return new AnimationProgram(animations, timeline);
            }

            for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
                String field = it.next();
                if (!field.equals(ANIMATIONS) && !field.equals(TIMELINE)) {
                    diagnostics.warning(field, "unknown top-level field ignored (expected \""
                            + ANIMATIONS + "\" or \"" + TIMELINE + "\")");
                }
            }

            JsonNode definitions = root.get(ANIMATIONS);
            if (definitions != null) {
                parseDefinitions(definitions);
            }

            JsonNode timelineNode = root.get(TIMELINE);
            if (timelineNode == null) {
                diagnostics.error("", "missing required field \"" + TIMELINE + "\": there is nothing to play");
            } else {
                timeline.setSteps(parseSteps(timelineNode, TIMELINE));
                if (timeline.isEmpty() && !diagnostics.hasErrors()) {
                    diagnostics.warning(TIMELINE, "the timeline is empty: nothing will be played");
                }
            }

            boolean cyclic = reportCycles();
            if (!cyclic) {
                reportSummary(timeline);
            }
            return new AnimationProgram(animations, timeline);
        }

        /**
         * Two passes: declare every name first so that definitions can reference animations defined
         * later in the file, then parse the bodies.
         */
        private void parseDefinitions(JsonNode definitions) {
            if (!definitions.isObject()) {
                diagnostics.error(ANIMATIONS, "expected an object mapping animation names to step lists, found "
                        + typeName(definitions));
                return;
            }

            for (Iterator<String> it = definitions.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                if (name.isBlank()) {
                    diagnostics.error(ANIMATIONS, "animation names cannot be empty");
                } else if (KEYWORDS.contains(name.toLowerCase(Locale.ROOT))) {
                    diagnostics.error(ANIMATIONS, "'" + name + "' is a reserved keyword and cannot be used as an animation name");
                } else if (name.equals(TIMELINE)) {
                    diagnostics.error(ANIMATIONS, "'" + TIMELINE + "' is reserved for the top-level timeline and cannot be used as an animation name");
                } else {
                    animations.put(name, new Animation(name));
                    diagnostics.info(pathOf(name), "declared animation '" + name + "'");
                }
            }

            for (Map.Entry<String, JsonNode> entry : definitions.properties()) {
                Animation animation = animations.get(entry.getKey());
                if (animation == null) continue; // name was rejected above
                animation.setSteps(parseSteps(entry.getValue(), pathOf(entry.getKey())));
            }
        }

        private List<AnimationStep> parseSteps(JsonNode node, String path) {
            List<AnimationStep> steps = new ArrayList<>();
            if (!node.isArray()) {
                diagnostics.error(path, "expected a list of steps (array), found " + typeName(node));
                return steps;
            }

            for (int i = 0; i < node.size(); i++) {
                JsonNode stepNode = node.get(i);
                String stepPath = path + "[" + i + "]";

                if (stepNode.isTextual()) {
                    diagnostics.info(stepPath, "comment ignored: \"" + stepNode.asText() + "\"");
                    continue;
                }
                if (!stepNode.isArray()) {
                    diagnostics.error(stepPath, "expected a step (array of parallel movements), found " + typeName(stepNode));
                    continue;
                }
                if (stepNode.isEmpty()) {
                    diagnostics.warning(stepPath, "empty step ignored");
                    continue;
                }

                List<Movement> movements = new ArrayList<>();
                if (stepNode.get(0).isTextual()) {
                    // ["rotate", ...] or ["walk"] directly as a step: a single-movement step
                    Movement movement = parseMovement(stepNode, stepPath);
                    if (movement != null) movements.add(movement);
                } else {
                    for (int j = 0; j < stepNode.size(); j++) {
                        Movement movement = parseMovement(stepNode.get(j), stepPath + "[" + j + "]");
                        if (movement != null) movements.add(movement);
                    }
                }

                if (movements.isEmpty()) continue; // errors already reported for each movement
                warnAboutConflicts(movements, stepPath);
                steps.add(new AnimationStep(movements));
            }
            return steps;
        }

        private Movement parseMovement(JsonNode node, String path) {
            if (!node.isArray()) {
                diagnostics.error(path, "expected a movement such as [\"rotate\", bodyPart, angle, ms] or [\"name\"], found "
                        + typeName(node));
                return null;
            }
            if (node.isEmpty()) {
                diagnostics.error(path, "empty movement");
                return null;
            }
            JsonNode head = node.get(0);
            if (!head.isTextual()) {
                diagnostics.error(path, "a movement must start with a keyword (rotate, rotateBy, move, moveTo, wait) or an animation name, found "
                        + typeName(head));
                return null;
            }

            String keyword = head.asText();
            return switch (keyword.toLowerCase(Locale.ROOT)) {
                case "rotate" -> parseRotation(node, path, keyword, false);
                case "rotateby" -> parseRotation(node, path, keyword, true);
                case "move" -> parseTranslation(node, path, keyword, true);
                case "moveto" -> parseTranslation(node, path, keyword, false);
                case "wait" -> parseWait(node, path);
                default -> parseReference(node, path, keyword);
            };
        }

        private Movement parseRotation(JsonNode node, String path, String keyword, boolean relative) {
            String usage = "[\"" + keyword + "\", bodyPart, " + (relative ? "deltaDegrees" : "angleDegrees") + ", durationMs]";
            if (!checkArity(node, 4, path, usage)) return null;

            BodyPart part = parseBodyPart(node.get(1), path + "[1]");
            Double angle = requireNumber(node.get(2), path + "[2]", relative ? "delta angle" : "angle");
            Integer duration = requireDuration(node.get(3), path + "[3]");
            if (part == null || angle == null || duration == null) return null;

            return new RotationMovement(part, angle, relative, duration);
        }

        private Movement parseTranslation(JsonNode node, String path, String keyword, boolean relative) {
            String usage = "[\"" + keyword + "\", " + (relative ? "dx, dy" : "x, y") + ", durationMs]";
            if (!checkArity(node, 4, path, usage)) return null;

            Double x = requireNumber(node.get(1), path + "[1]", relative ? "dx" : "x");
            Double y = requireNumber(node.get(2), path + "[2]", relative ? "dy" : "y");
            Integer duration = requireDuration(node.get(3), path + "[3]");
            if (x == null || y == null || duration == null) return null;

            return new TranslationMovement(x, y, relative, duration);
        }

        private Movement parseWait(JsonNode node, String path) {
            if (!checkArity(node, 2, path, "[\"wait\", durationMs]")) return null;
            Integer duration = requireDuration(node.get(1), path + "[1]");
            if (duration == null) return null;
            return new WaitMovement(duration);
        }

        private Movement parseReference(JsonNode node, String path, String name) {
            Animation target = animations.get(name);
            if (target == null) {
                diagnostics.error(path, "unknown animation '" + name + "'" + suggest(name, animations.keySet(), "animations"));
                return null;
            }
            if (node.size() > 2) {
                diagnostics.error(path, "animation reference expects [\"" + name + "\"] or [\"" + name
                        + "\", times], but got " + node.size() + " elements");
                return null;
            }
            int repetitions = 1;
            if (node.size() == 2) {
                Integer times = requirePositiveInt(node.get(1), path + "[1]", "repetition count");
                if (times == null) return null;
                repetitions = times;
            }
            return new AnimationReference(target, repetitions);
        }

        // ---------------------------------------------------------------------------------------------
        // Argument helpers: each returns null (after recording an error) when the value is invalid
        // ---------------------------------------------------------------------------------------------

        private boolean checkArity(JsonNode node, int expected, String path, String usage) {
            if (node.size() == expected) return true;
            diagnostics.error(path, "'" + node.get(0).asText() + "' expects " + (expected - 1)
                    + " argument(s) but got " + (node.size() - 1) + "; usage: " + usage);
            return false;
        }

        private BodyPart parseBodyPart(JsonNode node, String path) {
            if (!node.isTextual()) {
                diagnostics.error(path, "expected a body part name (string), found " + typeName(node));
                return null;
            }
            String name = node.asText();
            return BodyPart.fromName(name).orElseGet(() -> {
                diagnostics.error(path, "unknown body part '" + name + "'"
                        + suggest(BodyPart.normalize(name), BodyPart.jointNames(), "body parts"));
                return null;
            });
        }

        private Double requireNumber(JsonNode node, String path, String what) {
            if (!node.isNumber()) {
                diagnostics.error(path, "expected a number for " + what + ", found " + typeName(node));
                return null;
            }
            return node.asDouble();
        }

        private Integer requireDuration(JsonNode node, String path) {
            if (!node.isIntegralNumber()) {
                diagnostics.error(path, "expected an integer duration in milliseconds, found " + typeName(node));
                return null;
            }
            int value = node.asInt();
            if (value < 0) {
                diagnostics.error(path, "duration must be >= 0 ms, got " + value);
                return null;
            }
            if (value == 0) {
                diagnostics.warning(path, "duration of 0 ms: the movement will be applied instantly");
            }
            return value;
        }

        private Integer requirePositiveInt(JsonNode node, String path, String what) {
            if (!node.isIntegralNumber()) {
                diagnostics.error(path, "expected a positive integer for " + what + ", found " + typeName(node));
                return null;
            }
            int value = node.asInt();
            if (value < 1) {
                diagnostics.error(path, what + " must be >= 1, got " + value);
                return null;
            }
            return value;
        }

        // ---------------------------------------------------------------------------------------------
        // Semantic checks
        // ---------------------------------------------------------------------------------------------

        private void warnAboutConflicts(List<Movement> movements, String stepPath) {
            Map<BodyPart, Integer> seen = new HashMap<>();
            int translations = 0;
            for (Movement movement : movements) {
                if (movement instanceof RotationMovement rotation) {
                    seen.merge(rotation.bodyPart(), 1, Integer::sum);
                } else if (movement instanceof TranslationMovement) {
                    translations++;
                }
            }
            seen.forEach((part, count) -> {
                if (count > 1) {
                    diagnostics.warning(stepPath, "body part '" + part.jointName() + "' is rotated by " + count
                            + " movements in the same step; the last one wins");
                }
            });
            if (translations > 1) {
                diagnostics.warning(stepPath, translations + " translations in the same step; the last one wins");
            }
        }

        /** Depth-first search over references; returns true if at least one cycle was found. */
        private boolean reportCycles() {
            Set<Animation> finished = new HashSet<>();
            Set<Animation> onPath = new HashSet<>();
            Deque<String> path = new ArrayDeque<>();
            boolean cyclic = false;
            for (Animation animation : animations.values()) {
                cyclic |= findCycle(animation, finished, onPath, path);
            }
            return cyclic;
        }

        private boolean findCycle(Animation animation, Set<Animation> finished, Set<Animation> onPath, Deque<String> path) {
            if (finished.contains(animation)) return false;
            if (onPath.contains(animation)) {
                List<String> cycle = new ArrayList<>(path);
                cycle = cycle.subList(cycle.indexOf(animation.name()), cycle.size());
                cycle.add(animation.name());
                diagnostics.error(pathOf(animation.name()), "cyclic reference: " + String.join(" -> ", cycle)
                        + " (an animation cannot contain itself)");
                return true;
            }

            onPath.add(animation);
            path.addLast(animation.name());
            boolean cyclic = false;
            for (AnimationStep step : animation.steps()) {
                for (Movement movement : step.movements()) {
                    if (movement instanceof AnimationReference reference) {
                        cyclic |= findCycle(reference.target(), finished, onPath, path);
                    }
                }
            }
            path.removeLast();
            onPath.remove(animation);
            finished.add(animation);
            return cyclic;
        }

        private void reportSummary(Animation timeline) {
            Set<Animation> used = new HashSet<>();
            collectReferences(timeline, used);

            for (Animation animation : animations.values()) {
                diagnostics.info(pathOf(animation.name()), "defined animation '" + animation.name() + "': "
                        + animation.steps().size() + " step(s), " + animation.durationMs() + " ms");
                if (!used.contains(animation)) {
                    diagnostics.warning(pathOf(animation.name()), "animation '" + animation.name()
                            + "' is defined but never used by the timeline");
                }
            }
            diagnostics.info(TIMELINE, "timeline: " + timeline.steps().size() + " step(s), "
                    + timeline.durationMs() + " ms in total");
        }

        private void collectReferences(Animation animation, Set<Animation> used) {
            for (AnimationStep step : animation.steps()) {
                for (Movement movement : step.movements()) {
                    if (movement instanceof AnimationReference reference && used.add(reference.target())) {
                        collectReferences(reference.target(), used);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Message helpers
    // -------------------------------------------------------------------------------------------------

    private static String pathOf(String animationName) {
        return ANIMATIONS + "[\"" + animationName + "\"]";
    }

    private static String typeName(JsonNode node) {
        return node.getNodeType().name().toLowerCase(Locale.ROOT);
    }

    /** "Did you mean ...?" when a close candidate exists, otherwise lists the known candidates. */
    private static String suggest(String name, Collection<String> candidates, String what) {
        if (candidates.isEmpty()) {
            return " (no " + what + " are defined)";
        }
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : candidates) {
            int distance = levenshtein(name.toLowerCase(Locale.ROOT), candidate.toLowerCase(Locale.ROOT));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        if (best != null && bestDistance <= Math.max(2, name.length() / 3)) {
            return ". Did you mean '" + best + "'?";
        }
        return " (known " + what + ": " + candidates.stream().map(c -> "'" + c + "'").collect(Collectors.joining(", ")) + ")";
    }

    private static int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}

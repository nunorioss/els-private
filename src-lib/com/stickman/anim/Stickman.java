package com.stickman.anim;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * Super simple, self-contained stickman character model.
 * Features:
 * - Instantiable with (x, y) and scale.
 * - Hierarchical 15-joint rig with Forward Kinematics (FK).
 * - Direct joint rotation and root movement methods.
 * - Built-in no-frills display window.
 * - List-based animation playback with ~60 FPS timer.
 */
public class Stickman {

    /**
     * An articulated skeletal joint.
     */
    public static class Joint {
        private final String name;
        private final List<Joint> children = new ArrayList<>();
        private double length;
        private double baseAngle;   // Rest anatomical angle relative to parent (degrees)
        private double angle = 0.0; // Current articulation offset from rest pose (degrees)

        // Computed Forward Kinematics world coordinates
        private double startX, startY, endX, endY, worldAngle;

        public Joint(String name, double length, double baseAngle) {
            this.name = name;
            this.length = length;
            this.baseAngle = baseAngle;
        }

        public String getName() { return name; }
        public List<Joint> getChildren() { return Collections.unmodifiableList(children); }
        public double getLength() { return length; }
        public void setLength(double length) { this.length = length; }
        public double getBaseAngle() { return baseAngle; }
        public void setBaseAngle(double baseAngle) { this.baseAngle = baseAngle; }
        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
        public void rotateBy(double delta) { this.angle += delta; }
        public double getWorldStartX() { return startX; }
        public double getWorldStartY() { return startY; }
        public double getWorldEndX() { return endX; }
        public double getWorldEndY() { return endY; }
        public double getWorldAngle() { return worldAngle; }
    }

    // Position & scaling
    private double x;
    private double y;
    private double scale = 1.0;

    // Skeletal rig
    private final Joint root;
    private final Map<String, Joint> joints = new LinkedHashMap<>();
    private final Map<String, String> aliases = new HashMap<>();

    // Appearance
    private Color color = new Color(30, 41, 59);
    private float strokeWidth = 3.5f;

    // Built-in window and animation player
    private JFrame window;
    private JPanel canvas;
    private javax.swing.Timer timer;
    private boolean isPlaying = false;
    private int currentCommandIndex = 0;
    private long commandStartTime = 0;
    private List<Command> activeCommands;
    private boolean loopAnimation = false;
    private Runnable onCompleteCallback;

    public Stickman() {
        this(300, 300, 1.0);
    }

    public Stickman(double x, double y) {
        this(x, y, 1.0);
    }

    public Stickman(double x, double y, double scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;

        // Build standard 15-joint skeletal rig
        this.root = add(null, "root", 0, 0);

        Joint torso = add(root, "torso", 65, -90);
        Joint neck  = add(torso, "neck", 12, 0);
        add(neck, "head", 22, 0);

        Joint lShoulder = add(neck, "left_shoulder", 42, 175);
        Joint lElbow    = add(lShoulder, "left_elbow", 38, 0);
        add(lElbow, "left_wrist", 10, 0);

        Joint rShoulder = add(neck, "right_shoulder", 42, 185);
        Joint rElbow    = add(rShoulder, "right_elbow", 38, 0);
        add(rElbow, "right_wrist", 10, 0);

        Joint lHip  = add(root, "left_hip", 54, 92);
        Joint lKnee = add(lHip, "left_knee", 50, 0);
        add(lKnee, "left_ankle", 16, -90);

        Joint rHip  = add(root, "right_hip", 54, 88);
        Joint rKnee = add(rHip, "right_knee", 50, 0);
        add(rKnee, "right_ankle", 16, -90);

        setupAliases();
        updateKinematics();
    }

    private Joint add(Joint parent, String name, double length, double baseAngle) {
        Joint j = new Joint(name, length, baseAngle);
        if (parent != null) parent.children.add(j);
        joints.put(name.toLowerCase(), j);
        return j;
    }

    private void alias(String target, String... names) {
        for (String n : names) aliases.put(n.toLowerCase(), target.toLowerCase());
    }

    private void setupAliases() {
        alias("root", "hips", "pelvis");
        alias("torso", "spine", "body");
        alias("left_shoulder", "left_arm", "l_shoulder", "l_arm", "shoulder_l");
        alias("left_elbow", "left_forearm", "l_elbow", "l_forearm", "elbow_l");
        alias("left_wrist", "left_hand", "l_wrist", "l_hand", "wrist_l");
        alias("right_shoulder", "right_arm", "r_shoulder", "r_arm", "shoulder_r");
        alias("right_elbow", "right_forearm", "r_elbow", "r_forearm", "elbow_r");
        alias("right_wrist", "right_hand", "r_wrist", "r_hand", "wrist_r");
        alias("left_hip", "left_leg", "l_hip", "l_leg", "left_thigh", "hip_l");
        alias("left_knee", "left_shin", "l_knee", "l_shin", "knee_l");
        alias("left_ankle", "left_foot", "l_ankle", "l_foot", "ankle_l");
        alias("right_hip", "right_leg", "r_hip", "r_leg", "right_thigh", "hip_r");
        alias("right_knee", "right_shin", "r_knee", "r_shin", "knee_r");
        alias("right_ankle", "right_foot", "r_ankle", "r_foot", "ankle_r");
    }

    /**
     * Recursively updates Forward Kinematics world coordinates from root down to extremities.
     */
    public void updateKinematics() {
        root.startX = root.endX = x;
        root.startY = root.endY = y;
        root.worldAngle = 0.0;
        for (Joint child : root.children) {
            updateJointFK(child, root);
        }
    }

    private void updateJointFK(Joint joint, Joint parent) {
        joint.startX = parent.endX;
        joint.startY = parent.endY;
        joint.worldAngle = parent.worldAngle + joint.baseAngle + joint.angle;

        double rad = Math.toRadians(joint.worldAngle);
        joint.endX = joint.startX + joint.length * scale * Math.cos(rad);
        joint.endY = joint.startY + joint.length * scale * Math.sin(rad);

        for (Joint child : joint.children) {
            updateJointFK(child, joint);
        }
    }

    public Joint getJoint(String name) {
        if (name == null) return null;
        String norm = name.trim().toLowerCase().replace("-", "_").replace(" ", "_");
        if (aliases.containsKey(norm)) norm = aliases.get(norm);
        return joints.get(norm);
    }

    public Collection<Joint> getJoints() { return Collections.unmodifiableCollection(joints.values()); }
    public Set<String> getJointNames() { return Collections.unmodifiableSet(joints.keySet()); }
    public Joint getRoot() { return root; }

    public Stickman rotate(String jointName, double angleDegrees) {
        Joint j = getJoint(jointName);
        if (j != null) { j.setAngle(angleDegrees); updateKinematics(); }
        return this;
    }

    public Stickman rotateBy(String jointName, double deltaDegrees) {
        Joint j = getJoint(jointName);
        if (j != null) { j.rotateBy(deltaDegrees); updateKinematics(); }
        return this;
    }

    public Stickman moveTo(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        updateKinematics();
        return this;
    }

    public Stickman moveBy(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        updateKinematics();
        return this;
    }

    public void resetPose() {
        for (Joint j : joints.values()) j.setAngle(0.0);
        updateKinematics();
    }

    public double getX() { return x; }
    public void setX(double x) { moveTo(x, this.y); }
    public double getY() { return y; }
    public void setY(double y) { moveTo(this.x, y); }
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = Math.max(0.05, scale); updateKinematics(); }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    // =========================================================================
    // Rendering & Built-in Window (No Frills)
    // =========================================================================

    public void render(Graphics2D g2) {
        updateKinematics();
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke((float) (strokeWidth * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);

        // Draw bones
        for (Joint j : joints.values()) {
            if (j.length > 0.1) {
                g.draw(new Line2D.Double(j.startX, j.startY, j.endX, j.endY));
            }
        }

        // Draw head circle
        Joint head = getJoint("head");
        if (head != null) {
            double r = 18.0 * scale;
            g.draw(new Ellipse2D.Double(head.endX - r, head.endY - r * 1.3, r * 2, r * 2));
        }
        g.dispose();
    }

    public JFrame showWindow() {
        return showWindow(600, 600, "Stickman");
    }

    public JFrame showWindow(int width, int height, String title) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        if (window != null) {
            window.setVisible(true);
            return window;
        }

        window = new JFrame(title);
        window.setSize(width, height);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Ground guide line
                int groundY = (int) (getHeight() * 0.85);
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, groundY, getWidth(), groundY);

                render(g2);
            }
        };
        canvas.setBackground(new Color(248, 250, 252));
        window.add(canvas);
        window.setVisible(true);
        return window;
    }

    public void repaint() {
        if (canvas != null) canvas.repaint();
    }

    public JFrame getWindow() { return window; }

    // =========================================================================
    // Animation Playback
    // =========================================================================

    public void play(List<Command> commands) { play(commands, false, null); }
    public void play(Command... commands) { play(Arrays.asList(commands), false, null); }
    public void play(List<Command> commands, boolean loop) { play(commands, loop, null); }

    public void play(List<Command> commands, boolean loop, Runnable onComplete) {
        stop();
        if (commands == null || commands.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        if (window == null && !GraphicsEnvironment.isHeadless()) {
            showWindow();
        }

        this.activeCommands = new ArrayList<>(commands);
        this.loopAnimation = loop;
        this.onCompleteCallback = onComplete;
        this.currentCommandIndex = 0;
        this.isPlaying = true;
        this.commandStartTime = System.currentTimeMillis();

        activeCommands.get(0).start(this);

        timer = new javax.swing.Timer(16, e -> {
            if (!isPlaying || activeCommands == null || currentCommandIndex >= activeCommands.size()) {
                stop();
                if (onCompleteCallback != null) onCompleteCallback.run();
                return;
            }

            Command current = activeCommands.get(currentCommandIndex);
            long elapsed = System.currentTimeMillis() - commandStartTime;
            int dur = current.getDuration();
            double progress = dur <= 0 ? 1.0 : Math.min(1.0, (double) elapsed / dur);

            current.update(this, progress);
            repaint();

            if (progress >= 1.0) {
                current.finish(this);
                currentCommandIndex++;
                if (currentCommandIndex < activeCommands.size()) {
                    commandStartTime = System.currentTimeMillis();
                    activeCommands.get(currentCommandIndex).start(this);
                } else if (loopAnimation) {
                    currentCommandIndex = 0;
                    commandStartTime = System.currentTimeMillis();
                    activeCommands.get(0).start(this);
                } else {
                    stop();
                    if (onCompleteCallback != null) onCompleteCallback.run();
                }
            }
        });
        timer.start();
    }

    public void stop() {
        isPlaying = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public boolean isPlaying() { return isPlaying; }
}

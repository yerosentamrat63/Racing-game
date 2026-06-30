package com.taxigame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaxiGame extends SimpleApplication {

    private State state = State.MENU;
    private final Set<String> pressed = new HashSet<String>();

    // World
    private Node gameRoot;
    private City city;
    private Taxi taxi;
    private final List<Passenger> passengers = new ArrayList<Passenger>();
    private final List<Obstacle> obstacles = new ArrayList<Obstacle>();
    private final List<Pedestrian> pedestrians = new ArrayList<Pedestrian>();

    // Active delivery
    private Passenger carriedPassenger;
    private Passenger targetPassenger;
    private Vector3f dropOffPos;
    private float pickupDistance = 0.0f;

    // Scoring
    private float totalFareEarned = 0.0f;
    private int tripsCompleted = 0;
    private int collisions = 0;
    private float timeAlive = 0.0f;

    // Streak system
    private int deliveryStreak = 0;
    private float bestStreak = 0.0f;

    // Messages
    private float messageTimer = 0.0f;
    private String message = "";

    // HUD elements
    private BitmapText hudText;
    private BitmapText scoreText;
    private BitmapText messageText;
    private BitmapText menuTitle;
    private BitmapText menuPrompt;
    private BitmapText instructionsText;

    // Compass arrow
    private Node compassArrow;
    private Geometry arrowShaft;
    private Geometry arrowHead;
    private Node targetMarker;

    // Minimap (2D GUI overlay)
    private static final int MM_SIZE = 160;
    private static final int MM_MARGIN = 12;
    private static final float CITY_RANGE = 72.0f;
    private Node minimapNode;
    private Geometry minimapBg;
    private Geometry minimapTaxiDot;
    private Geometry minimapTargetDot;

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (isPressed) {
            this.pressed.add(name);
        } else {
            this.pressed.remove(name);
        }
        if (!isPressed) {
            return;
        }
        switch (this.state.ordinal()) {
            case 0: {
                if ("Enter".equals(name)) {
                    this.startGame();
                    break;
                }
                if ("I".equals(name)) {
                    this.showInstructions();
                    break;
                }
                if (!"Escape".equals(name)) break;
                this.stop();
                break;
            }
            case 1: {
                if (!"Enter".equals(name) && !"Escape".equals(name)) break;
                this.showMenu();
                break;
            }
            case 2: {
                if ("Escape".equals(name)) {
                    this.state = State.PAUSED;
                    break;
                }
                if ("Pause".equals(name)) {
                    this.state = State.PAUSED;
                    break;
                }
                if (!"Reset".equals(name) || this.taxi == null) break;
                this.taxi.setPosition(new Vector3f(0.0f, 0.0f, 12.0f));
                this.showMessage("Position reset", 1.5f);
                break;
            }
            case 3: {
                if (!"Escape".equals(name) && !"Pause".equals(name) && !"Enter".equals(name)) break;
                this.state = State.PLAYING;
            }
        }
    };

    @Override
    public void simpleInitApp() {
        this.setDisplayStatView(false);
        this.setDisplayFps(false);
        this.flyCam.setEnabled(false);
        this.inputManager.setCursorVisible(true);
        this.setupLighting();
        this.setupInput();
        this.createHud();
        this.showMenu();
    }

    private void setupLighting() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.4f, -1.0f, -0.3f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.4f));
        this.rootNode.addLight(sun);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.55f));
        this.rootNode.addLight(ambient);
    }

    private void setupInput() {
        this.inputManager.addMapping("Up", new KeyTrigger(17), new KeyTrigger(200));
        this.inputManager.addMapping("Down", new KeyTrigger(31), new KeyTrigger(208));
        this.inputManager.addMapping("Left", new KeyTrigger(30), new KeyTrigger(203));
        this.inputManager.addMapping("Right", new KeyTrigger(32), new KeyTrigger(205));
        this.inputManager.addMapping("Enter", new KeyTrigger(28));
        this.inputManager.addMapping("I", new KeyTrigger(23));
        this.inputManager.addMapping("Escape", new KeyTrigger(1));
        this.inputManager.addMapping("Reset", new KeyTrigger(19));
        this.inputManager.addMapping("Pause", new KeyTrigger(25));
        this.inputManager.addMapping("Handbrake", new KeyTrigger(57));
        this.inputManager.addListener(this.actionListener,
                "Up", "Down", "Left", "Right", "Enter", "I", "Escape", "Reset", "Pause", "Handbrake");
    }

    private void createHud() {
        this.hudText = this.makeText(20.0f, ColorRGBA.White, 0.0f, 0.0f);
        this.scoreText = this.makeText(36.0f, ColorRGBA.Yellow, 0.0f, 0.0f);
        this.messageText = this.makeText(26.0f, ColorRGBA.Cyan, 0.0f, 0.0f);
        this.menuTitle = this.makeText(56.0f, ColorRGBA.Yellow, 0.0f, 0.0f);
        this.menuPrompt = this.makeText(22.0f, ColorRGBA.White, 0.0f, 0.0f);
        this.instructionsText = this.makeText(20.0f, ColorRGBA.White, 0.0f, 0.0f);
        this.guiNode.attachChild(this.hudText);
        this.guiNode.attachChild(this.scoreText);
        this.guiNode.attachChild(this.messageText);
        this.guiNode.attachChild(this.menuTitle);
        this.guiNode.attachChild(this.menuPrompt);
        this.guiNode.attachChild(this.instructionsText);
    }

    private BitmapText makeText(float size, ColorRGBA colour, float x, float y) {
        BitmapText t = new BitmapText(this.guiFont);
        t.setSize(size);
        t.setColor(colour);
        t.setLocalTranslation(x, y, 0.0f);
        return t;
    }

    private void hideOverlayText() {
        this.menuTitle.setText("");
        this.menuPrompt.setText("");
        this.instructionsText.setText("");
    }

    // ========== MINIMAP (2D GUI overlay) ==========

    private void createMinimap() {
        this.minimapNode = new Node("Minimap");
        this.minimapBg = new Geometry("MMBg", new Quad(MM_SIZE, MM_SIZE));
        Material bgMat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.12f, 0.18f, 0.08f, 0.85f));
        this.minimapBg.setMaterial(bgMat);
        this.minimapBg.setLocalTranslation(MM_MARGIN, MM_MARGIN, 0.0f);
        this.minimapNode.attachChild(this.minimapBg);

        Material roadMat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        roadMat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 0.7f));
        int half = 5;
        float roadW = 12.0f;
        for (int iz = -half; iz <= half; iz++) {
            if (Math.abs(iz) % 2 == 0) continue;
            float mmX = ((float) iz * 12.0f + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
            float wPx = roadW / (CITY_RANGE * 2.0f) * MM_SIZE;
            Geometry rg = new Geometry("MMRoad", new Quad(MM_SIZE, wPx));
            rg.setMaterial(roadMat);
            rg.setLocalTranslation(MM_MARGIN, MM_MARGIN + MM_SIZE - mmX - wPx / 2.0f, 0.1f);
            this.minimapNode.attachChild(rg);
        }
        for (int ix = -half; ix <= half; ix++) {
            if (Math.abs(ix) % 2 == 0) continue;
            float mmY = ((float) ix * 12.0f + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
            float wPx = roadW / (CITY_RANGE * 2.0f) * MM_SIZE;
            Geometry rg = new Geometry("MMRoad2", new Quad(wPx, MM_SIZE));
            rg.setMaterial(roadMat);
            rg.setLocalTranslation(MM_MARGIN + mmY - wPx / 2.0f, MM_MARGIN, 0.1f);
            this.minimapNode.attachChild(rg);
        }

        this.minimapTaxiDot = this.createMinimapDot("MMTaxi", ColorRGBA.Yellow, 5.0f);
        this.minimapNode.attachChild(this.minimapTaxiDot);
        this.minimapTargetDot = this.createMinimapDot("MMTarget", ColorRGBA.Green, 4.0f);
        this.minimapTargetDot.setCullHint(Spatial.CullHint.Always);
        this.minimapNode.attachChild(this.minimapTargetDot);
        this.guiNode.attachChild(this.minimapNode);
    }

    private Geometry createMinimapDot(String name, ColorRGBA color, float size) {
        Geometry dot = new Geometry(name, new Quad(size, size));
        Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        dot.setMaterial(mat);
        return dot;
    }

    private void updateMinimap() {
        if (this.minimapTaxiDot == null || this.taxi == null) return;
        Vector3f pos = this.taxi.getPosition();
        float mmX = (pos.x + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
        float mmY = (pos.z + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
        this.minimapTaxiDot.setLocalTranslation(
            MM_MARGIN + mmX - 2.5f, MM_MARGIN + MM_SIZE - mmY - 2.5f, 1.0f);

        Vector3f targetPos = null;
        if (this.carriedPassenger != null && this.dropOffPos != null) {
            targetPos = this.dropOffPos;
            this.minimapTargetDot.setCullHint(Spatial.CullHint.Never);
        } else if (this.targetPassenger != null && this.passengers.contains(this.targetPassenger)) {
            targetPos = this.targetPassenger.getPosition();
            this.minimapTargetDot.setCullHint(Spatial.CullHint.Never);
        }
        if (targetPos != null) {
            float tmX = (targetPos.x + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
            float tmY = (targetPos.z + CITY_RANGE) / (CITY_RANGE * 2.0f) * MM_SIZE;
            this.minimapTargetDot.setLocalTranslation(
                MM_MARGIN + tmX - 2.0f, MM_MARGIN + MM_SIZE - tmY - 2.0f, 1.0f);
        } else {
            this.minimapTargetDot.setCullHint(Spatial.CullHint.Always);
        }
    }

    private void destroyMinimap() {
        if (this.minimapNode != null) {
            this.guiNode.detachChild(this.minimapNode);
            this.minimapNode = null;
        }
        this.minimapBg = null;
        this.minimapTaxiDot = null;
        this.minimapTargetDot = null;
    }

    // ========== MENU / INSTRUCTIONS ==========

    private void showMenu() {
        this.state = State.MENU;
        this.pressed.clear();
        this.clearWorld();
        this.hideOverlayText();
        int w = this.settings.getWidth();
        int h = this.settings.getHeight();
        this.menuTitle.setText("ADDIS  ABABA  TAXI  RACING");
        this.menuTitle.setLocalTranslation((float) w / 2.0f - this.menuTitle.getLineWidth() / 2.0f, (float) h * 0.7f, 0.0f);
        this.menuPrompt.setText("ENTER  -  Drive!\nI      -  Instructions\nESC    -  Quit");
        this.menuPrompt.setLocalTranslation((float) w / 2.0f - this.menuPrompt.getLineWidth() / 2.0f, (float) h * 0.5f, 0.0f);
        this.cam.setLocation(new Vector3f(0.0f, 50.0f, 50.0f));
        this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        this.hudText.setText("");
        this.messageText.setText("");
    }

    private void showInstructions() {
        this.state = State.INSTRUCTIONS;
        this.hideOverlayText();
        int w = this.settings.getWidth();
        int h = this.settings.getHeight();
        this.instructionsText.setText(
            "ADDIS ABABA TAXI RACING\n\n" +
            "Drive your taxi through the busy streets of Addis Ababa!\n" +
            "Pick up passengers and deliver them safely.\n\n" +
            "CONTROLS\n" +
            "   W / Up Arrow    - Accelerate\n" +
            "   S / Down Arrow  - Brake / Reverse\n" +
            "   A / Left Arrow  - Steer Left\n" +
            "   D / Right Arrow - Steer Right\n" +
            "   SPACE           - Handbrake (hold to slide)\n" +
            "   R               - Reset position\n" +
            "   P               - Pause\n" +
            "   ESC             - Pause / Back\n\n" +
            "HOW TO PLAY\n" +
            "   * ORANGE arrow points to the nearest waiting passenger.\n" +
            "   * GREEN arrow points to their destination.\n" +
            "   * Fare = distance-based ($50 base + $1/meter).\n" +
            "   * Consecutive clean deliveries build a STREAK.\n" +
            "   * Streak multiplier: x2 at 3, x3 at 6, x4 at 10.\n" +
            "   * Crash resets your streak to 0.\n" +
            "   * Watch out for other taxis, buses, boda-bodas!\n" +
            "   * MINIMAP in bottom-left shows your position.\n\n" +
            "Press ENTER or ESC to return to the menu"
        );
        this.instructionsText.setLocalTranslation((float) w / 2.0f - 250.0f, (float) h * 0.92f, 0.0f);
    }

    // ========== START / CLEAR ==========

    private void startGame() {
        this.state = State.PLAYING;
        this.hideOverlayText();
        this.totalFareEarned = 0.0f;
        this.tripsCompleted = 0;
        this.collisions = 0;
        this.timeAlive = 0.0f;
        this.deliveryStreak = 0;
        this.bestStreak = 0.0f;
        this.messageTimer = 0.0f;
        this.message = "";

        this.gameRoot = new Node("GameRoot");
        this.rootNode.attachChild(this.gameRoot);

        this.city = new City(this.assetManager, this.gameRoot);
        this.city.build();

        this.taxi = new Taxi(this.assetManager, this.gameRoot);
        this.taxi.setPosition(new Vector3f(0.0f, 0.0f, 12.0f));

        // Spawn 15 AI traffic vehicles (taxis, buses, boda-bodas, sedans)
        this.obstacles.clear();
        for (int i = 0; i < 15; ++i) {
            Vector3f pos = this.randomRoadPosition();
            Obstacle o = new Obstacle(this.assetManager, this.gameRoot, this.city, pos);
            this.obstacles.add(o);
        }

        // Spawn passenger waiting areas
        this.passengers.clear();
        this.carriedPassenger = null;
        this.targetPassenger = null;
        this.dropOffPos = null;
        this.pickupDistance = 0.0f;
        this.spawnPassenger();

        // Spawn pedestrians on sidewalks
        this.pedestrians.clear();
        for (int i = 0; i < 20; ++i) {
            Vector3f pos = this.randomSidewalkPosition();
            Pedestrian ped = new Pedestrian(this.assetManager, this.gameRoot, pos);
            this.pedestrians.add(ped);
        }

        this.createCompassArrow();
        this.createMinimap();

        this.showMessage("Welcome to Addis Ababa! Follow the arrow to find passengers.", 5.0f);
    }

    private void clearWorld() {
        if (this.gameRoot != null) {
            this.rootNode.detachChild(this.gameRoot);
            this.gameRoot = null;
        }
        if (this.compassArrow != null) {
            this.rootNode.detachChild(this.compassArrow);
            this.compassArrow = null;
        }
        if (this.targetMarker != null) {
            this.rootNode.detachChild(this.targetMarker);
            this.targetMarker = null;
        }
        this.passengers.clear();
        this.obstacles.clear();
        this.pedestrians.clear();
        this.carriedPassenger = null;
        this.targetPassenger = null;
        this.dropOffPos = null;
        this.city = null;
        this.taxi = null;
        this.destroyMinimap();
    }

    // ========== COMPASS ARROW ==========

    private void createCompassArrow() {
        this.compassArrow = new Node("CompassArrow");
        Material yellowMat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        yellowMat.setBoolean("UseMaterialColors", true);
        yellowMat.setColor("Diffuse", ColorRGBA.Yellow);
        yellowMat.setColor("Ambient", ColorRGBA.Yellow);
        yellowMat.setColor("Specular", ColorRGBA.White);
        yellowMat.setFloat("Shininess", 64.0f);

        this.arrowShaft = new Geometry("ArrowShaft", new Box(0.12f, 0.06f, 1.0f));
        this.arrowShaft.setMaterial(yellowMat);
        this.arrowShaft.setLocalTranslation(0.0f, 0.0f, -0.5f);
        this.compassArrow.attachChild(this.arrowShaft);

        float headWidth = 0.5f;
        float headLength = 0.6f;
        float headThickness = 0.06f;
        Box headBox = new Box(headWidth / 2.0f, headThickness, headLength / 2.0f);
        this.arrowHead = new Geometry("ArrowHead", headBox);
        this.arrowHead.setMaterial(yellowMat);
        this.arrowHead.setLocalTranslation(0.0f, 0.0f, -1.0f - headLength / 2.0f);
        this.compassArrow.attachChild(this.arrowHead);

        this.compassArrow.setLocalTranslation(0.0f, 5.0f, 0.0f);
        this.rootNode.attachChild(this.compassArrow);

        Material orangeMat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        orangeMat.setBoolean("UseMaterialColors", true);
        orangeMat.setColor("Diffuse", new ColorRGBA(1.0f, 0.6f, 0.0f, 1.0f));
        orangeMat.setColor("Ambient", new ColorRGBA(1.0f, 0.6f, 0.0f, 1.0f));
        orangeMat.setColor("Specular", ColorRGBA.White);
        orangeMat.setFloat("Shininess", 64.0f);

        this.targetMarker = new Node("TargetMarker");
        Geometry markerShaft = new Geometry("MarkerShaft", new Box(0.08f, 0.7f, 0.08f));
        markerShaft.setMaterial(orangeMat);
        markerShaft.setLocalTranslation(0.0f, 0.7f, 0.0f);
        this.targetMarker.attachChild(markerShaft);

        Box wingBox = new Box(0.2f, 0.04f, 0.04f);
        Geometry wing1 = new Geometry("Wing1", wingBox);
        wing1.setMaterial(orangeMat);
        wing1.rotate(0.0f, 0.0f, (float) (Math.PI / 4.0));
        this.targetMarker.attachChild(wing1);

        Geometry wing2 = new Geometry("Wing2", wingBox);
        wing2.setMaterial(orangeMat);
        wing2.rotate(0.0f, 0.0f, (float) (-Math.PI / 4.0));
        this.targetMarker.attachChild(wing2);

        this.targetMarker.setLocalTranslation(0.0f, 4.0f, 0.0f);
        this.rootNode.attachChild(this.targetMarker);
    }

    private void updateCompassArrow(Vector3f taxiPos, float taxiHeading) {
        if (this.compassArrow == null) return;

        float bob = FastMath.sin(this.timeAlive * 3.0f) * 0.4f;
        Vector3f targetPos = null;

        if (this.carriedPassenger != null && this.dropOffPos != null) {
            targetPos = this.dropOffPos;
            this.setArrowColor(ColorRGBA.Green);
            this.setMarkerColor(new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f));
        } else if (this.carriedPassenger == null) {
            if (this.targetPassenger == null || !this.passengers.contains(this.targetPassenger)) {
                this.targetPassenger = null;
                float closestDist = Float.MAX_VALUE;
                for (Passenger p : this.passengers) {
                    float dist = taxiPos.distance(p.getPosition());
                    if (dist < closestDist) {
                        closestDist = dist;
                        this.targetPassenger = p;
                    }
                }
            }
            if (this.targetPassenger != null) {
                targetPos = this.targetPassenger.getPosition();
                this.setArrowColor(ColorRGBA.Yellow);
                this.setMarkerColor(new ColorRGBA(1.0f, 0.6f, 0.0f, 1.0f));
            }
        }

        if (targetPos != null) {
            this.compassArrow.setCullHint(Spatial.CullHint.Never);
            this.compassArrow.setLocalTranslation(taxiPos.x, taxiPos.y + 5.0f, taxiPos.z);
            float dx = targetPos.x - taxiPos.x;
            float dz = targetPos.z - taxiPos.z;
            float angle = (float) Math.atan2(-dx, -dz);
            this.compassArrow.setLocalRotation(new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y));

            this.targetMarker.setCullHint(Spatial.CullHint.Never);
            this.targetMarker.setLocalTranslation(targetPos.x, 4.0f + bob, targetPos.z);
        } else {
            this.compassArrow.setCullHint(Spatial.CullHint.Always);
            this.targetMarker.setCullHint(Spatial.CullHint.Always);
        }
    }

    private void setArrowColor(ColorRGBA color) {
        Material mat = this.arrowShaft.getMaterial();
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        mat = this.arrowHead.getMaterial();
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
    }

    private void setMarkerColor(ColorRGBA color) {
        for (Spatial child : this.targetMarker.getChildren()) {
            if (child instanceof Geometry) {
                Material mat = ((Geometry) child).getMaterial();
                mat.setColor("Diffuse", color);
                mat.setColor("Ambient", color);
            }
        }
    }

    // ========== SPAWNING ==========

    private Vector3f randomRoadPosition() {
        for (int attempt = 0; attempt < 50; ++attempt) {
            int ix = FastMath.rand.nextInt(5) - 2;
            int iz = FastMath.rand.nextInt(5) - 2;
            if (!this.city.isRoadTile(ix, iz)) continue;
            float x = (float) ix * 12.0f;
            float z = (float) iz * 12.0f;
            x += (FastMath.rand.nextFloat() - 0.5f) * 12.0f * 0.4f;
            z += (FastMath.rand.nextFloat() - 0.5f) * 12.0f * 0.4f;
            float safeLimit = 45.0f;
            x = Math.max(-safeLimit, Math.min(safeLimit, x));
            z = Math.max(-safeLimit, Math.min(safeLimit, z));
            if (this.taxi != null) {
                Vector3f pos = new Vector3f(x, 0.0f, z);
                if (!(pos.distance(this.taxi.getPosition()) > 8.0f)) continue;
            }
            return new Vector3f(x, 0.0f, z);
        }
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }

    private Vector3f randomSidewalkPosition() {
        for (int attempt = 0; attempt < 50; ++attempt) {
            int ix = FastMath.rand.nextInt(5) - 2;
            int iz = FastMath.rand.nextInt(5) - 2;
            if (this.city.isRoadTile(ix, iz)) continue;
            float cx = (float) ix * 12.0f;
            float cz = (float) iz * 12.0f;
            float edge = 5.76f;
            int side = FastMath.rand.nextInt(4);
            float x = cx;
            float z = cz;
            switch (side) {
                case 0: z = cz - edge; break;
                case 1: z = cz + edge; break;
                case 2: x = cx - edge; break;
                case 3: x = cx + edge; break;
            }
            boolean insideBuilding = false;
            for (City.Bounds b : this.city.getBuildingBounds()) {
                if (b.contains(x, z, 0.5f)) {
                    insideBuilding = true;
                    break;
                }
            }
            if (insideBuilding) continue;
            if (this.taxi != null) {
                Vector3f pos = new Vector3f(x, 0.0f, z);
                if (!(pos.distance(this.taxi.getPosition()) > 12.0f)) continue;
            }
            return new Vector3f(x, 0.0f, z);
        }
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }

    private void spawnPassenger() {
        Vector3f pos = this.randomSidewalkPosition();
        Passenger p = new Passenger(this.assetManager, this.gameRoot, pos);
        this.passengers.add(p);
    }

    private void spawnDropOff() {
        Vector3f pos = this.randomRoadPosition();
        while (this.taxi != null && pos.distance(this.taxi.getPosition()) < 20.0f) {
            pos = this.randomRoadPosition();
        }
        this.dropOffPos = pos;
    }

    // ========== MESSAGES ==========

    private void showMessage(String msg, float seconds) {
        this.message = msg;
        this.messageTimer = seconds;
    }

    // ========== MAIN UPDATE ==========

    @Override
    public void simpleUpdate(float tpf) {
        if (this.state == State.PLAYING) {
            this.updateGame(tpf);
        }
        if (this.messageTimer > 0.0f) {
            this.messageTimer -= tpf;
            if (this.messageTimer <= 0.0f) {
                this.message = "";
            }
        }
        if (this.state == State.PLAYING) {
            int w = this.settings.getWidth();
            int h = this.settings.getHeight();
            this.messageText.setText(this.message);
            this.messageText.setLocalTranslation(
                (float) w / 2.0f - this.messageText.getLineWidth() / 2.0f,
                (float) h * 0.88f, 0.0f);
            String streakStr = this.deliveryStreak > 0
                ? String.format("  |  Streak: %d (x%.0f)", this.deliveryStreak, getMultiplier())
                : "";
            this.scoreText.setText(String.format("$%.0f%s", this.totalFareEarned, streakStr));
            this.scoreText.setLocalTranslation(
                (float) w - this.scoreText.getLineWidth() - 20.0f,
                (float) h - 30.0f, 0.0f);
        } else if (this.state == State.PAUSED) {
            int w = this.settings.getWidth();
            int h = this.settings.getHeight();
            this.messageText.setText("PAUSED  -  press P or ESC to resume");
            this.messageText.setLocalTranslation(
                (float) w / 2.0f - this.messageText.getLineWidth() / 2.0f,
                (float) h / 2.0f, 0.0f);
        } else {
            this.messageText.setText("");
            this.scoreText.setText("");
        }
    }

    private float getMultiplier() {
        if (this.deliveryStreak >= 10) return 4.0f;
        if (this.deliveryStreak >= 6) return 3.0f;
        if (this.deliveryStreak >= 3) return 2.0f;
        return 1.0f;
    }

    private void updateGame(float tpf) {
        this.timeAlive += tpf;

        boolean fwd = this.pressed.contains("Up");
        boolean back = this.pressed.contains("Down");
        boolean left = this.pressed.contains("Left");
        boolean right = this.pressed.contains("Right");
        boolean handbrake = this.pressed.contains("Handbrake");

        this.taxi.update(tpf, fwd, back, left, right, handbrake, this.city);

        Vector3f taxiPos = this.taxi.getPosition();
        float heading = this.taxi.getHeading();

        // Chase camera
        Vector3f offset = new Vector3f(0.0f, 7.0f, -12.0f);
        Quaternion q = new Quaternion().fromAngleNormalAxis(heading, Vector3f.UNIT_Y);
        q.multLocal(offset);
        Vector3f desired = taxiPos.add(offset);
        this.cam.getLocation().interpolateLocal(desired, Math.min(1.0f, tpf * 8.0f));
        Vector3f lookAt = taxiPos.add(0.0f, 1.5f, 0.0f);
        this.cam.lookAt(lookAt, Vector3f.UNIT_Y);

        // Compass
        this.updateCompassArrow(taxiPos, heading);

        // Minimap
        this.updateMinimap();

        // Update pedestrians
        for (Pedestrian ped : this.pedestrians) {
            ped.update(tpf);
        }

        // Update obstacles
        for (Obstacle o : this.obstacles) {
            o.update(tpf, this.obstacles);
        }

        // Passenger pickup
        if (this.carriedPassenger == null) {
            for (Passenger p : this.passengers) {
                float d = taxiPos.distance(p.getPosition());
                if (d < p.getPickupRadius()) {
                    this.passengers.remove(p);
                    this.gameRoot.detachChild(p.getNode());
                    this.carriedPassenger = p;
                    this.targetPassenger = null;
                    this.pickupDistance = 0.0f;
                    this.spawnDropOff();
                    this.showMessage("Passenger picked up! Drive to the green arrow.", 4.0f);
                    break;
                }
            }
        }

        // Delivery
        if (this.carriedPassenger != null && this.dropOffPos != null) {
            float distToDest = taxiPos.distance(this.dropOffPos);
            this.pickupDistance += tpf * Math.abs(this.taxi.getSpeed());

            if (distToDest < 4.5f && Math.abs(this.taxi.getSpeed()) < 5.0f) {
                float baseFare = 50.0f + this.pickupDistance;
                float multiplier = getMultiplier();
                float totalFare = baseFare * multiplier;

                this.totalFareEarned += totalFare;
                this.tripsCompleted++;
                this.deliveryStreak++;
                if (this.deliveryStreak > this.bestStreak) {
                    this.bestStreak = this.deliveryStreak;
                }

                this.dropOffPos = null;
                this.carriedPassenger = null;

                String bonusStr = multiplier > 1.0f
                    ? String.format("  (x%.0f streak bonus!)", multiplier)
                    : "";
                this.showMessage(
                    String.format("Delivered! +$%.0f%s", totalFare, bonusStr),
                    4.0f);
                this.spawnPassenger();
            }
        }

        // Obstacle collisions
        for (Obstacle o : this.obstacles) {
            float d = taxiPos.distance(o.getPosition());
            if (d < 3.0f && !o.isStunned()) {
                if (this.totalFareEarned > 0.0f) {
                    this.totalFareEarned = Math.max(0.0f, this.totalFareEarned - 10.0f);
                }
                this.collisions++;
                this.deliveryStreak = 0;
                this.taxi.crashStop(o.getPosition(), this.city);
                o.stun();
                this.showMessage("Crash! -$10 | Streak reset!", 1.5f);
                break;
            }
        }

        // HUD
        float speedMph = Math.abs(this.taxi.getSpeed()) * 2.2f;
        String dest = this.carriedPassenger != null ? "GREEN arrow (destination)" : "ORANGE arrow (passenger)";
        this.hudText.setText(String.format(
            "Speed: %.0f mph   |   Fare: $%.0f   |   Trips: %d   |   Crashes: %d   |   Follow: %s",
            speedMph,
            this.totalFareEarned, this.tripsCompleted, this.collisions,
            dest));
    }

    public static enum State {
        MENU,
        INSTRUCTIONS,
        PLAYING,
        PAUSED;
    }
}

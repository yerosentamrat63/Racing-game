package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.Random;

public class Obstacle {
    private static final float SPEED = 6.0f;
    private static final float RADIUS = 1.6f;
    private static final float LANE_OFFSET = 2.5f;
    private static final float STUN_DURATION = 1.5f;
    private static final String[] CAR_MODELS = new String[]{"Models/Cars/sedan.glb", "Models/Cars/suv.glb", "Models/Cars/van.glb", "Models/Cars/police.glb", "Models/Cars/hatchback-sports.glb"};
    private final Node node;
    private final City city;
    private final Random rng = new Random();
    private float heading;
    private float turnCooldown = 0.0f;
    private float stunTimer = 0.0f;
    private int roadIndex;
    private boolean horizontal;

    public Obstacle(AssetManager am, Node parent, City city, Vector3f startPos) {
        this.city = city;
        this.node = new Node("Obstacle");
        String path = CAR_MODELS[this.rng.nextInt(CAR_MODELS.length)];
        Spatial model = am.loadModel(path);
        model.scale(1.5f);
        model.setLocalTranslation(0.0f, 0.45f, 0.0f);
        this.node.attachChild(model);

        float snappedX = Math.round(startPos.x / 12.0f) * 12.0f;
        float snappedZ = Math.round(startPos.z / 12.0f) * 12.0f;

        this.horizontal = Math.abs(Math.round(startPos.x / 12.0f)) % 2 == 1
                && Math.abs(Math.round(startPos.z / 12.0f)) % 2 == 0;

        if (this.horizontal) {
            this.heading = this.rng.nextBoolean() ? 0.0f : FastMath.PI;
            this.roadIndex = Math.round(startPos.z / 12.0f);
            float laneDir = this.heading == 0.0f ? -1.0f : 1.0f;
            float x = startPos.x;
            float z = snappedZ + LANE_OFFSET * laneDir;
            this.node.setLocalTranslation(x, 0.0f, z);
        } else {
            this.heading = this.rng.nextBoolean() ? FastMath.PI / 2.0f : -FastMath.PI / 2.0f;
            this.roadIndex = Math.round(startPos.x / 12.0f);
            float laneDir = this.heading > 0 ? 1.0f : -1.0f;
            float x = snappedX + LANE_OFFSET * laneDir;
            float z = startPos.z;
            this.node.setLocalTranslation(x, 0.0f, z);
        }

        parent.attachChild(this.node);
    }

    public void update(float tpf, List<Obstacle> allObstacles) {
        if (this.stunTimer > 0.0f) {
            this.stunTimer -= tpf;
            return;
        }
        float dx = FastMath.sin(this.heading) * SPEED * tpf;
        float dz = FastMath.cos(this.heading) * SPEED * tpf;
        float newX = this.node.getLocalTranslation().x + dx;
        float newZ = this.node.getLocalTranslation().z + dz;

        boolean blocked = !this.city.isOnRoadPrecise(newX, newZ)
                || Math.abs(newX) > 62.0f || Math.abs(newZ) > 62.0f;

        if (!blocked) {
            for (Obstacle other : allObstacles) {
                if (other == this) continue;
                float dist = new Vector3f(newX, 0.0f, newZ).distance(other.getPosition());
                if (dist < 3.5f) {
                    blocked = true;
                    break;
                }
            }
        }

        if (blocked && this.turnCooldown <= 0.0f) {
            float snapX = Math.round(this.node.getLocalTranslation().x / 12.0f) * 12.0f;
            float snapZ = Math.round(this.node.getLocalTranslation().z / 12.0f) * 12.0f;
            boolean atIntersection = this.city.isOnRoadPrecise(snapX, snapZ)
                    && Math.abs(Math.abs(this.node.getLocalTranslation().x - snapX) + Math.abs(this.node.getLocalTranslation().z - snapZ)) < 4.0f;

            if (this.horizontal) {
                this.heading = this.rng.nextBoolean() ? FastMath.PI / 2.0f : -FastMath.PI / 2.0f;
                this.horizontal = false;
                this.roadIndex = Math.round(this.node.getLocalTranslation().x / 12.0f);
            } else {
                this.heading = this.rng.nextBoolean() ? 0.0f : FastMath.PI;
                this.horizontal = true;
                this.roadIndex = Math.round(this.node.getLocalTranslation().z / 12.0f);
            }

            float laneDir = this.getLaneDirection();
            if (this.horizontal) {
                float z = snapZ + LANE_OFFSET * laneDir;
                this.node.setLocalTranslation(this.node.getLocalTranslation().x, 0.0f, z);
            } else {
                float x = snapX + LANE_OFFSET * laneDir;
                this.node.setLocalTranslation(x, 0.0f, this.node.getLocalTranslation().z);
            }

            this.turnCooldown = 0.8f;
        } else if (!blocked) {
            this.node.setLocalTranslation(newX, 0.0f, newZ);
        }
        this.turnCooldown -= tpf;
        this.node.setLocalRotation(new Quaternion().fromAngleNormalAxis(this.heading, Vector3f.UNIT_Y));
    }

    private float getLaneDirection() {
        if (this.horizontal) {
            return this.heading == 0.0f ? -1.0f : 1.0f;
        }
        return this.heading > 0 ? 1.0f : -1.0f;
    }

    public Vector3f getPosition() {
        return this.node.getLocalTranslation();
    }

    public Node getNode() {
        return this.node;
    }

    public float getRadius() {
        return 1.6f;
    }

    public void stun() {
        this.stunTimer = STUN_DURATION;
    }

    public boolean isStunned() {
        return this.stunTimer > 0.0f;
    }
}

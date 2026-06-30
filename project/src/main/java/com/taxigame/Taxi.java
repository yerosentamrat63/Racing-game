package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class Taxi {
    private static final float MAX_SPEED = 22.0f;
    private static final float REVERSE_SPEED = -8.0f;
    private static final float ACCEL = 16.0f;
    private static final float BRAKE = 24.0f;
    private static final float TURN_RATE = 2.4f;
    private static final float DRAG = 0.7f;
    private static final float HAND_BRAKE = 40.0f;
    private static final float HAND_BRAKE_TURN = 4.8f;
    private final Node node;
    private final Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private float heading = 0.0f;
    private float speed = 0.0f;

    public Taxi(AssetManager assetManager, Node parent) {
        this.node = new Node("Taxi");
        Spatial model = assetManager.loadModel("Models/Cars/taxi.glb");
        model.scale(0.013f);
        model.setLocalTranslation(0.0f, 0.0f, 0.0f);
        this.node.attachChild(model);
        parent.attachChild(this.node);
        this.updateTransform();
    }

    public void update(float tpf, boolean fwd, boolean back, boolean left, boolean right, boolean handbrake, City city) {
        if (handbrake) {
            if (this.speed > 0.0f) {
                this.speed = Math.max(0.0f, this.speed - HAND_BRAKE * tpf);
            } else if (this.speed < 0.0f) {
                this.speed = Math.min(0.0f, this.speed + HAND_BRAKE * tpf);
            }
        } else if (fwd) {
            this.speed += ACCEL * tpf;
        } else if (back) {
            this.speed -= BRAKE * tpf;
        } else if (this.speed > 0.0f) {
            this.speed = Math.max(0.0f, this.speed - DRAG * tpf);
        } else if (this.speed < 0.0f) {
            this.speed = Math.min(0.0f, this.speed + DRAG * tpf);
        }
        this.speed = Math.max(REVERSE_SPEED, Math.min(MAX_SPEED, this.speed));
        float steer = 0.0f;
        if (left) {
            steer += 1.0f;
        }
        if (right) {
            steer -= 1.0f;
        }
        float speedFactor = Math.min(1.0f, Math.abs(this.speed) / 4.0f);
        float dir = this.speed >= 0.0f ? 1.0f : -1.0f;
        float turnRate = handbrake ? HAND_BRAKE_TURN : TURN_RATE;
        this.heading += steer * turnRate * speedFactor * tpf * dir;
        float dx = FastMath.sin(this.heading) * this.speed * tpf;
        float dz = FastMath.cos(this.heading) * this.speed * tpf;
        float newX = this.position.x + dx;
        float newZ = this.position.z + dz;
        boolean blocked = false;
        List<City.Bounds> bounds = city.getBuildingBounds();
        for (City.Bounds b : bounds) {
            if (!b.contains(newX, newZ, 1.6f)) continue;
            blocked = true;
            break;
        }
        float limit = 65.0f;
        if (Math.abs(newX) > limit || Math.abs(newZ) > limit) {
            blocked = true;
        }
        if (blocked) {
            this.speed = -this.speed * 0.2f;
        } else {
            this.position.set(newX, this.position.y, newZ);
        }
        this.updateTransform();
    }

    public void pushBackFrom(Vector3f other) {
        Vector3f away = this.position.subtract(other).normalizeLocal();
        float newX = this.position.x + away.x * 2.0f;
        float newZ = this.position.z + away.z * 2.0f;
        float limit = 55.0f;
        newX = Math.max(-limit, Math.min(limit, newX));
        newZ = Math.max(-limit, Math.min(limit, newZ));
        this.position.set(newX, this.position.y, newZ);
        this.speed *= -0.3f;
        this.updateTransform();
    }

    private void updateTransform() {
        this.node.setLocalTranslation(this.position);
        this.node.setLocalRotation(new Quaternion().fromAngleNormalAxis(this.heading, Vector3f.UNIT_Y));
    }

    public void setPosition(Vector3f pos) {
        this.position.set(pos);
        this.updateTransform();
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public float getHeading() {
        return this.heading;
    }

    public Node getNode() {
        return this.node;
    }

    public float getSpeed() {
        return this.speed;
    }
}

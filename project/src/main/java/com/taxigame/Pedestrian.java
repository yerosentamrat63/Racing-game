package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;

public class Pedestrian {
    private final Node node;
    private final Vector3f homePos;
    private final Random rng;
    private float wanderTimer;
    private float speed;
    private Vector3f wanderDir;

    public Pedestrian(AssetManager am, Node parent, Vector3f pos) {
        this.node = new Node("Pedestrian");
        this.homePos = pos.clone();
        this.rng = new Random();
        this.wanderTimer = 0.0f;
        this.speed = 1.2f + this.rng.nextFloat() * 0.8f;
        this.wanderDir = new Vector3f();

        String[] models = {
            "Models/Passengers/character-a.glb",
            "Models/Passengers/character-b.glb",
            "Models/Passengers/character-c.glb",
            "Models/Passengers/character-d.glb",
            "Models/Passengers/character-e.glb"
        };
        String path = models[this.rng.nextInt(models.length)];
        Spatial model = am.loadModel(path);
        model.scale(0.6f);
        model.setLocalTranslation(0.0f, 0.15f, 0.0f);
        model.rotate(0.0f, this.rng.nextFloat() * FastMath.TWO_PI, 0.0f);
        this.node.attachChild(model);
        this.node.setLocalTranslation(pos);
        parent.attachChild(this.node);
        pickNewDirection();
    }

    public void update(float tpf) {
        this.wanderTimer -= tpf;
        if (this.wanderTimer <= 0.0f) {
            pickNewDirection();
        }

        Vector3f pos = this.node.getLocalTranslation();
        float newX = pos.x + this.wanderDir.x * this.speed * tpf;
        float newZ = pos.z + this.wanderDir.z * this.speed * tpf;

        // Stay close to home
        float distFromHome = new Vector3f(newX, 0, newZ).distance(this.homePos);
        if (distFromHome > 4.0f) {
            // Turn back toward home
            this.wanderDir.set(this.homePos.x - pos.x, 0, this.homePos.z - pos.z).normalizeLocal();
            this.wanderTimer = 0.5f;
            return;
        }

        this.node.setLocalTranslation(newX, 0.0f, newZ);
    }

    private void pickNewDirection() {
        float angle = this.rng.nextFloat() * FastMath.TWO_PI;
        this.wanderDir.set(FastMath.sin(angle), 0, FastMath.cos(angle)).normalizeLocal();
        this.wanderTimer = 1.5f + this.rng.nextFloat() * 3.0f;
        this.speed = 0.8f + this.rng.nextFloat() * 1.2f;
    }

    public Vector3f getPosition() {
        return this.node.getLocalTranslation();
    }

    public Node getNode() {
        return this.node;
    }
}

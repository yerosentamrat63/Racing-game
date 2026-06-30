/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;

public class Passenger {
    private static final float SCALE = 0.2f;
    private static final float PICKUP_RADIUS = 3.5f;
    private final Node node;
    private float phase = new Random().nextFloat() * ((float)Math.PI * 2);

    public Passenger(AssetManager am, Node parent, Vector3f pos) {
        this.node = new Node("Passenger");
        String[] models = new String[]{"Models/Passengers/character-a.glb", "Models/Passengers/character-b.glb", "Models/Passengers/character-c.glb", "Models/Passengers/character-d.glb", "Models/Passengers/character-e.glb"};
        String path = models[(int)(Math.random() * (double)models.length)];
        Spatial model = am.loadModel(path);
        model.scale(0.8f);
        model.setLocalTranslation(0.0f, 0.2f, 0.0f);
        model.rotate(0.0f, FastMath.rand.nextFloat() * ((float)Math.PI * 2), 0.0f);
        this.node.attachChild(model);
        this.node.setLocalTranslation(pos);
        parent.attachChild(this.node);
    }

    public void update(float tpf) {
        this.phase += tpf * 2.0f;
    }

    public Vector3f getPosition() {
        return this.node.getLocalTranslation();
    }

    public Node getNode() {
        return this.node;
    }

    public float getPickupRadius() {
        return 3.5f;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import java.util.Random;

public class Props {
    private final AssetManager assetManager;
    private final Node parent;
    private final Random rng = new Random(2024L);
    private Spatial lampProto;
    private Spatial planterProto;
    private Spatial treeSmallProto;
    private Spatial treeLargeProto;

    public Props(AssetManager am, Node parent) {
        this.assetManager = am;
        this.parent = parent;
    }

    public void init() {
        this.lampProto = this.loadScaled("Models/Props/light-square.glb", 3.5f);
        this.planterProto = this.loadScaled("Models/Props/planter.glb", 3.0f);
        this.treeSmallProto = this.loadScaled("Models/Props/tree-small.glb", 3.5f);
        this.treeLargeProto = this.loadScaled("Models/Props/tree-large.glb", 4.0f);
    }

    public void addStreetLamp(float x, float z) {
        if (this.lampProto == null) {
            this.init();
        }
        Spatial lamp = this.lampProto.clone();
        lamp.rotate(0.0f, this.rng.nextFloat() * ((float)Math.PI * 2), 0.0f);
        lamp.setLocalTranslation(x, 0.05f, z);
        this.parent.attachChild(lamp);
    }

    public void addPlanter(float x, float z) {
        if (this.planterProto == null) {
            this.init();
        }
        Spatial p = this.planterProto.clone();
        p.rotate(0.0f, this.rng.nextFloat() * ((float)Math.PI * 2), 0.0f);
        p.setLocalTranslation(x, 0.05f, z);
        this.parent.attachChild(p);
    }

    public void addTreeSmall(float x, float z) {
        if (this.treeSmallProto == null) {
            this.init();
        }
        Spatial t = this.treeSmallProto.clone();
        t.rotate(0.0f, this.rng.nextFloat() * ((float)Math.PI * 2), 0.0f);
        t.setLocalTranslation(x, 0.05f, z);
        this.parent.attachChild(t);
    }

    public void addTreeLarge(float x, float z) {
        if (this.treeLargeProto == null) {
            this.init();
        }
        Spatial t = this.treeLargeProto.clone();
        t.rotate(0.0f, this.rng.nextFloat() * ((float)Math.PI * 2), 0.0f);
        t.setLocalTranslation(x, 0.05f, z);
        this.parent.attachChild(t);
    }

    public void addFireHydrant(float x, float z) {
        Node hydrant = new Node("FireHydrant");
        Material red = this.colouredMat(new ColorRGBA(0.85f, 0.1f, 0.1f, 1.0f));
        Material dark = this.colouredMat(new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
        Geometry body = new Geometry("HydrantBody", new Cylinder(8, 12, 0.13f, 0.55f, true));
        body.setMaterial(red);
        body.setLocalTranslation(0.0f, 0.3f, 0.0f);
        hydrant.attachChild(body);
        Geometry cap = new Geometry("HydrantCap", new Sphere(8, 12, 0.16f));
        cap.setMaterial(red);
        cap.setLocalTranslation(0.0f, 0.62f, 0.0f);
        hydrant.attachChild(cap);
        Geometry nozzle1 = new Geometry("Nozzle1", new Cylinder(6, 8, 0.05f, 0.12f, true));
        nozzle1.setMaterial(dark);
        nozzle1.rotate(0.0f, 0.0f, 1.5707964f);
        nozzle1.setLocalTranslation(0.15f, 0.4f, 0.0f);
        hydrant.attachChild(nozzle1);
        Geometry nozzle2 = new Geometry("Nozzle2", new Cylinder(6, 8, 0.05f, 0.12f, true));
        nozzle2.setMaterial(dark);
        nozzle2.rotate(0.0f, 0.0f, 1.5707964f);
        nozzle2.setLocalTranslation(-0.15f, 0.4f, 0.0f);
        hydrant.attachChild(nozzle2);
        Geometry base = new Geometry("HydrantBase", new Box(0.18f, 0.05f, 0.18f));
        base.setMaterial(dark);
        base.setLocalTranslation(0.0f, 0.05f, 0.0f);
        hydrant.attachChild(base);
        hydrant.setLocalTranslation(x, 0.05f, z);
        this.parent.attachChild(hydrant);
    }

    public void addMarketStall(float x, float z, float facingYaw) {
        Node stall = new Node("MarketStall");
        Material cloth = this.colouredMat(new ColorRGBA(0.8f, 0.2f, 0.1f, 1.0f));
        Material wood = this.colouredMat(new ColorRGBA(0.45f, 0.3f, 0.15f, 1.0f));
        Material crate = this.colouredMat(new ColorRGBA(0.55f, 0.4f, 0.2f, 1.0f));

        // Canopy
        Geometry canopy = new Geometry("Canopy", new Box(1.5f, 0.05f, 1.0f));
        canopy.setMaterial(cloth);
        canopy.setLocalTranslation(0.0f, 2.2f, 0.0f);
        stall.attachChild(canopy);

        // Back panel
        Geometry back = new Geometry("Back", new Box(1.5f, 1.0f, 0.05f));
        back.setMaterial(wood);
        back.setLocalTranslation(0.0f, 1.0f, 0.95f);
        stall.attachChild(back);

        // Table
        Geometry table = new Geometry("Table", new Box(1.4f, 0.06f, 0.6f));
        table.setMaterial(wood);
        table.setLocalTranslation(0.0f, 0.7f, 0.15f);
        stall.attachChild(table);

        // Legs
        for (float lx = -1.2f; lx <= 1.2f; lx += 2.4f) {
            for (float lz = -0.3f; lz <= 0.6f; lz += 0.9f) {
                Geometry leg = new Geometry("Leg", new Box(0.05f, 0.35f, 0.05f));
                leg.setMaterial(wood);
                leg.setLocalTranslation(lx, 0.35f, lz);
                stall.attachChild(leg);
            }
        }

        // Crates on table
        for (int i = 0; i < 3; i++) {
            Geometry c = new Geometry("Crate" + i, new Box(0.2f, 0.15f, 0.2f));
            c.setMaterial(crate);
            c.setLocalTranslation(-0.6f + i * 0.5f, 0.85f, 0.15f);
            stall.attachChild(c);
        }

        stall.setLocalRotation(new Quaternion().fromAngleNormalAxis(facingYaw, Vector3f.UNIT_Y));
        stall.setLocalTranslation(x, 0.2f, z);
        this.parent.attachChild(stall);
    }

    public void addConstructionBarrier(float x, float z, float facingYaw) {
        Node barrier = new Node("Barrier");
        Material orange = this.colouredMat(new ColorRGBA(0.9f, 0.5f, 0.0f, 1.0f));
        Material white = this.colouredMat(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));

        // Main bar
        Geometry bar = new Geometry("Bar", new Box(1.8f, 0.08f, 0.08f));
        bar.setMaterial(orange);
        bar.setLocalTranslation(0.0f, 0.5f, 0.0f);
        barrier.attachChild(bar);

        // Stripes
        for (int i = 0; i < 4; i++) {
            Geometry stripe = new Geometry("Stripe" + i, new Box(0.15f, 0.08f, 0.1f));
            stripe.setMaterial(i % 2 == 0 ? orange : white);
            stripe.setLocalTranslation(-0.75f + i * 0.5f, 0.5f, 0.0f);
            barrier.attachChild(stripe);
        }

        // Posts
        Geometry post1 = new Geometry("Post1", new Box(0.06f, 0.5f, 0.06f));
        post1.setMaterial(orange);
        post1.setLocalTranslation(-0.8f, 0.25f, 0.0f);
        barrier.attachChild(post1);
        Geometry post2 = new Geometry("Post2", new Box(0.06f, 0.5f, 0.06f));
        post2.setMaterial(orange);
        post2.setLocalTranslation(0.8f, 0.25f, 0.0f);
        barrier.attachChild(post2);

        barrier.setLocalRotation(new Quaternion().fromAngleNormalAxis(facingYaw, Vector3f.UNIT_Y));
        barrier.setLocalTranslation(x, 0.2f, z);
        this.parent.attachChild(barrier);
    }

    public void addBusStop(float x, float z, float facingYaw) {
        Node stop = new Node("BusStop");
        Material roof = this.colouredMat(new ColorRGBA(0.15f, 0.35f, 0.65f, 1.0f));
        Material post = this.colouredMat(new ColorRGBA(0.2f, 0.2f, 0.22f, 1.0f));
        Material glass = this.colouredMat(new ColorRGBA(0.75f, 0.85f, 0.95f, 0.6f));
        Material bench = this.colouredMat(new ColorRGBA(0.35f, 0.2f, 0.1f, 1.0f));
        Material signMat = this.colouredMat(new ColorRGBA(0.95f, 0.85f, 0.1f, 1.0f));
        Material signText = this.colouredMat(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        Geometry roofG = new Geometry("BusStopRoof", new Box(2.0f, 0.1f, 1.0f));
        roofG.setMaterial(roof);
        roofG.setLocalTranslation(0.0f, 2.7f, 0.0f);
        stop.attachChild(roofG);
        Geometry glassL = new Geometry("GlassL", new Box(0.05f, 1.2f, 0.95f));
        glassL.setMaterial(glass);
        glassL.setLocalTranslation(-1.95f, 1.5f, 0.0f);
        stop.attachChild(glassL);
        Geometry glassR = new Geometry("GlassR", new Box(0.05f, 1.2f, 0.95f));
        glassR.setMaterial(glass);
        glassR.setLocalTranslation(1.95f, 1.5f, 0.0f);
        stop.attachChild(glassR);
        Geometry back = new Geometry("Back", new Box(1.95f, 1.2f, 0.05f));
        back.setMaterial(glass);
        back.setLocalTranslation(0.0f, 1.5f, 0.95f);
        stop.attachChild(back);
        Geometry seat = new Geometry("Seat", new Box(1.2f, 0.06f, 0.3f));
        seat.setMaterial(bench);
        seat.setLocalTranslation(0.0f, 0.5f, -0.5f);
        stop.attachChild(seat);
        Geometry leg1 = new Geometry("Leg1", new Box(0.05f, 0.25f, 0.25f));
        leg1.setMaterial(bench);
        leg1.setLocalTranslation(-1.0f, 0.25f, -0.5f);
        stop.attachChild(leg1);
        Geometry leg2 = new Geometry("Leg2", new Box(0.05f, 0.25f, 0.25f));
        leg2.setMaterial(bench);
        leg2.setLocalTranslation(1.0f, 0.25f, -0.5f);
        stop.attachChild(leg2);
        Geometry post1 = new Geometry("Post1", new Box(0.08f, 2.6f, 0.08f));
        post1.setMaterial(post);
        post1.setLocalTranslation(-1.95f, 1.3f, 0.95f);
        stop.attachChild(post1);
        Geometry post2 = new Geometry("Post2", new Box(0.08f, 2.6f, 0.08f));
        post2.setMaterial(post);
        post2.setLocalTranslation(1.95f, 1.3f, 0.95f);
        stop.attachChild(post2);
        Geometry post3 = new Geometry("Post3", new Box(0.08f, 2.6f, 0.08f));
        post3.setMaterial(post);
        post3.setLocalTranslation(-1.95f, 1.3f, -0.95f);
        stop.attachChild(post3);
        Geometry post4 = new Geometry("Post4", new Box(0.08f, 2.6f, 0.08f));
        post4.setMaterial(post);
        post4.setLocalTranslation(1.95f, 1.3f, -0.95f);
        stop.attachChild(post4);
        Geometry signPost = new Geometry("SignPost", new Cylinder(6, 12, 0.06f, 3.2f, true));
        signPost.setMaterial(post);
        signPost.setLocalTranslation(2.4f, 1.6f, -0.8f);
        stop.attachChild(signPost);
        Geometry sign = new Geometry("BusSign", new Box(0.3f, 0.2f, 0.03f));
        sign.setMaterial(signMat);
        sign.setLocalTranslation(2.4f, 2.8f, -0.8f);
        stop.attachChild(sign);
        Geometry letterB = new Geometry("LetterB", new Box(0.05f, 0.08f, 0.01f));
        letterB.setMaterial(signText);
        letterB.setLocalTranslation(2.3f, 2.8f, -0.78f);
        stop.attachChild(letterB);
        Geometry letterU = new Geometry("LetterU", new Box(0.05f, 0.08f, 0.01f));
        letterU.setMaterial(signText);
        letterU.setLocalTranslation(2.4f, 2.8f, -0.78f);
        stop.attachChild(letterU);
        Geometry letterS = new Geometry("LetterS", new Box(0.05f, 0.08f, 0.01f));
        letterS.setMaterial(signText);
        letterS.setLocalTranslation(2.5f, 2.8f, -0.78f);
        stop.attachChild(letterS);
        stop.setLocalRotation(new Quaternion().fromAngleNormalAxis(facingYaw, Vector3f.UNIT_Y));
        stop.setLocalTranslation(x, 0.2f, z);
        this.parent.attachChild(stop);
    }

    private Spatial loadScaled(String path, float scale) {
        Spatial s = this.assetManager.loadModel(path);
        s.scale(scale);
        return s;
    }

    private Material colouredMat(ColorRGBA colour) {
        Material mat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", colour);
        mat.setColor("Ambient", colour);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 8.0f);
        return mat;
    }
}


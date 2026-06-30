/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.taxigame.Props;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class City {
    public static final float TILE_SIZE = 12.0f;
    public static final int GRID_SIZE = 11;
    public static final float HALF_SIZE = 66.0f;
    public static final float ROAD_WIDTH = 12.0f;
    private final AssetManager assetManager;
    private final Node root;
    private final List<Bounds> buildingBounds = new ArrayList<Bounds>();
    private final Random rng = new Random(1337L);
    private Material roadStraightMat;
    private Material roadCrossroadMat;
    private Material sidewalkMat;
    private Material grassMat;
    private Material curbMat;
    private Material wallMat;
    private Props props;

    public City(AssetManager assetManager, Node root) {
        this.assetManager = assetManager;
        this.root = root;
    }

    public void build() {
        this.initMaterials();
        this.props = new Props(this.assetManager, this.root);
        this.props.init();
        this.addGround();
        this.addContinuousRoads();
        this.addCurbs();
        this.addSidewalks();
        this.addBuildings();
        this.addStreetFurniture();
        this.addBoundaryWalls();
    }

    public List<Bounds> getBuildingBounds() {
        return this.buildingBounds;
    }

    public boolean isRoadTile(int ix, int iz) {
        return Math.abs(ix) % 2 == 1 || Math.abs(iz) % 2 == 1;
    }

    public boolean isOnRoadPrecise(float x, float z) {
        int iz;
        int ix = Math.round(x / 12.0f);
        if (!this.isRoadTile(ix, iz = Math.round(z / 12.0f))) {
            return false;
        }
        float tx = x - (float)ix * 12.0f;
        float tz = z - (float)iz * 12.0f;
        return Math.abs(tx) < 6.0f && Math.abs(tz) < 6.0f;
    }

    private void initMaterials() {
        this.roadStraightMat = this.texturedMat("Textures/Roads/lane-straight.png", true);
        this.roadCrossroadMat = this.texturedMat("Textures/Roads/lane-crossroad.png", true);
        this.sidewalkMat = this.texturedMat("Textures/Roads/sidewalk.png", true);
        this.grassMat = this.texturedMat("Textures/Roads/grass.png", true);
        this.curbMat = this.texturedMat("Textures/Roads/curb.png", true);
        this.wallMat = this.colouredMat(new ColorRGBA(0.35f, 0.32f, 0.28f, 1.0f));
    }

    private Material texturedMat(String texturePath, boolean repeatable) {
        Material mat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture tex = this.assetManager.loadTexture(texturePath);
        if (repeatable) {
            tex.setWrap(Texture.WrapMode.Repeat);
        }
        mat.setTexture("DiffuseMap", tex);
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.Black);
        mat.setFloat("Shininess", 2.0f);
        return mat;
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

    private void addGround() {
        float ext = 70.0f;
        Quad ground = new Quad(ext * 2.0f, ext * 2.0f);
        Geometry g = new Geometry("Ground", ground);
        g.setMaterial(this.grassMat);
        ground.scaleTextureCoordinates(new Vector2f(ext / 4.0f, ext / 4.0f));
        g.rotate(-1.5707964f, 0.0f, 0.0f);
        g.setLocalTranslation(-ext, -0.15f, ext);
        this.root.attachChild(g);
    }

    private void addContinuousRoads() {
        int ix;
        int half = 5;
        float cityExtent = (float)half * 12.0f;
        float roadLength = cityExtent * 2.0f + 12.0f;
        float roadWidth = 12.0f;
        for (int iz = -half; iz <= half; ++iz) {
            if (Math.abs(iz) % 2 == 0) continue;
            float z = (float)iz * 12.0f;
            this.addRoadStrip(roadLength, roadWidth, true, z);
        }
        for (ix = -half; ix <= half; ++ix) {
            if (Math.abs(ix) % 2 == 0) continue;
            float x = (float)ix * 12.0f;
            this.addRoadStrip(roadLength, roadWidth, false, x);
        }
        for (ix = -half; ix <= half; ++ix) {
            if (Math.abs(ix) % 2 == 0) continue;
            for (int iz = -half; iz <= half; ++iz) {
                if (Math.abs(iz) % 2 == 0) continue;
                float x = (float)ix * 12.0f;
                float z = (float)iz * 12.0f;
                this.addCrossroadMarkings(x, z);
            }
        }
    }

    private void addRoadStrip(float length, float width, boolean horizontal, float fixedCoord) {
        Geometry g;
        if (horizontal) {
            Quad quad = new Quad(length, width);
            g = new Geometry("RoadH", quad);
            quad.scaleTextureCoordinates(new Vector2f(length / 12.0f, 1.0f));
            g.rotate(-1.5707964f, 0.0f, 0.0f);
            g.setLocalTranslation(-length / 2.0f, 0.02f, fixedCoord + width / 2.0f);
        } else {
            Quad quad = new Quad(width, length);
            g = new Geometry("RoadV", quad);
            quad.scaleTextureCoordinates(new Vector2f(1.0f, length / 12.0f));
            g.rotate(-1.5707964f, 0.0f, 0.0f);
            g.setLocalTranslation(fixedCoord - width / 2.0f, 0.02f, length / 2.0f);
        }
        g.setMaterial(this.roadStraightMat);
        this.root.attachChild(g);
    }

    private void addCrossroadMarkings(float cx, float cz) {
        float size = 12.0f;
        Quad quad = new Quad(size, size);
        Geometry g = new Geometry("Crossroad", quad);
        g.setMaterial(this.roadCrossroadMat);
        g.rotate(-1.5707964f, 0.0f, 0.0f);
        g.setLocalTranslation(cx - size / 2.0f, 0.04f, cz + size / 2.0f);
        this.root.attachChild(g);
    }

    private void addSidewalks() {
        int half = 5;
        float swSize = 11.04f;
        for (int ix = -half; ix <= half; ++ix) {
            for (int iz = -half; iz <= half; ++iz) {
                if (this.isRoadTile(ix, iz)) continue;
                float cx = (float)ix * 12.0f;
                float cz = (float)iz * 12.0f;
                Quad quad = new Quad(swSize, swSize);
                Geometry g = new Geometry("Sidewalk", quad);
                g.setMaterial(this.sidewalkMat);
                quad.scaleTextureCoordinates(new Vector2f(2.0f, 2.0f));
                g.rotate(-1.5707964f, 0.0f, 0.0f);
                g.setLocalTranslation(cx - swSize / 2.0f, 0.18f, cz + swSize / 2.0f);
                this.root.attachChild(g);
            }
        }
    }

    private void addCurbs() {
        int half = 5;
        float blockHalf = 6.0f;
        float curbThickness = 0.4f;
        float curbHeight = 0.2f;
        for (int ix = -half; ix <= half; ++ix) {
            for (int iz = -half; iz <= half; ++iz) {
                if (this.isRoadTile(ix, iz)) continue;
                float cx = (float)ix * 12.0f;
                float cz = (float)iz * 12.0f;
                this.addCurbStrip(cx, cz + blockHalf - curbThickness / 2.0f, 12.0f, curbThickness, curbHeight, true);
                this.addCurbStrip(cx, cz - blockHalf + curbThickness / 2.0f, 12.0f, curbThickness, curbHeight, true);
                this.addCurbStrip(cx - blockHalf + curbThickness / 2.0f, cz, curbThickness, 12.0f, curbHeight, false);
                this.addCurbStrip(cx + blockHalf - curbThickness / 2.0f, cz, curbThickness, 12.0f, curbHeight, false);
            }
        }
    }

    private void addCurbStrip(float cx, float cz, float longSize, float shortSize, float height, boolean horizontal) {
        float sx = horizontal ? longSize : shortSize;
        float sz = horizontal ? shortSize : longSize;
        Box box = new Box(sx / 2.0f, height / 2.0f, sz / 2.0f);
        Geometry g = new Geometry("Curb", box);
        g.setMaterial(this.curbMat);
        box.scaleTextureCoordinates(new Vector2f(longSize / 2.0f, shortSize / 2.0f));
        g.setLocalTranslation(cx, height / 2.0f, cz);
        this.root.attachChild(g);
    }

    private void addStreetFurniture() {
        int iz;
        int ix;
        int half = 5;
        for (ix = -half; ix <= half; ++ix) {
            if (Math.abs(ix) % 2 == 0) continue;
            for (iz = -half; iz <= half; ++iz) {
                if (Math.abs(iz) % 2 == 0) continue;
                float x = (float)ix * 12.0f;
                float z = (float)iz * 12.0f;
                float offset = 6.6000004f;
                this.props.addStreetLamp(x + offset, z + offset);
                this.props.addStreetLamp(x - offset, z - offset);
            }
        }
        for (ix = -half; ix <= half; ++ix) {
            for (iz = -half; iz <= half; ++iz) {
                if (this.isRoadTile(ix, iz)) continue;
                float cx = (float)ix * 12.0f;
                float cz = (float)iz * 12.0f;
                float planterOffset = 5.04f;
                int side = this.rng.nextInt(4);
                float px = cx;
                float pz = cz;
                switch (side) {
                    case 0: {
                        pz = cz - planterOffset;
                        break;
                    }
                    case 1: {
                        pz = cz + planterOffset;
                        break;
                    }
                    case 2: {
                        px = cx - planterOffset;
                        break;
                    }
                    case 3: {
                        px = cx + planterOffset;
                    }
                }
                this.props.addPlanter(px, pz);
                if (this.rng.nextFloat() < 0.15f) {
                    int side4 = (side + 3) % 4;
                    float mx = cx;
                    float mz = cz;
                    float marketOffset = 4.8f;
                    switch (side4) {
                        case 0: mz = cz - marketOffset; break;
                        case 1: mz = cz + marketOffset; break;
                        case 2: mx = cx - marketOffset; break;
                        case 3: mx = cx + marketOffset; break;
                    }
                    float marketFacing = side4 < 2 ? 0.0f : (float)(Math.PI / 2.0);
                    this.props.addMarketStall(mx, mz, marketFacing);
                }
                if (this.rng.nextFloat() < 0.25f) {
                    int side2 = (side + 2) % 4;
                    float hx = cx;
                    float hz = cz;
                    float hydrantOffset = 4.56f;
                    switch (side2) {
                        case 0: {
                            hz = cz - hydrantOffset;
                            break;
                        }
                        case 1: {
                            hz = cz + hydrantOffset;
                            break;
                        }
                        case 2: {
                            hx = cx - hydrantOffset;
                            break;
                        }
                        case 3: {
                            hx = cx + hydrantOffset;
                        }
                    }
                    this.props.addFireHydrant(hx, hz);
                }
                if (this.rng.nextFloat() < 0.12f) {
                    float bx = cx + (this.rng.nextFloat() - 0.5f) * 4.0f;
                    float bz = cz + (this.rng.nextFloat() - 0.5f) * 4.0f;
                    this.props.addConstructionBarrier(bx, bz, this.rng.nextFloat() * (float)Math.PI * 2);
                }
                if (!(this.rng.nextFloat() < 0.3f)) continue;
                int side3 = (side + 1) % 4;
                float tx = cx;
                float tz = cz;
                float treeOffset = 4.2f;
                switch (side3) {
                    case 0: {
                        tz = cz - treeOffset;
                        break;
                    }
                    case 1: {
                        tz = cz + treeOffset;
                        break;
                    }
                    case 2: {
                        tx = cx - treeOffset;
                        break;
                    }
                    case 3: {
                        tx = cx + treeOffset;
                    }
                }
                if (this.rng.nextBoolean()) {
                    this.props.addTreeSmall(tx, tz);
                    continue;
                }
                this.props.addTreeLarge(tx, tz);
            }
        }
    }

    private void addBuildings() {
        ArrayList<Spatial> buildingProtos = new ArrayList<Spatial>();
        for (int i = 0; i < 21; ++i) {
            char type = (char)(97 + i);
            try {
                Spatial b = this.loadScaled("Models/Buildings/building-type-" + type + ".glb", 6.0f);
                buildingProtos.add(b);
                continue;
            }
            catch (Exception b) {
                // empty catch block
            }
        }
        int half = 5;
        for (int ix = -half; ix <= half; ++ix) {
            for (int iz = -half; iz <= half; ++iz) {
                if (this.isRoadTile(ix, iz)) continue;
                float cx = (float)ix * 12.0f;
                float cz = (float)iz * 12.0f;
                int count = 1 + this.rng.nextInt(3);
                for (int i = 0; i < count; ++i) {
                    Spatial proto = (Spatial)buildingProtos.get(this.rng.nextInt(buildingProtos.size()));
                    Spatial b = proto.clone();
                    float ox = (this.rng.nextFloat() - 0.5f) * 3.6f;
                    float oz = (this.rng.nextFloat() - 0.5f) * 3.6f;
                    float rot = (float)this.rng.nextInt(4) * 1.5707964f;
                    b.rotate(0.0f, rot, 0.0f);
                    b.setLocalTranslation(cx + ox, 0.25f, cz + oz);
                    this.root.attachChild(b);
                    this.buildingBounds.add(new Bounds(cx + ox - 3.0f, cx + ox + 3.0f, cz + oz - 3.0f, cz + oz + 3.0f));
                }
            }
        }
    }

    private void addBoundaryWalls() {
        float ext = 66.0f;
        float h = 5.0f;
        Geometry n = new Geometry("WallN", new Box(ext, h / 2.0f, 0.5f));
        n.setMaterial(this.wallMat);
        n.setLocalTranslation(0.0f, h / 2.0f, -ext);
        this.root.attachChild(n);
        Geometry s = new Geometry("WallS", new Box(ext, h / 2.0f, 0.5f));
        s.setMaterial(this.wallMat);
        s.setLocalTranslation(0.0f, h / 2.0f, ext);
        this.root.attachChild(s);
        Geometry w = new Geometry("WallW", new Box(0.5f, h / 2.0f, ext));
        w.setMaterial(this.wallMat);
        w.setLocalTranslation(-ext, h / 2.0f, 0.0f);
        this.root.attachChild(w);
        Geometry e = new Geometry("WallE", new Box(0.5f, h / 2.0f, ext));
        e.setMaterial(this.wallMat);
        e.setLocalTranslation(ext, h / 2.0f, 0.0f);
        this.root.attachChild(e);
    }

    private Spatial loadScaled(String path, float targetSize) {
        Spatial s = this.assetManager.loadModel(path);
        s.scale(targetSize);
        return s;
    }

    public static final class Bounds {
        public final float minX;
        public final float maxX;
        public final float minZ;
        public final float maxZ;

        public Bounds(float minX, float maxX, float minZ, float maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public boolean contains(float x, float z, float radius) {
            return x + radius > this.minX && x - radius < this.maxX && z + radius > this.minZ && z - radius < this.maxZ;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame.tools;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.system.AppSettings;
import java.nio.FloatBuffer;

public class InspectModels
extends SimpleApplication {
    public static void main(String[] args) {
        InspectModels app = new InspectModels();
        AppSettings s = new AppSettings(true);
        s.setAudioRenderer(null);
        app.setSettings(s);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        String[] paths;
        for (String p : paths = new String[]{"Models/Roads/road-straight.glb", "Models/Roads/road-crossroad.glb", "Models/Roads/road-curve.glb", "Models/Roads/road-intersection.glb", "Models/Buildings/building-type-a.glb", "Models/Buildings/building-type-b.glb", "Models/Buildings/building-type-c.glb", "Models/Cars/taxi.glb", "Models/Cars/sedan.glb", "Models/Cars/police.glb", "Models/Cars/van.glb", "Models/Cars/suv.glb", "Models/Cars/hatchback-sports.glb", "Models/Passengers/character-a.glb", "Models/Passengers/character-b.glb", "Models/Passengers/character-c.glb", "Models/Props/light-square.glb", "Models/Props/light-curved.glb", "Models/Props/planter.glb", "Models/Props/tree-small.glb", "Models/Props/tree-large.glb", "Models/Props/construction-cone.glb"}) {
            try {
                Spatial s = this.assetManager.loadModel(p);
                Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
                final Vector3f fmin = min;
                final Vector3f fmax = max;
                s.breadthFirstTraversal(new SceneGraphVisitor(){

                    @Override
                    public void visit(Spatial sp) {
                        if (sp instanceof Geometry) {
                            Mesh m = ((Geometry)sp).getMesh();
                            FloatBuffer vb = m.getFloatBuffer(VertexBuffer.Type.Position);
                            vb.rewind();
                            while (vb.remaining() >= 3) {
                                float x = vb.get();
                                float y = vb.get();
                                float z = vb.get();
                                if (x < fmin.x) {
                                    fmin.x = x;
                                }
                                if (y < fmin.y) {
                                    fmin.y = y;
                                }
                                if (z < fmin.z) {
                                    fmin.z = z;
                                }
                                if (x > fmax.x) {
                                    fmax.x = x;
                                }
                                if (y > fmax.y) {
                                    fmax.y = y;
                                }
                                if (!(z > fmax.z)) continue;
                                fmax.z = z;
                            }
                        }
                    }
                });
                Vector3f size = max.subtract(min);
                System.out.printf("%-55s size=(%.2f, %.2f, %.2f)  min=(%.2f, %.2f, %.2f)  max=(%.2f, %.2f, %.2f)%n", p, Float.valueOf(size.x), Float.valueOf(size.y), Float.valueOf(size.z), Float.valueOf(min.x), Float.valueOf(min.y), Float.valueOf(min.z), Float.valueOf(max.x), Float.valueOf(max.y), Float.valueOf(max.z));
            }
            catch (Throwable e) {
                System.out.println(p + "  FAILED: " + String.valueOf(e));
                StackTraceElement[] st = e.getStackTrace();
                if (st != null) {
                    for (int i = 0; i < Math.min(st.length, 6); ++i) {
                        System.out.println("    at " + String.valueOf(st[i]));
                    }
                }
                for (Throwable c = e.getCause(); c != null; c = c.getCause()) {
                    System.out.println("    caused by: " + String.valueOf(c));
                    st = c.getStackTrace();
                    if (st == null) continue;
                    for (int i = 0; i < Math.min(st.length, 3); ++i) {
                        System.out.println("      at " + String.valueOf(st[i]));
                    }
                }
            }
        }
        this.stop();
    }
}


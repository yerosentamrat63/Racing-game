/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame.tools;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.taxigame.City;
import com.taxigame.Obstacle;
import com.taxigame.Passenger;
import com.taxigame.Taxi;
import java.util.ArrayList;
import java.util.List;

public class GameTest
extends SimpleApplication {
    public static void main(String[] args) throws Exception {
        GameTest app = new GameTest();
        AppSettings s = new AppSettings(true);
        s.setAudioRenderer(null);
        app.setSettings(s);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int i;
        System.out.println("[GameTest] Building city...");
        Node gameRoot = new Node("GameRoot");
        this.rootNode.attachChild(gameRoot);
        City city = new City(this.assetManager, gameRoot);
        city.build();
        System.out.println("[GameTest]   City built. Building bounds: " + city.getBuildingBounds().size());
        System.out.println("[GameTest] Spawning taxi...");
        Taxi taxi = new Taxi(this.assetManager, gameRoot);
        taxi.setPosition(new Vector3f(0.0f, 0.0f, 12.0f));
        System.out.println("[GameTest] Spawning 6 obstacles...");
        List<Obstacle> obstacles = new ArrayList<>();
        for (i = 0; i < 6; ++i) {
            int ix = 1;
            int iz = 1 + i * 2;
            Vector3f pos = new Vector3f((float)ix * 12.0f, 0.0f, (float)iz * 12.0f);
            Obstacle o = new Obstacle(this.assetManager, gameRoot, city, pos);
            obstacles.add(o);
            o.update(0.016f, obstacles);
        }
        System.out.println("[GameTest] Spawning 4 passengers...");
        for (i = 0; i < 4; ++i) {
            Vector3f pos = new Vector3f((float)(i - 2) * 12.0f, 0.0f, 5.7599998f);
            Passenger p = new Passenger(this.assetManager, gameRoot, pos);
            p.update(0.016f);
        }
        System.out.println("[GameTest] Simulating 60 frames of driving...");
        boolean ok = true;
        for (int i2 = 0; i2 < 60; ++i2) {
            try {
                taxi.update(0.016f, true, false, i2 % 2 == 0, i2 % 2 == 1, city);
                continue;
            }
            catch (Throwable t) {
                System.out.println("[GameTest]   FAILED at frame " + i2 + ": " + String.valueOf(t));
                ok = false;
                break;
            }
        }
        if (ok) {
            System.out.println("[GameTest]   Taxi drove to position: " + String.valueOf(taxi.getPosition()));
            System.out.println("[GameTest] ALL OK - assets loaded and game ticks successfully.");
        } else {
            System.out.println("[GameTest] FAILED - see above.");
        }
        this.stop();
    }
}


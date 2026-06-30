/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame;

import com.jme3.system.AppSettings;
import com.taxigame.TaxiGame;

public final class Main {
    public static void main(String[] args) {
        TaxiGame app = new TaxiGame();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("City Taxi 3D");
        settings.setResolution(1280, 720);
        settings.setSamples(2);
        settings.setVSync(true);
        settings.setFullscreen(false);
        if (!Boolean.getBoolean("taxigame.audio")) {
            settings.setAudioRenderer(null);
        }
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    private Main() {
    }
}


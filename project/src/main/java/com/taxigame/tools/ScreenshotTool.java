/*
 * Decompiled with CFR 0.152.
 */
package com.taxigame.tools;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;
import com.taxigame.City;
import com.taxigame.Obstacle;
import com.taxigame.Passenger;
import com.taxigame.Taxi;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public class ScreenshotTool
extends SimpleApplication {
    private int frame = 0;
    private ScreenshotAppState shotState;

    public static void main(String[] args) {
        ScreenshotTool app = new ScreenshotTool();
        AppSettings s = new AppSettings(true);
        s.setAudioRenderer(null);
        s.setResolution(1280, 720);
        s.setSamples(1);
        s.setDepthBits(24);
        s.setGammaCorrection(false);
        app.setSettings(s);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int i;
        this.shotState = new ScreenshotAppState("/home/z/my-project/download/", "city-shot");
        this.shotState.setIsNumbered(false);
        this.stateManager.attach(this.shotState);
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.4f, -1.0f, -0.3f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.4f));
        this.rootNode.addLight(sun);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.7f));
        this.rootNode.addLight(ambient);
        Node gameRoot = new Node("GameRoot");
        this.rootNode.attachChild(gameRoot);
        City city = new City(this.assetManager, gameRoot);
        city.build();
        Taxi taxi = new Taxi(this.assetManager, gameRoot);
        taxi.setPosition(new Vector3f(0.0f, 0.0f, 12.0f));
        for (i = 0; i < 6; ++i) {
            int ix = 1;
            int iz = 1 + i * 2;
            Vector3f pos = new Vector3f((float)ix * 12.0f, 0.0f, (float)iz * 12.0f);
            new Obstacle(this.assetManager, gameRoot, city, pos);
        }
        for (i = 0; i < 4; ++i) {
            Vector3f pos = new Vector3f((float)(i - 2) * 12.0f, 0.0f, 5.7599998f);
            new Passenger(this.assetManager, gameRoot, pos);
        }
        this.cam.setLocation(new Vector3f(40.0f, 30.0f, 40.0f));
        this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        this.cam.setFrustumPerspective(60.0f, 1.777f, 0.1f, 1000.0f);
        System.out.println("[ScreenshotTool] Camera location: " + String.valueOf(this.cam.getLocation()));
        System.out.println("[ScreenshotTool] Camera direction: " + String.valueOf(this.cam.getDirection()));
    }

    @Override
    public void simpleUpdate(float tpf) {
        ++this.frame;
        if (this.frame == 1) {
            this.cam.setLocation(new Vector3f(30.0f, 35.0f, 30.0f));
            this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        }
        if (this.frame == 90 || this.frame == 120) {
            try {
                String path = "/home/z/my-project/download/city-shot-" + this.frame + ".png";
                int w = this.viewPort.getCamera().getWidth();
                int h = this.viewPort.getCamera().getHeight();
                ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
                this.renderer.readFrameBufferWithFormat(this.viewPort.getOutputFrameBuffer(), buf, Image.Format.RGBA8);
                BufferedImage img = new BufferedImage(w, h, 1);
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        int i = (y * w + x) * 4;
                        int r = buf.get(i) & 0xFF;
                        int g = buf.get(i + 1) & 0xFF;
                        int b = buf.get(i + 2) & 0xFF;
                        img.setRGB(x, h - 1 - y, r << 16 | g << 8 | b);
                    }
                }
                ImageIO.write((RenderedImage)img, "PNG", new File(path));
                System.out.println("[ScreenshotTool] Saved " + path + " at frame " + this.frame);
            }
            catch (Throwable t) {
                System.out.println("[ScreenshotTool] Failed at frame " + this.frame + ": " + String.valueOf(t));
                t.printStackTrace();
            }
        }
        if (this.frame >= 150) {
            this.stop();
        }
    }
}


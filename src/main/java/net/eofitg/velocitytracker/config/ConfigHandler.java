package net.eofitg.velocitytracker.config;

import net.eofitg.velocitytracker.hud.VelocityOverlayRenderer;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
    private final Configuration config;

    public ConfigHandler(File file) {
        this.config = new Configuration(file);
    }

    public void load() {
        config.load();

        VelocityOverlayRenderer.enabled = config.getBoolean("enabled", "velocitytracker", DefaultConfig.VelocityOverlay.enabled, "Whether velocity tracker rendering is enabled");
        VelocityOverlayRenderer.scale = config.getFloat("scale", "velocitytracker", DefaultConfig.VelocityOverlay.scale, 1f, 50f, "");
        VelocityOverlayRenderer.alpha = config.getInt("alpha", "velocitytracker", DefaultConfig.VelocityOverlay.alpha, 0, 255, "");
        VelocityOverlayRenderer.graphSize = config.getInt("graphsize", "velocitytracker", DefaultConfig.VelocityOverlay.graphSize, 1, 250, "");
        VelocityOverlayRenderer.posX = config.getInt("posx", "velocitytracker", DefaultConfig.VelocityOverlay.posX, 0, 1920, "");
        VelocityOverlayRenderer.posY = config.getInt("posy", "velocitytracker", DefaultConfig.VelocityOverlay.posY, 0, 1080, "");
        VelocityOverlayRenderer.vyRange = config.getFloat("vyrange", "velocitytracker", DefaultConfig.VelocityOverlay.vyRange, 1f, 50f, "");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public void save() {
        config.get("velocitytracker", "enabled", DefaultConfig.VelocityOverlay.enabled).set(VelocityOverlayRenderer.enabled);
        config.get("velocitytracker", "scale", DefaultConfig.VelocityOverlay.scale).set(VelocityOverlayRenderer.scale);
        config.get("velocitytracker", "alpha", DefaultConfig.VelocityOverlay.alpha).set(VelocityOverlayRenderer.alpha);
        config.get("velocitytracker", "graphsize", DefaultConfig.VelocityOverlay.graphSize).set(VelocityOverlayRenderer.graphSize);
        config.get("velocitytracker", "posx", DefaultConfig.VelocityOverlay.posX).set(VelocityOverlayRenderer.posX);
        config.get("velocitytracker", "posy", DefaultConfig.VelocityOverlay.posY).set(VelocityOverlayRenderer.posY);
        config.get("velocitytracker", "vyrange", DefaultConfig.VelocityOverlay.vyRange).set(VelocityOverlayRenderer.vyRange);

        config.save();
    }
}

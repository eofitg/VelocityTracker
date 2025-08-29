package net.eofitg.velocitytracker;

import net.eofitg.velocitytracker.commands.CommandVelocityTracker;
import net.eofitg.velocitytracker.config.ConfigHandler;
import net.eofitg.velocitytracker.hud.VelocityOverlayRenderer;
import net.eofitg.velocitytracker.util.Reference;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.Arrays;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.MOD_VERSION,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class VelocityTracker {
    public static ConfigHandler configHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        File configFile = new File(e.getModConfigurationDirectory(), Reference.MOD_ID + ".cfg");
        configHandler = new ConfigHandler(configFile);
        configHandler.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        registerListeners(new VelocityOverlayRenderer());
        registerCommands(new CommandVelocityTracker());
    }

    private void registerListeners(Object... listeners) {
        Arrays.stream(listeners).forEachOrdered(MinecraftForge.EVENT_BUS::register);
    }

    private void registerCommands(ICommand... commands) {
        Arrays.stream(commands).forEachOrdered(ClientCommandHandler.instance::registerCommand);
    }
}

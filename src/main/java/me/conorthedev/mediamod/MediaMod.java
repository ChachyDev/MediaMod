package me.conorthedev.mediamod;

import me.conorthedev.mediamod.base.BaseMod;
import me.conorthedev.mediamod.command.MediaModCommand;
import me.conorthedev.mediamod.gui.PlayerOverlay;
import me.conorthedev.mediamod.gui.util.DynamicTextureWrapper;
import me.conorthedev.mediamod.media.base.ServiceHandler;
import me.conorthedev.mediamod.media.browser.BrowserHandler;
import me.conorthedev.mediamod.media.spotify.SpotifyHandler;
import me.conorthedev.mediamod.media.spotify.api.playing.CurrentlyPlayingObject;
import me.conorthedev.mediamod.media.spotify.api.track.Track;
import me.conorthedev.mediamod.util.Metadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.awt.Color.white;

/**
 * The main class for MediaMod
 *
 * @author ConorTheDev
 * @see net.minecraftforge.fml.common.Mod
 */
@Mod(name = Metadata.NAME, modid = Metadata.MODID, version = Metadata.VERSION)
public class MediaMod {
    /**
     * An instance of this class to access non-static methods from other classes
     */
    @Mod.Instance(Metadata.MODID)
    public static MediaMod INSTANCE;

    /**
     * Logger used to log info messages, debug messages, error messages & more
     *
     * @see org.apache.logging.log4j.Logger
     */
    public final Logger LOGGER = LogManager.getLogger("MediaMod");

    /**
     * Check if the user is in a development environment, this is used for DEBUG messages
     */
    public boolean DEVELOPMENT_ENVIRONMENT = classExists("net.minecraft.client.Minecraft");

    /**
     * Fired when Minecraft is starting
     *
     * @param event - FMLInitializationEvent
     * @see FMLInitializationEvent
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("MediaMod starting...");

        // Register event subscribers and commands
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PlayerOverlay.INSTANCE);
        ClientCommandHandler.instance.registerCommand(new MediaModCommand());

        LOGGER.info("Attempting to register with analytics...");

        // Register with analytics
        if (Minecraft.getMinecraft().gameSettings.snooperEnabled) {
            boolean successful = BaseMod.init();

            if (successful) {
                LOGGER.info("Successfully registered with analytics!");
            } else {
                LOGGER.error("Failed to register with analytics...");
            }
        }

        // Load the config
        LOGGER.info("Loading Configuration...");
        Settings.loadConfig();

        // Load Media Handlers
        ServiceHandler serviceHandler = ServiceHandler.INSTANCE;
        //serviceHandler.registerHandler(new BrowserHandler());
        serviceHandler.registerHandler(new SpotifyHandler());

        // Initialize the handlers
        serviceHandler.initializeHandlers();
    }

    /**
     * Checks if a class exists by the class name
     *
     * @param className - the class name including package (i.e. net.minecraft.client.Minecraft)
     * @return boolean
     */
    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
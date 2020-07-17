package org.mediamod.mediamod;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mediamod.mediamod.command.MediaModCommand;
import org.mediamod.mediamod.config.Settings;
import org.mediamod.mediamod.core.CoreMod;
import org.mediamod.mediamod.event.MediaInfoUpdateEvent;
import org.mediamod.mediamod.gui.PlayerOverlay;
import org.mediamod.mediamod.keybinds.KeybindInputHandler;
import org.mediamod.mediamod.keybinds.KeybindManager;
import org.mediamod.mediamod.levelhead.LevelheadIntegration;
import org.mediamod.mediamod.media.MediaHandler;
import org.mediamod.mediamod.media.core.api.MediaInfo;
import org.mediamod.mediamod.parties.PartyManager;
import org.mediamod.mediamod.util.*;

import java.io.File;
import java.io.IOException;

/**
 * The main class for MediaMod
 *
 * @author ConorTheDev
 * @see net.minecraftforge.fml.common.Mod
 */
@Mod(name = Metadata.NAME, modid = Metadata.MODID, version = Metadata.VERSION)
public class MediaMod {
    /**
     * The API Endpoint for MediaMod requests
     */
    public static final String ENDPOINT = "http://localhost:3000/";

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
     * A CoreMod instance which assists with analytics
     */
    public final CoreMod coreMod = new CoreMod("mediamod");

    /**
     * If this is the first load of MediaMod
     */
    private boolean firstLoad = true;

    /**
     * Fired before Minecraft starts
     *
     * @param event - FMLPreInitializationEvent
     * @see FMLPreInitializationEvent
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeybindManager.INSTANCE.register();
        MinecraftForge.EVENT_BUS.register(new KeybindInputHandler());
    }

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
        MinecraftForge.EVENT_BUS.register(PlayerMessager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new LevelheadIntegration());

        ClientCommandHandler.instance.registerCommand(new MediaModCommand());
        //ClientCommandHandler.instance.registerCommand(new MediaModUpdateCommand());

        File MEDIAMOD_DIRECTORY = new File(FMLClientHandler.instance().getClient().mcDataDir, "mediamod");
        if (!MEDIAMOD_DIRECTORY.exists()) {
            LOGGER.info("Creating necessary directories and files for first launch...");
            boolean mkdir = MEDIAMOD_DIRECTORY.mkdir();

            if (mkdir) {
                LOGGER.info("Created necessary directories and files!");
            } else {
                LOGGER.fatal("Failed to create necessary directories and files!");
            }
        }

        LOGGER.info("Checking if MediaMod is up-to-date...");
        VersionChecker.checkVersion();

        if (VersionChecker.INSTANCE.IS_LATEST_VERSION) {
            LOGGER.info("MediaMod is up-to-date!");
        } else {
            LOGGER.warn("MediaMod is NOT up-to-date! Latest Version: v" + VersionChecker.INSTANCE.LATEST_VERSION_INFO.latestVersionS + " Your Version: v" + Metadata.VERSION);
        }

        try {
            this.coreMod.register();
        } catch (Exception e) {
            LOGGER.warn("Failed to register with analytics! ", e);
        }

        LOGGER.info("Loading Configuration...");
        Settings.loadConfig();

        // Load Media Handlers
        MediaHandler mediaHandler = MediaHandler.instance;
        mediaHandler.loadAll();
    }

    /**
     * Fired when the world fires a tick
     *
     * @param event WorldTickEvent
     * @see net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (firstLoad && !VersionChecker.INSTANCE.IS_LATEST_VERSION && Minecraft.getMinecraft().thePlayer != null) {
            PlayerMessager.sendMessage("&cMediaMod is out of date!" +
                    "\n&7Latest Version: &r&lv" + VersionChecker.INSTANCE.LATEST_VERSION_INFO.latestVersionS +
                    "\n&7Changelog: &r&l" + VersionChecker.INSTANCE.LATEST_VERSION_INFO.changelog);

            /*IChatComponent urlComponent = new ChatComponentText(ChatColor.GRAY + "" + ChatColor.BOLD +  "Click this to automatically update now!");
            urlComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "mediamodupdate"));
            urlComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatColor.translateAlternateColorCodes('&',
                    "&7Runs /mediamodupdate"))));
            PlayerMessager.sendMessage(urlComponent);*/
            firstLoad = false;
        }
    }


    /**
     * Fired when the current song information changes
     *
     * @see MediaInfoUpdateEvent
     */
    @SubscribeEvent
    public void onMediaInfoChange(MediaInfoUpdateEvent event) {
        MediaInfo info = event.mediaInfo;
        if (info == null) return;

        if (Settings.ANNOUNCE_TRACKS) {
            PlayerMessager.sendMessage(ChatColor.GRAY + "Current track: " + info.track.name + " by " + info.track.artists[0].name, true);
        }

        PartyManager.instance.updateInfo(info);
    }

}
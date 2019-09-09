package me.conorthedev.mediamod.gui;

import me.conorthedev.mediamod.Settings;
import me.conorthedev.mediamod.gui.util.CustomButton;
import me.conorthedev.mediamod.gui.util.IMediaGui;
import me.conorthedev.mediamod.util.Metadata;
import me.conorthedev.mediamod.util.Multithreading;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

/**
 * The Gui for editing the MediaMod Settings
 *
 * @see net.minecraft.client.gui.GuiScreen
 */
public class GuiMediaModSettings extends GuiScreen implements IMediaGui {

    private static final ResourceLocation headerResource = new ResourceLocation("mediamod", "header.png");

    @Override
    public void initGui() {
        Settings.loadConfig();
        this.buttonList.add(new CustomButton(0, width / 2 - 100, height / 2 - 47, getSuffix(Settings.ENABLED, "Enabled")));
        this.buttonList.add(new CustomButton(1, width / 2 - 100, height / 2 - 23, getSuffix(Settings.ENABLED, "Show Player")));
        this.buttonList.add(new CustomButton(2, width / 2 - 100, height / 2, "Player Settings"));
        this.buttonList.add(new CustomButton(3, width / 2 - 100, height / 2 + 23, "Services"));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();

        this.drawString(this.fontRendererObj, "Developed by ConorTheDev", this.width - this.fontRendererObj.getStringWidth("Developed by ConorTheDev") - 2, this.height - 10, -1);
        this.drawString(this.fontRendererObj, "Version " + Metadata.VERSION, this.width - this.fontRendererObj.getStringWidth("Version " + Metadata.VERSION) - 2, this.height - 20, -1);

        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, 1);

        // Bind the texture for rendering
        mc.getTextureManager().bindTexture(headerResource);

        // Render the album art as 35x35
        Gui.drawModalRectWithCustomSizedTexture(width / 2 - 111, height / 2 - 110, 0, 0, 222, 55, 222, 55);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                Settings.ENABLED = !Settings.ENABLED;
                button.displayString = getSuffix(Settings.ENABLED, "Enabled");
                break;

            case 1:
                Settings.SHOW_PLAYER = !Settings.SHOW_PLAYER;
                button.displayString = getSuffix(Settings.SHOW_PLAYER, "Show Player");
                break;

            case 2:
                this.mc.displayGuiScreen(new GuiPlayerSettings());
                break;

            case 3:
                this.mc.displayGuiScreen(new GuiServices());
                break;
        }

        super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        Settings.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

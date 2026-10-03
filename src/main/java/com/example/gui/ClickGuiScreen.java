package com.example.gui;

import com.example.ExampleMod;
import com.example.module.Module;
import com.example.module.combat.Aura;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private int guiX = 20;
    private int guiY = 20;
    private static final int GUI_WIDTH = 230;
    private static final int GUI_HEIGHT = 260;

    private boolean dragging = false;
    private int dragX, dragY;

    // Aura sub-settings
    private boolean showingAuraSettings = false;
    private int auraSettingsScroll = 0;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Полупрозрачный фон
        context.fill(0, 0, this.width, this.height, 0x55000000);

        // Основная панель — тёмная с закруглением (имитация)
        drawRoundedRect(context, guiX, guiY, GUI_WIDTH, GUI_HEIGHT, 0xE0101018);

        // Заголовок
        context.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + 24, 0xE0151520);
        context.drawText(this.textRenderer, "ExampleMod", guiX + 8, guiY + 7, 0xFF7B68EE, true);

        // Разделитель
        context.fill(guiX, guiY + 24, guiX + GUI_WIDTH, guiY + 25, 0xFF2A2A3A);

        // Модули
        int yOffset = guiY + 32;
        for (Module module : ExampleMod.moduleManager.getModules()) {
            boolean hovered = isHovered(mouseX, mouseY, guiX + 6, yOffset, GUI_WIDTH - 12, 24);

            // Фон модуля
            int bgColor = module.isEnabled() ? 0xFF2D2D44 : (hovered ? 0xFF1E1E2E : 0xFF181820);
            context.fill(guiX + 6, yOffset, guiX + GUI_WIDTH - 6, yOffset + 24, bgColor);

            // Индикатор слева
            if (module.isEnabled()) {
                context.fill(guiX + 6, yOffset, guiX + 9, yOffset + 24, 0xFF7B68EE);
            }

            // Имя
            int textColor = module.isEnabled() ? 0xFFA29BFE : 0xFFCCCCCC;
            context.drawText(this.textRenderer, module.getName(), guiX + 14, yOffset + 7, textColor, true);

            // Стрелка настроек для Aura
            if (module instanceof Aura) {
                String arrow = showingAuraSettings ? "v" : ">";
                context.drawText(this.textRenderer, arrow, guiX + GUI_WIDTH - 22, yOffset + 7, 0xFF666677, false);
            }

            yOffset += 28;
        }

        // Панель настроек Aura
        if (showingAuraSettings) {
            renderAuraSettings(context, guiX + GUI_WIDTH + 4, guiY, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderAuraSettings(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int width = 180;
        int height = 220;
        drawRoundedRect(context, x, y, width, height, 0xE00E0E16);

        context.drawText(this.textRenderer, "Aura Settings", x + 8, y + 6, 0xFF7B68EE, true);
        context.fill(x, y + 20, x + width, y + 21, 0xFF2A2A3A);

        Aura aura = (Aura) ExampleMod.moduleManager.getByName("Aura");
        if (aura == null) return;

        int sy = y + 28;

        // Target Mode — циклическое переключение
        context.drawText(this.textRenderer, "Targets: " + aura.targetMode.name(), x + 8, sy, 0xFFCCCCCC, false);
        if (isHovered(mouseX, mouseY, x + 8, sy - 2, width - 16, 12)) {
            context.fill(x + 8, sy - 2, x + width - 8, sy + 10, 0x30FFFFFF);
        }
        sy += 18;

        // Attack Range — слайдер
        context.drawText(this.textRenderer, "Range: " + String.format("%.1f", aura.attackRange), x + 8, sy, 0xFFCCCCCC, false);
        drawSlider(context, x + 8, sy + 12, width - 16, aura.attackRange / 6.0f, 0xFF7B68EE);
        if (isHovered(mouseX, mouseY, x + 8, sy + 10, width - 16, 8) && dragging) {
            float val = (mouseX - (x + 8)) / (float)(width - 16) * 6.0f;
            aura.setAttackRange(Math.max(1.0f, Math.min(6.0f, val)));
        }
        sy += 30;

        // Rotation Speed — слайдер
        context.drawText(this.textRenderer, "Smooth: " + String.format("%.0f", aura.rotationSpeed), x + 8, sy, 0xFFCCCCCC, false);
        drawSlider(context, x + 8, sy + 12, width - 16, aura.rotationSpeed / 45.0f, 0xFF7B68EE);
        if (isHovered(mouseX, mouseY, x + 8, sy + 10, width - 16, 8) && dragging) {
            float val = (mouseX - (x + 8)) / (float)(width - 16) * 45.0f;
            aura.setRotationSpeed(Math.max(1.0f, val));
        }
        sy += 30;

        // Attack Delay
        context.drawText(this.textRenderer, "Delay: " + aura.attackDelay + " ticks", x + 8, sy, 0xFFCCCCCC, false);
        drawSlider(context, x + 8, sy + 12, width - 16, aura.attackDelay / 40.0f, 0xFF7B68EE);
        if (isHovered(mouseX, mouseY, x + 8, sy + 10, width - 16, 8) && dragging) {
            int val = (int) ((mouseX - (x + 8)) / (float)(width - 16) * 40.0f);
            aura.setAttackDelay(Math.max(1, Math.min(40, val)));
        }
        sy += 30;

        // Smooth Rotation toggle
        String smoothText = "Smooth: " + (aura.smoothRotation ? "ON" : "OFF");
        int smoothColor = aura.smoothRotation ? 0xFF7B68EE : 0xFF555555;
        context.drawText(this.textRenderer, smoothText, x + 8, sy, smoothColor, true);
        if (isHovered(mouseX, mouseY, x + 8, sy - 2, width - 16, 12)) {
            context.fill(x + 8, sy - 2, x + width - 8, sy + 10, 0x30FFFFFF);
        }
    }

    private void drawSlider(DrawContext context, int x, int y, int width, float progress, int color) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        context.fill(x, y, x + width, y + 4, 0xFF2A2A3A);
        int fillWidth = (int) (width * progress);
        context.fill(x, y, x + fillWidth, y + 4, color);
        // Кружок на конце
        int circleX = x + fillWidth;
        context.fill(circleX - 3, y - 2, circleX + 3, y + 6, color);
    }

    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        // Простой прямоугольник с "закруглением" — верх/низ на 2px меньше
        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + width, y + height - 2, color);
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Drag заголовка
        if (isHovered(mx, my, guiX, guiY, GUI_WIDTH, 24)) {
            dragging = true;
            dragX = mx - guiX;
            dragY = my - guiY;
            return true;
        }

        // Клик по модулю
        int yOffset = guiY + 32;
        for (Module module : ExampleMod.moduleManager.getModules()) {
            if (isHovered(mx, my, guiX + 6, yOffset, GUI_WIDTH - 12, 24)) {
                if (button == 0) {
                    // ЛКМ — toggle
                    module.toggle();
                } else if (button == 1 && module instanceof Aura) {
                    // ПКМ — настройки
                    showingAuraSettings = !showingAuraSettings;
                }
                return true;
            }
            yOffset += 28;
        }

        // Клик по настройкам Aura
        if (showingAuraSettings) {
            int sx = guiX + GUI_WIDTH + 4;
            Aura aura = (Aura) ExampleMod.moduleManager.getByName("Aura");
            if (aura != null) {
                int sy = guiY + 28;
                // Target mode клик
                if (isHovered(mx, my, sx + 8, sy, 164, 14)) {
                    TargetModeCycle(aura);
                    return true;
                }
                sy += 18 + 30 + 30 + 30;
                // Smooth toggle
                if (isHovered(mx, my, sx + 8, sy, 164, 14)) {
                    aura.setSmoothRotation(!aura.smoothRotation);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void TargetModeCycle(Aura aura) {
        switch (aura.targetMode) {
            case PLAYERS_AND_MOBS -> aura.setTargetMode(Aura.TargetMode.PLAYERS_ONLY);
            case PLAYERS_ONLY -> aura.setTargetMode(Aura.TargetMode.MOBS_ONLY);
            case MOBS_ONLY -> aura.setTargetMode(Aura.TargetMode.PLAYERS_AND_MOBS);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            guiX = (int) mouseX - dragX;
            guiY = (int) mouseY - dragY;
            return true;
        }
        // Слайдеры
        Aura aura = (Aura) ExampleMod.moduleManager.getByName("Aura");
        if (aura != null && showingAuraSettings) {
            int sx = guiX + GUI_WIDTH + 4;
            int width = 180;
            int sy = guiY + 28 + 18; // после target mode

            // Range slider
            if (isHovered((int) mouseX, (int) mouseY, sx + 8, sy + 10, width - 16, 10)) {
                float val = ((float) mouseX - (sx + 8)) / (width - 16) * 6.0f;
                aura.setAttackRange(Math.max(1.0f, Math.min(6.0f, val)));
                return true;
            }
            sy += 30;
            // Rotation speed slider
            if (isHovered((int) mouseX, (int) mouseY, sx + 8, sy + 10, width - 16, 10)) {
                float val = ((float) mouseX - (sx + 8)) / (width - 16) * 45.0f;
                aura.setRotationSpeed(Math.max(1.0f, val));
                return true;
            }
            sy += 30;
            // Delay slider
            if (isHovered((int) mouseX, (int) mouseY, sx + 8, sy + 10, width - 16, 10)) {
                int val = (int) (((float) mouseX - (sx + 8)) / (width - 16) * 40.0f);
                aura.setAttackDelay(Math.max(1, Math.min(40, val)));
                return true;
            }
        }
        return super.mouseDragged(mouseY, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
                   }

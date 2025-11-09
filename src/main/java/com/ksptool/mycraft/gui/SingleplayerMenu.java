package com.ksptool.mycraft.gui;

import com.ksptool.mycraft.core.Input;
import com.ksptool.mycraft.rendering.GuiRenderer;
import com.ksptool.mycraft.rendering.TextRenderer;
import com.ksptool.mycraft.world.WorldManager;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.List;

/**
 * 单人游戏菜单界面类，显示已保存的世界列表并处理世界选择
 */
public class SingleplayerMenu {
    private static final float BUTTON_WIDTH = 200.0f;
    private static final float BUTTON_HEIGHT = 40.0f;
    private static final float BUTTON_SPACING = 10.0f;
    private static final float WORLD_ITEM_HEIGHT = 30.0f;
    private static final float WORLD_LIST_Y = 150.0f;
    private static final float WORLD_LIST_HEIGHT = 400.0f;

    private int scrollOffset = 0;
    private String selectedWorld = null;

    public void render(GuiRenderer guiRenderer, int windowWidth, int windowHeight, Input input) {
        float centerX = windowWidth / 2.0f;

        float createWorldY = 50.0f;
        float backY = windowHeight - 60.0f;

        float buttonX = centerX - BUTTON_WIDTH / 2.0f;

        Vector2d mousePos = input.getMousePosition();

        boolean createWorldHovered = isMouseOverButton(mousePos.x, mousePos.y, buttonX, createWorldY, BUTTON_WIDTH, BUTTON_HEIGHT);
        boolean backHovered = isMouseOverButton(mousePos.x, mousePos.y, buttonX, backY, BUTTON_WIDTH, BUTTON_HEIGHT);

        guiRenderer.renderButton(buttonX, createWorldY, BUTTON_WIDTH, BUTTON_HEIGHT, "创建新世界", createWorldHovered, windowWidth, windowHeight);
        guiRenderer.renderButton(buttonX, backY, BUTTON_WIDTH, BUTTON_HEIGHT, "返回", backHovered, windowWidth, windowHeight);

        List<String> worlds = WorldManager.getInstance().getWorldList();
        renderWorldList(guiRenderer, worlds, windowWidth, windowHeight);
    }

    private void renderWorldList(GuiRenderer guiRenderer, List<String> worlds, int windowWidth, int windowHeight) {
        if (worlds == null || worlds.isEmpty()) {
            TextRenderer textRenderer = guiRenderer.getTextRenderer();
            if (textRenderer != null) {
                float centerX = windowWidth / 2.0f;
                float textX = centerX - textRenderer.getTextWidth("没有已保存的世界", 1.0f) / 2.0f;
                textRenderer.renderText(guiRenderer, textX, WORLD_LIST_Y + 50.0f, "没有已保存的世界", 1.0f, new Vector3f(1.0f, 1.0f, 1.0f), windowWidth, windowHeight);
            }
            return;
        }

        float listX = windowWidth / 2.0f - 300.0f;
        float currentY = WORLD_LIST_Y;

        int visibleCount = (int) (WORLD_LIST_HEIGHT / WORLD_ITEM_HEIGHT);
        int startIndex = Math.max(0, scrollOffset);
        int endIndex = Math.min(worlds.size(), startIndex + visibleCount);

        for (int i = startIndex; i < endIndex; i++) {
            String worldName = worlds.get(i);
            boolean isSelected = worldName.equals(selectedWorld);
            
            org.joml.Vector4f bgColor;
            if (isSelected) {
                bgColor = new org.joml.Vector4f(0.3f, 0.5f, 0.8f, 0.9f);
            } else {
                bgColor = new org.joml.Vector4f(0.2f, 0.2f, 0.2f, 0.9f);
            }

            guiRenderer.renderQuad(listX, currentY, 600.0f, WORLD_ITEM_HEIGHT, bgColor, windowWidth, windowHeight);

            TextRenderer textRenderer = guiRenderer.getTextRenderer();
            if (textRenderer != null) {
                textRenderer.renderText(guiRenderer, listX + 10.0f, currentY + 5.0f, worldName, 1.0f, new Vector3f(1.0f, 1.0f, 1.0f), windowWidth, windowHeight);
            }

            currentY += WORLD_ITEM_HEIGHT;
        }
    }

    public int handleInput(Input input, int windowWidth, int windowHeight) {
        Vector2d mousePos = input.getMousePosition();
        float centerX = windowWidth / 2.0f;

        float createWorldY = 50.0f;
        float backY = windowHeight - 60.0f;
        float buttonX = centerX - BUTTON_WIDTH / 2.0f;

        if (input.isMouseButtonPressed(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (isMouseOverButton(mousePos.x, mousePos.y, buttonX, createWorldY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                return 1;
            }
            if (isMouseOverButton(mousePos.x, mousePos.y, buttonX, backY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                return 2;
            }

            List<String> worlds = WorldManager.getInstance().getWorldList();
            float listX = windowWidth / 2.0f - 300.0f;
            float currentY = WORLD_LIST_Y;

            int startIndex = Math.max(0, scrollOffset);
            int endIndex = Math.min(worlds.size(), startIndex + (int)(WORLD_LIST_HEIGHT / WORLD_ITEM_HEIGHT));

            for (int i = startIndex; i < endIndex; i++) {
                if (isMouseOverButton(mousePos.x, mousePos.y, listX, currentY, 600.0f, WORLD_ITEM_HEIGHT)) {
                    selectedWorld = worlds.get(i);
                    return 3;
                }
                currentY += WORLD_ITEM_HEIGHT;
            }
        }

        double scrollY = input.getScrollY();
        if (scrollY != 0) {
            List<String> worlds = WorldManager.getInstance().getWorldList();
            int maxScroll = Math.max(0, worlds.size() - (int)(WORLD_LIST_HEIGHT / WORLD_ITEM_HEIGHT));
            scrollOffset += (int)scrollY;
            if (scrollOffset < 0) {
                scrollOffset = 0;
            }
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll;
            }
        }

        return 0;
    }

    public String getSelectedWorld() {
        return selectedWorld;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, float buttonX, float buttonY, float buttonWidth, float buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
               mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }
}


package com.ksptool.mycraft.gui;

import com.ksptool.mycraft.core.Input;
import com.ksptool.mycraft.rendering.GuiRenderer;
import com.ksptool.mycraft.rendering.TextRenderer;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建世界菜单界面类，处理世界名称输入和创建操作
 */
public class CreateWorldMenu {
    private static final float BUTTON_WIDTH = 200.0f;
    private static final float BUTTON_HEIGHT = 40.0f;
    private static final float INPUT_WIDTH = 400.0f;
    private static final float INPUT_HEIGHT = 40.0f;

    private StringBuilder worldNameInput = new StringBuilder();
    private boolean isTyping = false;

    public void render(GuiRenderer guiRenderer, int windowWidth, int windowHeight, Input input) {
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;

        float inputY = centerY - 50.0f;
        float createY = centerY + 30.0f;
        float cancelY = centerY + 80.0f;

        float inputX = centerX - INPUT_WIDTH / 2.0f;
        float buttonX = centerX - BUTTON_WIDTH / 2.0f;

        Vector4f inputBgColor = new Vector4f(0.3f, 0.3f, 0.3f, 0.9f);
        if (isTyping) {
            inputBgColor = new Vector4f(0.4f, 0.4f, 0.4f, 0.9f);
        }

        guiRenderer.renderQuad(inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT, inputBgColor, windowWidth, windowHeight);

        TextRenderer textRenderer = guiRenderer.getTextRenderer();
        if (textRenderer != null) {
            String displayText = worldNameInput.length() > 0 ? worldNameInput.toString() : "输入世界名称...";
            if (isTyping && worldNameInput.length() == 0) {
                displayText = "输入世界名称...";
            }
            float textX = inputX + 10.0f;
            float textY = inputY + 10.0f;
            Vector3f textColor = worldNameInput.length() > 0 ? new Vector3f(1.0f, 1.0f, 1.0f) : new Vector3f(0.7f, 0.7f, 0.7f);
            textRenderer.renderText(guiRenderer, textX, textY, displayText, 1.0f, textColor, windowWidth, windowHeight);
        }

        Vector2d mousePos = input.getMousePosition();
        boolean createHovered = isMouseOverButton(mousePos.x, mousePos.y, buttonX, createY, BUTTON_WIDTH, BUTTON_HEIGHT);
        boolean cancelHovered = isMouseOverButton(mousePos.x, mousePos.y, buttonX, cancelY, BUTTON_WIDTH, BUTTON_HEIGHT);

        guiRenderer.renderButton(buttonX, createY, BUTTON_WIDTH, BUTTON_HEIGHT, "创建", createHovered, windowWidth, windowHeight);
        guiRenderer.renderButton(buttonX, cancelY, BUTTON_WIDTH, BUTTON_HEIGHT, "取消", cancelHovered, windowWidth, windowHeight);
    }

    public int handleInput(Input input, int windowWidth, int windowHeight) {
        Vector2d mousePos = input.getMousePosition();
        float centerX = windowWidth / 2.0f;
        float centerY = windowHeight / 2.0f;

        float inputY = centerY - 50.0f;
        float createY = centerY + 30.0f;
        float cancelY = centerY + 80.0f;

        float inputX = centerX - INPUT_WIDTH / 2.0f;
        float buttonX = centerX - BUTTON_WIDTH / 2.0f;

        if (input.isMouseButtonPressed(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (isMouseOverButton(mousePos.x, mousePos.y, inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT)) {
                isTyping = true;
            } else {
                isTyping = false;
            }

            if (isMouseOverButton(mousePos.x, mousePos.y, buttonX, createY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                String worldName = worldNameInput.toString().trim();
                if (!StringUtils.isBlank(worldName)) {
                    return 1;
                }
            }

            if (isMouseOverButton(mousePos.x, mousePos.y, buttonX, cancelY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                worldNameInput.setLength(0);
                isTyping = false;
                return 2;
            }
        }

        if (isTyping) {
            handleTextInput(input);
        }

        return 0;
    }

    private void handleTextInput(Input input) {
        for (int key = org.lwjgl.glfw.GLFW.GLFW_KEY_A; key <= org.lwjgl.glfw.GLFW.GLFW_KEY_Z; key++) {
            if (input.isKeyPressed(key)) {
                char c = (char) ('a' + (key - org.lwjgl.glfw.GLFW.GLFW_KEY_A));
                if (input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) || 
                    input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                    c = Character.toUpperCase(c);
                }
                worldNameInput.append(c);
                return;
            }
        }

        for (int key = org.lwjgl.glfw.GLFW.GLFW_KEY_0; key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9; key++) {
            if (input.isKeyPressed(key)) {
                char c = (char) ('0' + (key - org.lwjgl.glfw.GLFW.GLFW_KEY_0));
                worldNameInput.append(c);
                return;
            }
        }

        if (input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE)) {
            worldNameInput.append(' ');
        }

        if (input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) && worldNameInput.length() > 0) {
            worldNameInput.setLength(worldNameInput.length() - 1);
        }
    }

    public String getWorldName() {
        return worldNameInput.toString().trim();
    }

    public void reset() {
        worldNameInput.setLength(0);
        isTyping = false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, float buttonX, float buttonY, float buttonWidth, float buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
               mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }
}


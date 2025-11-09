package com.ksptool.mycraft.rendering;

import com.ksptool.mycraft.entity.Player;
import com.ksptool.mycraft.item.Inventory;
import com.ksptool.mycraft.item.ItemStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

/**
 * 快捷栏渲染器类，负责渲染玩家快捷栏界面
 */
public class HotbarRenderer {
    private ShaderProgram uiShader;
    private int vaoId;
    private int vboId;
    private static final float[] quadVertices = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        0.0f, 1.0f
    };

    public void init() {
        uiShader = new ShaderProgram("/shaders/ui_vertex.glsl", "/shaders/ui_fragment.glsl");
        
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(quadVertices.length);
        verticesBuffer.put(quadVertices);
        verticesBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(verticesBuffer);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        glBindVertexArray(0);
    }

    public void render(Player player, int windowWidth, int windowHeight) {
        if (uiShader == null) {
            init();
        }

        Inventory inventory = player.getInventory();
        int hotbarSize = 9;
        int slotSize = 50;
        int hotbarWidth = hotbarSize * slotSize;
        int hotbarX = (windowWidth - hotbarWidth) / 2;
        int hotbarY = windowHeight - 80;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        uiShader.bind();
        uiShader.setUniform("screenSize", new Vector2f(windowWidth, windowHeight));

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);

        for (int i = 0; i < hotbarSize; i++) {
            int x = hotbarX + i * slotSize;
            int y = hotbarY;

            Vector3f color;
            if (i == inventory.getSelectedSlot()) {
                color = new Vector3f(1.0f, 1.0f, 1.0f);
            } else {
                color = new Vector3f(0.5f, 0.5f, 0.5f);
            }

            renderQuad(x, y, slotSize, slotSize, color, windowWidth, windowHeight);

            Vector3f borderColor = new Vector3f(0.0f, 0.0f, 0.0f);
            renderQuadBorder(x, y, slotSize, slotSize, borderColor, windowWidth, windowHeight);

            ItemStack stack = inventory.getHotbar()[i];
            if (stack != null && !stack.isEmpty()) {
                Vector3f itemColor = new Vector3f(0.8f, 0.8f, 0.8f);
                renderQuad(x + 5, y + 5, slotSize - 10, slotSize - 10, itemColor, windowWidth, windowHeight);
            }
        }

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        uiShader.unbind();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void renderQuad(float x, float y, float width, float height, Vector3f color, int windowWidth, int windowHeight) {
        uiShader.setUniform("position", new Vector2f(x, y));
        uiShader.setUniform("size", new Vector2f(width, height));
        uiShader.setUniform("color", color);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 4);
    }

    private void renderQuadBorder(float x, float y, float width, float height, Vector3f color, int windowWidth, int windowHeight) {
        float borderWidth = 2.0f;
        
        renderQuad(x, y, width, borderWidth, color, windowWidth, windowHeight);
        renderQuad(x, y + height - borderWidth, width, borderWidth, color, windowWidth, windowHeight);
        renderQuad(x, y, borderWidth, height, color, windowWidth, windowHeight);
        renderQuad(x + width - borderWidth, y, borderWidth, height, color, windowWidth, windowHeight);
    }

    public void cleanup() {
        if (uiShader != null) {
            uiShader.cleanup();
        }
        if (vaoId != 0) {
            glDeleteVertexArrays(vaoId);
        }
        if (vboId != 0) {
            glDeleteBuffers(vboId);
        }
    }
}

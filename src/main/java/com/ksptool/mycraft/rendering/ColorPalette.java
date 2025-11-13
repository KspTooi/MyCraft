package com.ksptool.mycraft.rendering;

import org.joml.Vector3f;

/**
 * 颜色调色板常量类，统一管理所有硬编码的颜色值
 */
public class ColorPalette {
    // Sky Colors
    public static final Vector3f SKY_MIDNIGHT = new Vector3f(0.05f, 0.05f, 0.15f);
    public static final Vector3f SKY_SUNRISE_START = new Vector3f(1.0f, 0.5f, 0.1f);
    public static final Vector3f SKY_SUNRISE_END = new Vector3f(0.4f, 0.7f, 0.9f);
    public static final Vector3f SKY_NOON = new Vector3f(0.48f, 0.75f, 0.94f);
    public static final Vector3f SKY_SUNSET_START = new Vector3f(0.4f, 0.7f, 0.9f);
    public static final Vector3f SKY_SUNSET_END = new Vector3f(1.0f, 0.5f, 0.1f);

    // Ambient Light Colors
    public static final Vector3f AMBIENT_MIDNIGHT = new Vector3f(0.1f, 0.1f, 0.15f);
    public static final Vector3f AMBIENT_NOON = new Vector3f(1.0f, 1.0f, 1.0f);
}


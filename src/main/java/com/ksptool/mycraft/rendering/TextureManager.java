package com.ksptool.mycraft.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final int TEXTURE_SIZE = 16;
    private static final int ATLAS_SIZE = 1024;
    private static TextureManager instance;
    
    private Map<String, UVCoords> textureUVMap;
    private int[] atlasPixels;
    private int atlasWidth;
    private int atlasHeight;
    
    public static class UVCoords {
        public float u0, v0, u1, v1;
        
        public UVCoords(float u0, float v0, float u1, float v1) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }
    }
    
    private TextureManager() {
        textureUVMap = new HashMap<>();
    }
    
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }
    
    public void loadAtlas() {
        String texturePath = "/textures/blocks/";
        java.util.List<String> textureFiles = new java.util.ArrayList<>();
        
        try {
            java.io.File dir = new java.io.File("src/main/resources" + texturePath);
            if (dir.exists() && dir.isDirectory()) {
                java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".png") && !name.endsWith(".mcmeta"));
                if (files != null) {
                    for (java.io.File file : files) {
                        textureFiles.add(file.getName());
                    }
                }
            }
            
            if (textureFiles.isEmpty()) {
                java.net.URL resourceUrl = getClass().getResource(texturePath);
                if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                    java.io.File resourceDir = new java.io.File(resourceUrl.getPath());
                    if (resourceDir.exists() && resourceDir.isDirectory()) {
                        java.io.File[] resourceFiles = resourceDir.listFiles((d, name) -> name.endsWith(".png") && !name.endsWith(".mcmeta"));
                        if (resourceFiles != null) {
                            for (java.io.File file : resourceFiles) {
                                textureFiles.add(file.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing texture directory: " + e.getMessage());
        }
        
        if (textureFiles.isEmpty()) {
            textureFiles.add("dirt.png");
            textureFiles.add("grass_top.png");
            textureFiles.add("grass_side.png");
            textureFiles.add("stone.png");
            textureFiles.add("log_oak.png");
            textureFiles.add("leaves_oak.png");
        }
        
        java.util.Collections.sort(textureFiles);
        
        int tilesPerRow = ATLAS_SIZE / TEXTURE_SIZE;
        int currentX = 0;
        int currentY = 0;
        
        atlasPixels = new int[ATLAS_SIZE * ATLAS_SIZE];
        atlasWidth = ATLAS_SIZE;
        atlasHeight = ATLAS_SIZE;
        
        for (String textureName : textureFiles) {
            if (textureName.endsWith(".mcmeta")) {
                continue;
            }
            
            if (currentX >= tilesPerRow) {
                currentX = 0;
                currentY++;
                if (currentY * TEXTURE_SIZE >= ATLAS_SIZE) {
                    System.err.println("Atlas too small! Cannot fit all textures.");
                    break;
                }
            }
            
            int[] texturePixels = loadTexture(texturePath + textureName);
            if (texturePixels == null) {
                continue;
            }
            
            int atlasX = currentX * TEXTURE_SIZE;
            int atlasY = currentY * TEXTURE_SIZE;
            
            for (int y = 0; y < TEXTURE_SIZE; y++) {
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    int srcIndex = y * TEXTURE_SIZE + x;
                    int dstIndex = (atlasY + y) * ATLAS_SIZE + (atlasX + x);
                    if (dstIndex < atlasPixels.length && srcIndex < texturePixels.length) {
                        atlasPixels[dstIndex] = texturePixels[srcIndex];
                    }
                }
            }
            
            float u0 = (float) atlasX / ATLAS_SIZE;
            float v0 = (float) atlasY / ATLAS_SIZE;
            float u1 = (float) (atlasX + TEXTURE_SIZE) / ATLAS_SIZE;
            float v1 = (float) (atlasY + TEXTURE_SIZE) / ATLAS_SIZE;
            
            textureUVMap.put(textureName, new UVCoords(u0, v0, u1, v1));
            
            currentX++;
        }
    }
    
    private int[] loadTexture(String path) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            if (inputStream == null) {
                java.io.File file = new java.io.File("src/main/resources" + path);
                if (file.exists()) {
                    inputStream = new java.io.FileInputStream(file);
                } else {
                    System.err.println("Texture not found: " + path);
                    return null;
                }
            }
            
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();
            
            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();
            
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (image == null) {
                System.err.println("Failed to load texture: " + path + " - " + STBImage.stbi_failure_reason());
                return null;
            }
            
            int imgWidth = width.get(0);
            int imgHeight = height.get(0);
            
            int[] pixels = new int[TEXTURE_SIZE * TEXTURE_SIZE];
            int imageSize = imgWidth * imgHeight * 4;
            
            for (int y = 0; y < TEXTURE_SIZE; y++) {
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    int srcX = (x * imgWidth) / TEXTURE_SIZE;
                    int srcY = (y * imgHeight) / TEXTURE_SIZE;
                    int srcIndex = (srcY * imgWidth + srcX) * 4;
                    
                    if (srcIndex + 3 < imageSize && srcIndex + 3 < image.capacity()) {
                        int r = image.get(srcIndex) & 0xFF;
                        int g = image.get(srcIndex + 1) & 0xFF;
                        int b = image.get(srcIndex + 2) & 0xFF;
                        int a = image.get(srcIndex + 3) & 0xFF;
                        
                        pixels[y * TEXTURE_SIZE + x] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }
            
            STBImage.stbi_image_free(image);
            return pixels;
        } catch (IOException e) {
            System.err.println("Error loading texture: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    public UVCoords getUVCoords(String textureName) {
        return textureUVMap.get(textureName);
    }
    
    public int[] getAtlasPixels() {
        return atlasPixels;
    }
    
    public int getAtlasWidth() {
        return atlasWidth;
    }
    
    public int getAtlasHeight() {
        return atlasHeight;
    }
}


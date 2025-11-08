package com.ksptool.mycraft.world;

public class MeshGenerationResult {
    public final float[] vertices;
    public final float[] texCoords;
    public final float[] tints;
    public final int[] indices;
    public final Chunk chunk;

    public MeshGenerationResult(Chunk chunk, float[] vertices, float[] texCoords, float[] tints, int[] indices) {
        this.chunk = chunk;
        this.vertices = vertices;
        this.texCoords = texCoords;
        this.tints = tints;
        this.indices = indices;
    }
}


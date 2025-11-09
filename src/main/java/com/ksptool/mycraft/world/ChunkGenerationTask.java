package com.ksptool.mycraft.world;

/**
 * 区块生成任务类，封装区块生成任务的状态信息
 */
public class ChunkGenerationTask {
    private final int chunkX;
    private final int chunkZ;
    private Chunk chunk;
    private boolean dataGenerated;

    public ChunkGenerationTask(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.dataGenerated = false;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public boolean isDataGenerated() {
        return dataGenerated;
    }

    public void setDataGenerated(boolean dataGenerated) {
        this.dataGenerated = dataGenerated;
    }
}


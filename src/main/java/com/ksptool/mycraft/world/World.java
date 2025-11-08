package com.ksptool.mycraft.world;

import com.ksptool.mycraft.entity.BoundingBox;
import com.ksptool.mycraft.entity.Entity;
import com.ksptool.mycraft.rendering.ShaderProgram;
import com.ksptool.mycraft.rendering.TextureManager;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class World {
    private Map<String, Chunk> chunks;
    private static final int RENDER_DISTANCE = 8;
    private int textureId;
    private float timeOfDay = 0.5f;
    private float timeSpeed = 0.0001f;
    
    private final BlockingQueue<ChunkGenerationTask> generationQueue;
    private final Map<String, ChunkGenerationTask> pendingChunks;
    private WorldGenerator worldGenerator;
    private ChunkMeshGenerator chunkMeshGenerator;
    
    private final List<Entity> entities;

    public World() {
        this.chunks = new ConcurrentHashMap<>();
        this.generationQueue = new LinkedBlockingQueue<>();
        this.pendingChunks = new ConcurrentHashMap<>();
        this.entities = new ArrayList<>();
    }

    public void init() {
        System.out.println("Initializing world...");
        loadTexture();
        chunkMeshGenerator = new ChunkMeshGenerator(this);
        worldGenerator = new WorldGenerator(this, generationQueue);
        worldGenerator.start();
        System.out.println("World initialized");
    }

    private void loadTexture() {
        TextureManager textureManager = TextureManager.getInstance();
        textureManager.loadAtlas();
        
        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        int[] pixels = textureManager.getAtlasPixels();
        int atlasWidth = textureManager.getAtlasWidth();
        int atlasHeight = textureManager.getAtlasHeight();

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(atlasWidth * atlasHeight * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        org.lwjgl.opengl.GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, atlasWidth, atlasHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public void update(Vector3f playerPosition) {
        long updateStartTime = System.nanoTime();
        
        timeOfDay += timeSpeed;
        if (timeOfDay > 1.0f) {
            timeOfDay -= 1.0f;
        }

        int playerChunkX = (int) Math.floor(playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(playerPosition.z / Chunk.CHUNK_SIZE);

        for (int x = playerChunkX - RENDER_DISTANCE; x <= playerChunkX + RENDER_DISTANCE; x++) {
            for (int z = playerChunkZ - RENDER_DISTANCE; z <= playerChunkZ + RENDER_DISTANCE; z++) {
                String key = x + "," + z;
                if (!chunks.containsKey(key) && !pendingChunks.containsKey(key)) {
                    ChunkGenerationTask task = new ChunkGenerationTask(x, z);
                    pendingChunks.put(key, task);
                    generationQueue.offer(task);
                }
            }
        }

        for (int x = playerChunkX - RENDER_DISTANCE; x <= playerChunkX + RENDER_DISTANCE; x++) {
            for (int z = playerChunkZ - RENDER_DISTANCE; z <= playerChunkZ + RENDER_DISTANCE; z++) {
                String key = x + "," + z;
                ChunkGenerationTask task = pendingChunks.get(key);
                if (task != null && task.isDataGenerated() && task.getChunk() != null) {
                    Chunk chunk = task.getChunk();
                    if (!chunks.containsKey(key)) {
                        chunks.put(key, chunk);
                    }
                    if (chunk.getState() == Chunk.ChunkState.DATA_LOADED) {
                        chunkMeshGenerator.submitMeshTask(chunk);
                        chunk.setState(Chunk.ChunkState.AWAITING_MESH);
                    }
                }
            }
        }
        
        

        chunks.entrySet().removeIf(entry -> {
            String[] parts = entry.getKey().split(",");
            int chunkX = Integer.parseInt(parts[0]);
            int chunkZ = Integer.parseInt(parts[1]);
            int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
            if (distance > RENDER_DISTANCE + 5) {
                entry.getValue().cleanup();
                pendingChunks.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        

        long updateElapsed = System.nanoTime() - updateStartTime;
        if (updateElapsed > 10_000_000) {
            System.out.println("World.update took " + (updateElapsed / 1_000_000) + "ms");
        }
    }

    public void generateChunkData(Chunk chunk) {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        GlobalPalette palette = GlobalPalette.getInstance();
        Registry registry = Registry.getInstance();

        Block grassBlock = registry.get("mycraft:grass_block");
        Block dirtBlock = registry.get("mycraft:dirt");
        Block stoneBlock = registry.get("mycraft:stone");
        
        int grassStateId = palette.getStateId(grassBlock.getDefaultState());
        int dirtStateId = palette.getStateId(dirtBlock.getDefaultState());
        int stoneStateId = palette.getStateId(stoneBlock.getDefaultState());

        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                int worldX = chunkX * Chunk.CHUNK_SIZE + x;
                int worldZ = chunkZ * Chunk.CHUNK_SIZE + z;

                double noiseValue = NoiseGenerator.noise(worldX * 0.05, worldZ * 0.05);
                int height = (int) (64 + noiseValue * 20);

                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    if (y > height) {
                        continue;
                    }
                    if (y == height) {
                        chunk.setBlockState(x, y, z, grassStateId);
                    } else if (y > height - 3) {
                        chunk.setBlockState(x, y, z, dirtStateId);
                    } else {
                        chunk.setBlockState(x, y, z, stoneStateId);
                    }
                }
            }
        }
    }
    
    public void generateChunkSynchronously(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (chunks.containsKey(key)) {
            return;
        }
        
        Chunk chunk = new Chunk(chunkX, chunkZ);
        generateChunkData(chunk);
        chunk.setState(Chunk.ChunkState.DATA_LOADED);
        MeshGenerationResult result = chunk.calculateMeshData(this);
        if (result != null) {
            chunk.uploadToGPU(result);
        }
        chunks.put(key, chunk);
        System.out.println("Synchronously generated chunk [" + chunkX + "," + chunkZ + "]");
    }
    
    public int getHeightAt(int worldX, int worldZ) {
        double noiseValue = NoiseGenerator.noise(worldX * 0.05, worldZ * 0.05);
        return (int) (64 + noiseValue * 20);
    }

    public void render(ShaderProgram shader) {
        if (chunks.isEmpty()) {
            System.out.println("World.render: No chunks to render");
            return;
        }
        
        int chunksWithMesh = 0;
        for (Chunk chunk : chunks.values()) {
            if (chunk != null && chunk.hasMesh()) {
                chunksWithMesh++;
            }
        }
        
        System.out.println("World.render: Rendering " + chunksWithMesh + " chunks (total: " + chunks.size() + ")");
        
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        shader.setUniform("textureSampler", 0);
        
        if (textureId == 0) {
            System.err.println("WARNING: Texture ID is 0!");
        }

        int rendered = 0;
        for (Chunk chunk : chunks.values()) {
            if (chunk != null && chunk.hasMesh()) {
                chunk.render();
                rendered++;
            }
        }
        
        System.out.println("World.render: Actually rendered " + rendered + " chunks");
        
        if (rendered == 0) {
            System.out.println("WARNING: No chunks were rendered despite " + chunks.size() + " chunks loaded!");
        }
    }
    
    public int getChunkCount() {
        return chunks.size();
    }

    public int getBlockState(int x, int y, int z) {
        int chunkX = (int) Math.floor((float) x / Chunk.CHUNK_SIZE);
        int chunkZ = (int) Math.floor((float) z / Chunk.CHUNK_SIZE);
        String key = chunkX + "," + chunkZ;
        Chunk chunk = chunks.get(key);
        if (chunk == null) {
            return 0;
        }
        int localX = x - chunkX * Chunk.CHUNK_SIZE;
        int localZ = z - chunkZ * Chunk.CHUNK_SIZE;
        return chunk.getBlockState(localX, y, localZ);
    }

    public void setBlockState(int x, int y, int z, int stateId) {
        int chunkX = (int) Math.floor((float) x / Chunk.CHUNK_SIZE);
        int chunkZ = (int) Math.floor((float) z / Chunk.CHUNK_SIZE);
        String key = chunkX + "," + chunkZ;
        Chunk chunk = chunks.get(key);
        if (chunk == null) {
            return;
        }
        int localX = x - chunkX * Chunk.CHUNK_SIZE;
        int localZ = z - chunkZ * Chunk.CHUNK_SIZE;
        chunk.setBlockState(localX, y, localZ, stateId);
        if (chunk.getState() == Chunk.ChunkState.READY) {
            chunk.setState(Chunk.ChunkState.DATA_LOADED);
        }
        chunkMeshGenerator.submitMeshTask(chunk);
        chunk.setState(Chunk.ChunkState.AWAITING_MESH);

        int[][] neighborOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] offset : neighborOffsets) {
            String neighborKey = (chunkX + offset[0]) + "," + (chunkZ + offset[1]);
            Chunk neighborChunk = chunks.get(neighborKey);
            if (neighborChunk != null) {
                if (neighborChunk.getState() == Chunk.ChunkState.READY) {
                    neighborChunk.setState(Chunk.ChunkState.DATA_LOADED);
                }
                chunkMeshGenerator.submitMeshTask(neighborChunk);
                neighborChunk.setState(Chunk.ChunkState.AWAITING_MESH);
            }
        }
    }

    public boolean canMoveTo(Vector3f position, float height) {
        int minX = (int) Math.floor(position.x - 0.3f);
        int maxX = (int) Math.floor(position.x + 0.3f);
        int minY = (int) Math.floor(position.y);
        int maxY = (int) Math.floor(position.y + height);
        int minZ = (int) Math.floor(position.z - 0.3f);
        int maxZ = (int) Math.floor(position.z + 0.3f);

        GlobalPalette palette = GlobalPalette.getInstance();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int stateId = getBlockState(x, y, z);
                    BlockState state = palette.getState(stateId);
                    Block block = state.getBlock();
                    if (block.isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean canMoveTo(BoundingBox box) {
        int minX = (int) Math.floor(box.getMinX());
        int maxX = (int) Math.floor(box.getMaxX());
        int minY = (int) Math.floor(box.getMinY());
        int maxY = (int) Math.floor(box.getMaxY());
        int minZ = (int) Math.floor(box.getMinZ());
        int maxZ = (int) Math.floor(box.getMaxZ());

        GlobalPalette palette = GlobalPalette.getInstance();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int stateId = getBlockState(x, y, z);
                    BlockState state = palette.getState(stateId);
                    Block block = state.getBlock();
                    if (block.isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }

    public void cleanup() {
        if (worldGenerator != null) {
            worldGenerator.stopGenerator();
            try {
                worldGenerator.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        for (Chunk chunk : chunks.values()) {
            chunk.cleanup();
        }
        chunks.clear();
        pendingChunks.clear();
        generationQueue.clear();
        if (chunkMeshGenerator != null) {
            chunkMeshGenerator.shutdown();
        }
        GL11.glDeleteTextures(textureId);
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public org.joml.Vector3f getSkyColor() {
        float brightness = (float) (0.3 + 0.7 * Math.sin(timeOfDay * Math.PI * 2));
        return new org.joml.Vector3f(brightness, brightness, brightness);
    }

    public ChunkMeshGenerator getChunkMeshGenerator() {
        return chunkMeshGenerator;
    }
}


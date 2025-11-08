package com.ksptool.mycraft.core;

import com.ksptool.mycraft.world.World;
import com.ksptool.mycraft.entity.Player;
import com.ksptool.mycraft.rendering.Renderer;
import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.GlobalPalette;

public class Game {
    private Window window;
    private Input input;
    private Renderer renderer;
    private World world;
    private Player player;
    private boolean running;

    public void init() {
        Block.registerBlocks();
        GlobalPalette.getInstance().bake();
        
        window = new Window(1280, 720, "MyCraft");
        window.init();
        input = new Input(window.getWindowHandle());
        input.setMouseLocked(true);

        renderer = new Renderer();
        renderer.init();
        renderer.resize(window.getWidth(), window.getHeight());

        world = new World();
        world.init();
        
        float initialX = 8.0f;
        float initialZ = 8.0f;
        
        int playerChunkX = (int) Math.floor(initialX / com.ksptool.mycraft.world.Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(initialZ / com.ksptool.mycraft.world.Chunk.CHUNK_SIZE);
        
        System.out.println("Pre-generating chunks around player spawn at chunk [" + playerChunkX + "," + playerChunkZ + "]");
        
        for (int x = playerChunkX - 2; x <= playerChunkX + 2; x++) {
            for (int z = playerChunkZ - 2; z <= playerChunkZ + 2; z++) {
                world.generateChunkSynchronously(x, z);
            }
        }
        
        int groundHeight = world.getHeightAt((int) initialX, (int) initialZ);
        float initialY = groundHeight + 1.0f;
        
        System.out.println("Player spawn height calculated: " + initialY + " (ground at " + groundHeight + ")");
        
        player = new Player(world);
        player.getPosition().set(initialX, initialY, initialZ);
        player.initializeCamera();
        world.addEntity(player);

        running = true;
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void loop() {
        double lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;

        while (running && !window.shouldClose()) {
            double now = System.nanoTime();
            double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            if (deltaSeconds > 0.1) {
                deltaSeconds = 0.1;
            }

            update((float) deltaSeconds);
            updates++;

            render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void update(float delta) {
        long updateStartTime = System.nanoTime();
        
        input.update();

        if (input.isKeyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE)) {
            running = false;
        }

        long entityUpdateStart = System.nanoTime();
        for (com.ksptool.mycraft.entity.Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                // Skip player here, will be updated separately
                continue;
            }
            entity.update(delta);
        }
        long entityUpdateTime = System.nanoTime() - entityUpdateStart;
        
        long playerUpdateStart = System.nanoTime();
        player.update(input, delta);
        long playerUpdateTime = System.nanoTime() - playerUpdateStart;
        
        long worldUpdateStart = System.nanoTime();
        world.update(player.getPosition());
        long worldUpdateTime = System.nanoTime() - worldUpdateStart;
        
        long meshUploadStart = System.nanoTime();
        com.ksptool.mycraft.world.ChunkMeshGenerator meshGenerator = world.getChunkMeshGenerator();
        if (meshGenerator != null) {
            java.util.List<java.util.concurrent.Future<com.ksptool.mycraft.world.MeshGenerationResult>> futures = meshGenerator.getPendingFutures();
            java.util.List<java.util.concurrent.Future<com.ksptool.mycraft.world.MeshGenerationResult>> completedFutures = new java.util.ArrayList<>();
            for (java.util.concurrent.Future<com.ksptool.mycraft.world.MeshGenerationResult> future : futures) {
                if (future.isDone()) {
                    try {
                        com.ksptool.mycraft.world.MeshGenerationResult result = future.get();
                        if (result != null) {
                            result.chunk.uploadToGPU(result);
                        }
                    } catch (Exception e) {
                        System.err.println("Error uploading mesh to GPU: " + e.getMessage());
                        e.printStackTrace();
                    }
                    completedFutures.add(future);
                }
            }
            futures.removeAll(completedFutures);
        }
        long meshUploadTime = System.nanoTime() - meshUploadStart;
        
        long totalUpdateTime = System.nanoTime() - updateStartTime;
        
        if (totalUpdateTime > 16_666_666) {
            System.out.println("Update took " + (totalUpdateTime / 1_000_000) + "ms (Entities: " + (entityUpdateTime / 1_000_000) + "ms, Player: " + (playerUpdateTime / 1_000_000) + "ms, World: " + (worldUpdateTime / 1_000_000) + "ms, MeshUpload: " + (meshUploadTime / 1_000_000) + "ms)");
        }
    }

    private void render() {
        long renderStartTime = System.nanoTime();
        
        if (window.isResized()) {
            renderer.resize(window.getWidth(), window.getHeight());
        }

        renderer.clear();
        renderer.render(world, player, window.getWidth(), window.getHeight());
        window.update();
        
        long renderTime = System.nanoTime() - renderStartTime;
        if (renderTime > 16_666_666) {
            System.out.println("Render took " + (renderTime / 1_000_000) + "ms");
        }
    }

    private void cleanup() {
        renderer.cleanup();
        world.cleanup();
        window.cleanup();
    }
}


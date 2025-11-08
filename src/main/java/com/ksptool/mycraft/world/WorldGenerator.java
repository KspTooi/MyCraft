package com.ksptool.mycraft.world;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorldGenerator extends Thread {
    private final BlockingQueue<ChunkGenerationTask> generationQueue;
    private final BlockingQueue<ChunkGenerationTask> meshQueue;
    private final World world;
    private volatile boolean running = true;

    public WorldGenerator(World world, BlockingQueue<ChunkGenerationTask> generationQueue, BlockingQueue<ChunkGenerationTask> meshQueue) {
        this.world = world;
        this.generationQueue = generationQueue;
        this.meshQueue = meshQueue;
        this.setDaemon(true);
        this.setName("WorldGenerator");
    }

    @Override
    public void run() {
        System.out.println("WorldGenerator thread started");
        while (running) {
            try {
                ChunkGenerationTask task = generationQueue.take();
                if (task == null) {
                    continue;
                }

                long startTime = System.nanoTime();
                Chunk chunk = new Chunk(task.getChunkX(), task.getChunkZ());
                world.generateChunkData(chunk);
                task.setChunk(chunk);
                task.setDataGenerated(true);
                
                long elapsed = System.nanoTime() - startTime;
                System.out.println("Generated chunk [" + task.getChunkX() + "," + task.getChunkZ() + "] data in " + (elapsed / 1_000_000) + "ms");
                
                meshQueue.offer(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in WorldGenerator: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("WorldGenerator thread stopped");
    }

    public void stopGenerator() {
        running = false;
        this.interrupt();
    }
}


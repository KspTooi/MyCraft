package com.ksptool.mycraft.world;

import java.util.List;
import java.util.concurrent.*;

/**
 * 区块网格异步生成器类，使用线程池异步生成区块网格数据
 */
public class ChunkMeshGenerator {
    private final ExecutorService executor;
    private final List<Future<MeshGenerationResult>> pendingFutures = new CopyOnWriteArrayList<>();
    private final World world;

    public ChunkMeshGenerator(World world) {
        this.world = world;
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public void submitMeshTask(Chunk chunk) {
        Callable<MeshGenerationResult> task = () -> chunk.calculateMeshData(world);
        Future<MeshGenerationResult> future = executor.submit(task);
        pendingFutures.add(future);
    }

    public List<Future<MeshGenerationResult>> getPendingFutures() {
        return pendingFutures;
    }

    public void shutdown() {
        executor.shutdown();
    }
}


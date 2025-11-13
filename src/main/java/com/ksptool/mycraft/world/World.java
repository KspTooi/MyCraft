package com.ksptool.mycraft.world;

import com.ksptool.mycraft.entity.BoundingBox;
import com.ksptool.mycraft.entity.Entity;
import com.ksptool.mycraft.world.save.RegionManager;
import com.ksptool.mycraft.world.gen.GenerationContext;
import com.ksptool.mycraft.world.gen.TerrainPipeline;
import com.ksptool.mycraft.world.gen.layers.BaseDensityLayer;
import com.ksptool.mycraft.world.gen.layers.FeatureLayer;
import com.ksptool.mycraft.world.gen.layers.SurfaceLayer;
import com.ksptool.mycraft.world.gen.layers.WaterLayer;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.List;

/**
 * 世界管理类，负责世界模拟逻辑的协调
 */
@Getter
public class World {
    private static final int TICKS_PER_DAY = 24000;
    
    private final WorldTemplate template;
    private double timeAccumulator = 0.0;
    
    private final ChunkManager chunkManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    
    @Setter
    private long gameTime = 0;
    
    @Setter
    private String worldName;
    @Setter
    private long seed;
    private RegionManager regionManager;
    private RegionManager entityRegionManager;
    private String saveName;
    
    private NoiseGenerator noiseGenerator;
    private TerrainPipeline terrainPipeline;
    private GenerationContext generationContext;

    public World(WorldTemplate template) {
        this.template = template;
        this.chunkManager = new ChunkManager(this);
        this.entityManager = new EntityManager(this);
        this.collisionManager = new CollisionManager(this);
        this.seed = System.currentTimeMillis();
    }
    
    public void setSaveName(String saveName) {
        this.saveName = saveName;
        this.chunkManager.setSaveName(saveName);
        this.entityManager.setSaveName(saveName);
    }
    
    public void setRegionManager(RegionManager regionManager) {
        this.regionManager = regionManager;
        this.chunkManager.setRegionManager(regionManager);
    }
    
    public void setEntityRegionManager(RegionManager entityRegionManager) {
        this.entityRegionManager = entityRegionManager;
        this.entityManager.setEntityRegionManager(entityRegionManager);
    }

    public void init() {
        chunkManager.init();
        
        noiseGenerator = new NoiseGenerator(seed);
        terrainPipeline = new TerrainPipeline();
        terrainPipeline.addLayer(new BaseDensityLayer());
        terrainPipeline.addLayer(new WaterLayer());
        terrainPipeline.addLayer(new SurfaceLayer());
        terrainPipeline.addLayer(new FeatureLayer());
        generationContext = new GenerationContext(noiseGenerator, this, seed);
    }

    /**
     * 新的update方法，由Game.java的主循环调用
     * 使用累加器模式，根据模板配置的TPS进行逻辑更新
     */
    public void update(float deltaTime, Vector3f playerPosition, Runnable playerTickCallback) {
        timeAccumulator += deltaTime;
        double tickTime = 1.0 / template.getTicksPerSecond();
        
        while (timeAccumulator >= tickTime) {
            tick(playerPosition, playerTickCallback);
            timeAccumulator -= tickTime;
        }
    }
    
    /**
     * 单次逻辑更新（tick）
     */
    private void tick(Vector3f playerPosition, Runnable playerTickCallback) {
        for (Entity entity : getEntities()) {
            entity.getPreviousPosition().set(entity.getPosition());
            if (entity instanceof com.ksptool.mycraft.entity.Player) {
                com.ksptool.mycraft.entity.Player player = (com.ksptool.mycraft.entity.Player) entity;
                player.getCamera().setPreviousYaw(player.getCamera().getYaw());
                player.getCamera().setPreviousPitch(player.getCamera().getPitch());
            }
        }
        
        gameTime++;
        chunkManager.update(playerPosition);
        
        float tickDelta = 1.0f / template.getTicksPerSecond();
        
        if (playerTickCallback != null) {
            playerTickCallback.run();
        }
        
        for (Entity entity : getEntities()) {
            if (entity instanceof com.ksptool.mycraft.entity.Player) {
                continue;
            }
            entity.update(tickDelta);
        }
    }
    
    /**
     * 获取部分刻（Partial Tick），用于渲染插值
     * @return 0.0 到 1.0 之间的值，表示距离下一次tick的进度
     */
    public float getPartialTick() {
        if (template.getTicksPerSecond() == 0) {
            return 0.0f;
        }
        double tickTime = 1.0 / template.getTicksPerSecond();
        return (float) (timeAccumulator / tickTime);
    }

    public void generateChunkData(Chunk chunk) {
        if (terrainPipeline == null || generationContext == null) {
            return;
        }
        terrainPipeline.execute(chunk, generationContext);
    }
    
    public void generateChunkSynchronously(int chunkX, int chunkZ) {
        chunkManager.generateChunkSynchronously(chunkX, chunkZ);
    }
    
    public int getHeightAt(int worldX, int worldZ) {
        if (noiseGenerator == null) {
            return 64;
        }
        double noiseValue = noiseGenerator.noise(worldX * 0.05 + seed, worldZ * 0.05 + seed);
        return (int) (64 + noiseValue * 20);
    }
    
    public int getChunkCount() {
        return chunkManager.getChunkCount();
    }

    public int getBlockState(int x, int y, int z) {
        return chunkManager.getBlockState(x, y, z);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunkManager.getChunk(chunkX, chunkZ);
    }
    
    public void setBlockState(int x, int y, int z, int stateId) {
        chunkManager.setBlockState(x, y, z, stateId);
    }

    public boolean canMoveTo(Vector3f position, float height) {
        return collisionManager.canMoveTo(position, height);
    }

    public boolean canMoveTo(BoundingBox box) {
        return collisionManager.canMoveTo(box);
    }

    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }

    public void removeEntity(Entity entity) {
        entityManager.removeEntity(entity);
    }

    public List<Entity> getEntities() {
        return entityManager.getEntities();
    }

    public void cleanup() {
        chunkManager.cleanup();
    }

    public ChunkMeshGenerator getChunkMeshGenerator() {
        return chunkManager.getChunkMeshGenerator();
    }

    public void saveAllDirtyData() {
        chunkManager.saveAllDirtyChunks();
        entityManager.saveAllDirtyEntities();
    }
    
    public void saveToFile(String chunksDirPath) {
        saveAllDirtyData();
    }

    public void loadFromFile(String chunksDirPath) {
    }
    
    public float getTimeOfDay() {
        return (float) (gameTime % TICKS_PER_DAY) / TICKS_PER_DAY;
    }
}

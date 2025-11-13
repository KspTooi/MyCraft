package com.ksptool.mycraft.world;

import com.ksptool.mycraft.entity.Entity;
import com.ksptool.mycraft.world.save.EntitySerializer;
import com.ksptool.mycraft.world.save.RegionFile;
import com.ksptool.mycraft.world.save.RegionManager;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体管理器，负责实体的生命周期管理
 */
public class EntityManager {
    private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
    
    private final World world;
    private final List<Entity> entities;
    private RegionManager entityRegionManager;
    private String saveName;
    
    public EntityManager(World world) {
        this.world = world;
        this.entities = new ArrayList<>();
    }
    
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }
    
    public void setEntityRegionManager(RegionManager entityRegionManager) {
        this.entityRegionManager = entityRegionManager;
    }
    
    public RegionManager getEntityRegionManager() {
        return entityRegionManager;
    }
    
    public void addEntity(Entity entity) {
        entities.add(entity);
        entity.getPreviousPosition().set(entity.getPosition());
        if (entity instanceof com.ksptool.mycraft.entity.Player) {
            com.ksptool.mycraft.entity.Player player = (com.ksptool.mycraft.entity.Player) entity;
            player.getCamera().setPreviousYaw(player.getCamera().getYaw());
            player.getCamera().setPreviousPitch(player.getCamera().getPitch());
        }
        entity.markDirty(true);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }
    
    public void loadEntitiesForChunk(int chunkX, int chunkZ) {
        if (entityRegionManager == null) {
            return;
        }
        
        try {
            int regionX = RegionManager.getRegionX(chunkX);
            int regionZ = RegionManager.getRegionZ(chunkZ);
            int localX = RegionManager.getLocalChunkX(chunkX);
            int localZ = RegionManager.getLocalChunkZ(chunkZ);
            
            RegionFile entityRegionFile = entityRegionManager.getRegionFile(regionX, regionZ);
            entityRegionFile.open();
            
            byte[] compressedData = entityRegionFile.readChunk(localX, localZ);
            if (compressedData == null) {
                return;
            }
            
            List<Entity> loadedEntities = EntitySerializer.deserialize(compressedData, world);
            if (loadedEntities != null && !loadedEntities.isEmpty()) {
                logger.debug("从区块 [{},{}] 加载了 {} 个实体", chunkX, chunkZ, loadedEntities.size());
                for (Entity entity : loadedEntities) {
                    if (!entities.contains(entity)) {
                        entity.getPreviousPosition().set(entity.getPosition());
                        if (entity instanceof com.ksptool.mycraft.entity.Player) {
                            com.ksptool.mycraft.entity.Player player = (com.ksptool.mycraft.entity.Player) entity;
                            player.getCamera().setPreviousYaw(player.getCamera().getYaw());
                            player.getCamera().setPreviousPitch(player.getCamera().getPitch());
                        }
                        entities.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("加载实体失败 [{},{}]", chunkX, chunkZ, e);
        }
    }
    
    public void saveEntitiesForChunk(int chunkX, int chunkZ) {
        if (entityRegionManager == null || StringUtils.isBlank(saveName)) {
            return;
        }
        
        try {
            List<Entity> chunkEntities = new ArrayList<>();
            float chunkMinX = chunkX * Chunk.CHUNK_SIZE;
            float chunkMaxX = chunkMinX + Chunk.CHUNK_SIZE;
            float chunkMinZ = chunkZ * Chunk.CHUNK_SIZE;
            float chunkMaxZ = chunkMinZ + Chunk.CHUNK_SIZE;
            
            for (Entity entity : entities) {
                Vector3f pos = entity.getPosition();
                if (pos.x >= chunkMinX && pos.x < chunkMaxX && 
                    pos.z >= chunkMinZ && pos.z < chunkMaxZ) {
                    chunkEntities.add(entity);
                }
            }
            
            if (chunkEntities.isEmpty()) {
                return;
            }
            
            byte[] compressedData = EntitySerializer.serialize(chunkEntities);
            
            int regionX = RegionManager.getRegionX(chunkX);
            int regionZ = RegionManager.getRegionZ(chunkZ);
            int localX = RegionManager.getLocalChunkX(chunkX);
            int localZ = RegionManager.getLocalChunkZ(chunkZ);
            
            RegionFile entityRegionFile = entityRegionManager.getRegionFile(regionX, regionZ);
            entityRegionFile.open();
            entityRegionFile.writeChunk(localX, localZ, compressedData);
            logger.debug("成功保存区块 [{},{}] 的 {} 个实体", chunkX, chunkZ, chunkEntities.size());
        } catch (Exception e) {
            logger.error("保存实体失败 [{},{}]", chunkX, chunkZ, e);
        }
    }
    
    public void saveAllDirtyEntities() {
        if (entityRegionManager == null || StringUtils.isBlank(saveName)) {
            return;
        }
        
        try {
            int dirtyEntityChunkCount = 0;
            
            for (Entity entity : entities) {
                Vector3f pos = entity.getPosition();
                int entityChunkX = (int) Math.floor(pos.x / Chunk.CHUNK_SIZE);
                int entityChunkZ = (int) Math.floor(pos.z / Chunk.CHUNK_SIZE);
                
                Chunk chunk = world.getChunkManager().getChunk(entityChunkX, entityChunkZ);
                if (chunk != null && chunk.areEntitiesDirty()) {
                    logger.debug("保存脏实体区块 [{},{}]", entityChunkX, entityChunkZ);
                    saveEntitiesForChunk(entityChunkX, entityChunkZ);
                    chunk.markEntitiesDirty(false);
                    dirtyEntityChunkCount++;
                    
                    entity.markDirty(false);
                }
            }
            
            if (dirtyEntityChunkCount == 0) {
                logger.debug("没有需要保存的脏实体");
                return;
            }
            logger.info("保存完成: 脏实体区块数={}", dirtyEntityChunkCount);
        } catch (Exception e) {
            logger.error("保存实体失败", e);
        }
    }
    
    public void update(float delta) {
        for (Entity entity : entities) {
            entity.update(delta);
        }
    }
}

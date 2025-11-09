package com.ksptool.mycraft.world;

import com.ksptool.mycraft.entity.Player;
import com.ksptool.mycraft.world.save.RegionManager;
import com.ksptool.mycraft.world.save.SaveManager;
import com.ksptool.mycraft.world.save.WorldIndex;
import com.ksptool.mycraft.world.save.WorldMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 世界保存/加载管理类，负责世界的保存、加载和删除操作
 */
public class WorldManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldManager.class);
    private static WorldManager instance;

    private WorldManager() {
    }

    public static WorldManager getInstance() {
        if (instance == null) {
            instance = new WorldManager();
        }
        return instance;
    }

    public java.util.List<String> getWorldList(String saveName) {
        if (StringUtils.isBlank(saveName)) {
            return new java.util.ArrayList<>();
        }

        WorldIndex index = SaveManager.getInstance().loadWorldIndex(saveName);
        if (index == null || index.worlds == null) {
            return new java.util.ArrayList<>();
        }

        java.util.List<String> worldNames = new java.util.ArrayList<>();
        for (WorldMetadata metadata : index.worlds) {
            if (metadata != null && StringUtils.isNotBlank(metadata.name)) {
                worldNames.add(metadata.name);
            }
        }
        return worldNames;
    }

    public boolean worldExists(String saveName, String worldName) {
        if (StringUtils.isBlank(saveName) || StringUtils.isBlank(worldName)) {
            return false;
        }

        WorldIndex index = SaveManager.getInstance().loadWorldIndex(saveName);
        if (index == null || index.worlds == null) {
            return false;
        }

        for (WorldMetadata metadata : index.worlds) {
            if (metadata != null && worldName.equals(metadata.name)) {
                return true;
            }
        }
        return false;
    }

    public void saveWorld(World world, Player player, String saveName, String worldName) {
        if (StringUtils.isBlank(saveName) || StringUtils.isBlank(worldName) || world == null) {
            logger.warn("保存世界失败: 参数无效 saveName={}, worldName={}, world={}", saveName, worldName, world != null);
            return;
        }

        logger.info("开始保存世界: saveName={}, worldName={}", saveName, worldName);

        SaveManager saveManager = SaveManager.getInstance();
        WorldIndex index = saveManager.loadWorldIndex(saveName);
        if (index == null) {
            index = new WorldIndex();
        }

        WorldMetadata metadata = null;
        for (WorldMetadata m : index.worlds) {
            if (m != null && worldName.equals(m.name)) {
                metadata = m;
                break;
            }
        }

        if (metadata == null) {
            metadata = new WorldMetadata();
            metadata.name = worldName;
            index.worlds.add(metadata);
            logger.debug("创建新的世界元数据: {}", worldName);
        }

        metadata.seed = world.getSeed();
        metadata.worldTime = world.getGameTime();

        saveManager.saveWorldIndex(saveName, index);
        saveManager.savePalette(saveName, GlobalPalette.getInstance());
        logger.debug("已保存世界索引和调色板");

        if (world.getRegionManager() == null) {
            File chunksDir = saveManager.getWorldChunkDir(saveName, worldName);
            if (chunksDir != null) {
                RegionManager regionManager = new RegionManager(chunksDir, ".sca", "SCAF");
                world.setRegionManager(regionManager);
                logger.debug("初始化区块区域管理器");
            }
        }
        
        if (world.getEntityRegionManager() == null) {
            File entityDir = saveManager.getWorldEntityDir(saveName, worldName);
            if (entityDir != null) {
                RegionManager entityRegionManager = new RegionManager(entityDir, ".sce", "SCEF");
                world.setEntityRegionManager(entityRegionManager);
                logger.debug("初始化实体区域管理器");
            }
        }
        
        world.setSaveName(saveName);
        File chunksDir = saveManager.getWorldChunkDir(saveName, worldName);
        if (chunksDir != null) {
            int chunkCount = world.getChunkCount();
            logger.info("开始保存区块数据，当前已加载区块数: {}", chunkCount);
            world.saveToFile(chunksDir.getAbsolutePath());
            logger.info("区块数据保存完成");
        }
        
        if (player != null) {
            logger.debug("保存玩家数据: UUID={}", player.getUniqueId());
            saveManager.savePlayer(saveName, player.getUniqueId(), player);
        }
        
        logger.info("世界保存完成: saveName={}, worldName={}", saveName, worldName);
    }
    
    public void saveWorld(World world, String saveName, String worldName) {
        saveWorld(world, null, saveName, worldName);
    }

    public World loadWorld(String saveName, String worldName) {
        if (StringUtils.isBlank(saveName) || StringUtils.isBlank(worldName)) {
            logger.warn("加载世界失败: 参数无效 saveName={}, worldName={}", saveName, worldName);
            return null;
        }

        logger.info("开始加载世界: saveName={}, worldName={}", saveName, worldName);

        SaveManager saveManager = SaveManager.getInstance();
        WorldIndex index = saveManager.loadWorldIndex(saveName);
        if (index == null || index.worlds == null) {
            logger.error("加载世界失败: 无法读取世界索引 saveName={}", saveName);
            return null;
        }

        WorldMetadata metadata = null;
        for (WorldMetadata m : index.worlds) {
            if (m != null && worldName.equals(m.name)) {
                metadata = m;
                break;
            }
        }

        if (metadata == null) {
            logger.error("加载世界失败: 世界不存在 saveName={}, worldName={}", saveName, worldName);
            return null;
        }

        logger.debug("找到世界元数据: seed={}, worldTime={}", metadata.seed, metadata.worldTime);

        GlobalPalette palette = GlobalPalette.getInstance();
        if (!palette.isBaked()) {
            if (!saveManager.loadPalette(saveName, palette)) {
                logger.debug("调色板文件不存在，使用默认调色板");
                palette.bake();
            } else {
                logger.debug("已加载调色板");
            }
        }
        
        World world = new World();
        world.setWorldName(worldName);
        world.setSeed(metadata.seed);
        world.setGameTime(metadata.worldTime);
        world.setSaveName(saveName);
        
        File chunksDir = saveManager.getWorldChunkDir(saveName, worldName);
        if (chunksDir != null) {
            RegionManager regionManager = new RegionManager(chunksDir, ".sca", "SCAF");
            world.setRegionManager(regionManager);
            logger.debug("已设置区块区域管理器");
        }
        
        File entityDir = saveManager.getWorldEntityDir(saveName, worldName);
        if (entityDir != null) {
            RegionManager entityRegionManager = new RegionManager(entityDir, ".sce", "SCEF");
            world.setEntityRegionManager(entityRegionManager);
            logger.debug("已设置实体区域管理器");
        }
        
        world.init();
        logger.info("世界加载完成: saveName={}, worldName={}", saveName, worldName);
        return world;
    }

    public void deleteWorld(String saveName, String worldName) {
        if (StringUtils.isBlank(saveName) || StringUtils.isBlank(worldName)) {
            return;
        }

        SaveManager saveManager = SaveManager.getInstance();
        WorldIndex index = saveManager.loadWorldIndex(saveName);
        if (index == null || index.worlds == null) {
            return;
        }

        index.worlds.removeIf(m -> m != null && worldName.equals(m.name));
        saveManager.saveWorldIndex(saveName, index);

        File chunksDir = saveManager.getWorldChunkDir(saveName, worldName);
        if (chunksDir != null && chunksDir.exists()) {
            deleteDirectory(chunksDir);
        }

        File entityDir = saveManager.getWorldEntityDir(saveName, worldName);
        if (entityDir != null && entityDir.exists()) {
            deleteDirectory(entityDir);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}


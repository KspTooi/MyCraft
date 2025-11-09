package com.ksptool.mycraft.world;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 世界保存/加载管理类，负责世界的保存、加载和删除操作
 */
public class WorldManager {
    private static final String SAVES_DIR = "saves";
    private static WorldManager instance;

    private WorldManager() {
        File savesDir = new File(SAVES_DIR);
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }
    }

    public static WorldManager getInstance() {
        if (instance == null) {
            instance = new WorldManager();
        }
        return instance;
    }

    public List<String> getWorldList() {
        List<String> worlds = new ArrayList<>();
        File savesDir = new File(SAVES_DIR);
        if (!savesDir.exists()) {
            return worlds;
        }

        File[] files = savesDir.listFiles();
        if (files == null) {
            return worlds;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String worldName = file.getName();
                File levelFile = new File(file, "level.dat");
                if (levelFile.exists()) {
                    worlds.add(worldName);
                }
            }
        }

        return worlds;
    }

    public boolean worldExists(String worldName) {
        if (StringUtils.isBlank(worldName)) {
            return false;
        }
        File worldDir = new File(SAVES_DIR, worldName);
        if (!worldDir.exists()) {
            return false;
        }
        File levelFile = new File(worldDir, "level.dat");
        return levelFile.exists();
    }

    public void saveWorld(World world, String worldName) {
        if (StringUtils.isBlank(worldName)) {
            return;
        }

        File worldDir = new File(SAVES_DIR, worldName);
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        }

        File chunksDir = new File(worldDir, "chunks");
        if (!chunksDir.exists()) {
            chunksDir.mkdirs();
        }

        try {
            saveLevelData(world, worldName, worldDir);
            world.saveToFile(chunksDir.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("保存世界失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public World loadWorld(String worldName) {
        if (StringUtils.isBlank(worldName)) {
            return null;
        }

        File worldDir = new File(SAVES_DIR, worldName);
        if (!worldDir.exists()) {
            return null;
        }

        File levelFile = new File(worldDir, "level.dat");
        if (!levelFile.exists()) {
            return null;
        }

        try {
            WorldData data = loadLevelData(levelFile);
            World world = new World();
            world.setWorldName(worldName);
            world.setSeed(data.seed);
            
            File chunksDir = new File(worldDir, "chunks");
            if (chunksDir.exists()) {
                world.loadFromFile(chunksDir.getAbsolutePath());
            }
            
            world.init();
            return world;
        } catch (IOException e) {
            System.err.println("加载世界失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void deleteWorld(String worldName) {
        if (StringUtils.isBlank(worldName)) {
            return;
        }

        File worldDir = new File(SAVES_DIR, worldName);
        if (!worldDir.exists()) {
            return;
        }

        deleteDirectory(worldDir);
    }

    private void saveLevelData(World world, String worldName, File worldDir) throws IOException {
        File levelFile = new File(worldDir, "level.dat");
        try (FileWriter writer = new FileWriter(levelFile)) {
            writer.write("worldName=" + worldName + "\n");
            writer.write("seed=" + world.getSeed() + "\n");
            writer.write("gameTime=" + world.getGameTime() + "\n");
        }
    }

    private WorldData loadLevelData(File levelFile) throws IOException {
        WorldData data = new WorldData();
        List<String> lines = Files.readAllLines(levelFile.toPath());
        for (String line : lines) {
            if (line.startsWith("seed=")) {
                try {
                    data.seed = Long.parseLong(line.substring(5));
                } catch (NumberFormatException e) {
                    data.seed = System.currentTimeMillis();
                }
            }
        }
        if (data.seed == 0) {
            data.seed = System.currentTimeMillis();
        }
        return data;
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

    private static class WorldData {
        long seed = 0;
    }
}


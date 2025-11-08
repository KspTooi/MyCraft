# 注意 这是一个临时项目，不要用于生产环境，只能用于学习参考。 该项目的全部代码来源于生成式AI，且未经任何测试与Review!


# MyCraft

一个使用 Java、LWJGL 3 和 Maven 开发的类似 Minecraft 的 3D 沙盒游戏。

## 功能特性

- ✅ 3D 渲染引擎（使用 OpenGL）
- ✅ 无限世界生成（程序化地形生成）
- ✅ 区块系统（16x256x16 区块，动态加载/卸载）
- ✅ 玩家移动和第一人称相机控制
- ✅ 方块破坏和放置
- ✅ 物品系统和快捷栏
- ✅ 昼夜循环光照系统
- ✅ 碰撞检测
- ✨ **现代化的方块系统**（"扁平化"架构，支持方块状态和属性）
- ✨ **可扩展的实体系统**（`Entity` -> `LivingEntity` -> `Player` 继承结构）

## 控制说明

- **W/A/S/D**: 移动
- **空格**: 跳跃
- **鼠标移动**: 视角转动
- **鼠标左键**: 破坏方块
- **鼠标右键**: 放置方块
- **鼠标滚轮**: 切换快捷栏物品
- **1-9**: 选择快捷栏物品
- **ESC**: 退出游戏

## 构建和运行

### 前置要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 构建项目

```bash
mvn clean compile
```

### 运行项目

```bash
mvn exec:java -Dexec.mainClass="com.ksptool.mycraft.Launcher"
```

或者打包成可执行 JAR：

```bash
mvn clean package
java -jar target/MyCraft-1.0-SNAPSHOT.jar
```

## 项目结构

```
src/main/java/com/ksptool/mycraft/
├── core/           # 核心系统（窗口、输入、游戏循环）
├── entity/         # 实体系统 (Entity, LivingEntity, Player)
├── item/           # 物品系统
├── rendering/      # 渲染系统（着色器、网格、渲染器）
└── world/          # 世界系统（区块、方块、地形生成）
    ├── blocks/     # 具体的方块实现
    └── properties/ # 方块属性系统
```

## 技术栈

- **LWJGL 3**: 图形库（OpenGL、GLFW）
- **JOML**: 3D 数学库（向量、矩阵）
- **Maven**: 构建工具

## 开发计划

已完成的功能：
- [x] 基础渲染引擎
- [x] 世界生成和区块系统
- [x] 玩家移动和交互
- [x] 物品系统
- [x] 昼夜循环
- [x] **重构: 方块系统 "扁平化"**
- [x] **重构: 实体系统 (Entity Component System)**

未来可能添加的功能：
- [ ] 僵尸AI (基于新的实体系统)
- [ ] 更复杂的地形生成（生物群系、洞穴）
- [ ] 音效系统
- [ ] 保存/加载世界
- [ ] 多人游戏支持


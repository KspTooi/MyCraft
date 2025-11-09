package com.ksptool.mycraft.entity;

import com.ksptool.mycraft.world.World;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.UUID;

/**
 * 实体基类，定义实体的基本属性和行为
 */
public abstract class Entity {
    protected final World world;
    protected final UUID uniqueId;
    protected final Vector3f position;
    protected final Vector3f velocity;
    protected boolean onGround;
    protected BoundingBox boundingBox;
    protected boolean isDead;
    protected boolean isDirty = false;

    public Entity(World world) {
        this(world, UUID.randomUUID());
    }

    public Entity(World world, UUID uniqueId) {
        this.world = Objects.requireNonNull(world);
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID();
        this.position = new Vector3f();
        this.velocity = new Vector3f();
        this.onGround = false;
        this.isDead = false;
    }

    public abstract void update(float delta);

    public World getWorld() {
        return world;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        this.isDead = dead;
    }

    public void markDirty(boolean isDirty) {
        this.isDirty = isDirty;
        if (isDirty && world != null) {
            int chunkX = (int) Math.floor(position.x / com.ksptool.mycraft.world.Chunk.CHUNK_SIZE);
            int chunkZ = (int) Math.floor(position.z / com.ksptool.mycraft.world.Chunk.CHUNK_SIZE);
            com.ksptool.mycraft.world.Chunk chunk = world.getChunk(chunkX, chunkZ);
            if (chunk != null) {
                chunk.markEntitiesDirty(true);
            }
        }
    }

    public boolean isDirty() {
        return isDirty;
    }
}


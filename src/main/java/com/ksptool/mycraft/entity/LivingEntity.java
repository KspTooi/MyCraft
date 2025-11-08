package com.ksptool.mycraft.entity;

import com.ksptool.mycraft.world.World;
import org.joml.Vector3f;

public abstract class LivingEntity extends Entity {
    protected static final float GRAVITY = -20.0f;
    protected static final float JUMP_VELOCITY = 8.0f;
    
    protected float health = 20.0f;
    protected float eyeHeight = 1.6f;

    public LivingEntity(World world) {
        super(world);
    }

    @Override
    public void update(float delta) {
        handlePhysics(delta);
    }

    protected void handlePhysics(float delta) {
        if (delta <= 0) {
            return;
        }
        
        float clampedDelta = Math.min(delta, 0.1f);
        
        velocity.y += GRAVITY * clampedDelta;
        
        Vector3f movement = new Vector3f(velocity);
        movement.mul(clampedDelta);
        
        Vector3f newPosition = new Vector3f(position);
        
        if (boundingBox == null) {
            boundingBox = new BoundingBox(position, 0.6f, 1.8f);
        }
        
        newPosition.x += movement.x;
        BoundingBox testBox = boundingBox.offset(new Vector3f(movement.x, 0, 0));
        if (!world.canMoveTo(testBox)) {
            newPosition.x = position.x;
            velocity.x = 0;
        }
        
        newPosition.z += movement.z;
        testBox = boundingBox.offset(new Vector3f(0, 0, movement.z));
        if (!world.canMoveTo(testBox)) {
            newPosition.z = position.z;
            velocity.z = 0;
        }
        
        newPosition.y += movement.y;
        testBox = boundingBox.offset(new Vector3f(0, movement.y, 0));
        if (!world.canMoveTo(testBox)) {
            if (movement.y < 0) {
                onGround = true;
                velocity.y = 0;
            } else {
                velocity.y = 0;
            }
            newPosition.y = position.y;
        } else {
            onGround = false;
        }
        
        position.set(newPosition);
        if (boundingBox != null) {
            boundingBox.update(position);
        } else {
            boundingBox = new BoundingBox(position, 0.6f, 1.8f);
        }
        
        velocity.x *= 0.8f;
        velocity.z *= 0.8f;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getEyeHeight() {
        return eyeHeight;
    }

    public void setEyeHeight(float eyeHeight) {
        this.eyeHeight = eyeHeight;
    }
}


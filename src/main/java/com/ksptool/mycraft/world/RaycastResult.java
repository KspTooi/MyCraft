package com.ksptool.mycraft.world;

import org.joml.Vector3i;

public class RaycastResult {
    private Vector3i blockPosition;
    private Vector3i faceNormal;
    private boolean hit;

    public RaycastResult() {
        this.blockPosition = new Vector3i();
        this.faceNormal = new Vector3i();
        this.hit = false;
    }

    public Vector3i getBlockPosition() {
        return blockPosition;
    }

    public void setBlockPosition(Vector3i blockPosition) {
        this.blockPosition = blockPosition;
    }

    public Vector3i getFaceNormal() {
        return faceNormal;
    }

    public void setFaceNormal(Vector3i faceNormal) {
        this.faceNormal = faceNormal;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }
}


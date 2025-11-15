package com.ksptool.mycraft.entity;

import com.ksptool.mycraft.client.Input;
import com.ksptool.mycraft.item.Inventory;
import com.ksptool.mycraft.sharedcore.item.Item;
import com.ksptool.mycraft.sharedcore.item.ItemStack;
import com.ksptool.mycraft.sharedcore.BoundingBox;
import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.sharedcore.BlockType;
import com.ksptool.mycraft.world.GlobalPalette;
import com.ksptool.mycraft.world.Registry;
import com.ksptool.mycraft.world.Raycast;
import com.ksptool.mycraft.world.RaycastResult;
import com.ksptool.mycraft.world.World;
import com.ksptool.mycraft.world.save.ItemStackData;
import com.ksptool.mycraft.world.save.PlayerIndex;
import lombok.Getter;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 * 玩家实体类，处理玩家移动、相机控制、方块放置和破坏
 */
@Getter
public class Player extends LivingEntity {
    //相机
    private final Camera camera;

    //背包
    private final Inventory inventory;

    //鼠标灵敏度
    private float mouseSensitivity = 0.1f;
    
    //地面加速度
    private static final float GROUND_ACCELERATION = 40F;
    
    //空中加速度
    private static final float AIR_ACCELERATION = 5F;
    
    //最大移动速度
    private static final float MAX_SPEED = 40F;

    private static final java.util.Map<Integer, Integer> KEY_TO_SLOT_MAP = java.util.Map.of(
        GLFW.GLFW_KEY_1, 0,
        GLFW.GLFW_KEY_2, 1,
        GLFW.GLFW_KEY_3, 2,
        GLFW.GLFW_KEY_4, 3,
        GLFW.GLFW_KEY_5, 4,
        GLFW.GLFW_KEY_6, 5,
        GLFW.GLFW_KEY_7, 6,
        GLFW.GLFW_KEY_8, 7,
        GLFW.GLFW_KEY_9, 8
    );

    public Player(World world) {
        super(world);
        this.camera = new Camera();
        this.inventory = new Inventory();
        this.eyeHeight = 1.6f;
        this.boundingBox = new BoundingBox(position, 0.6f, 1.8f);
    }

    public Player(World world, java.util.UUID uniqueId) {
        super(world, uniqueId);
        this.camera = new Camera();
        this.inventory = new Inventory();
        this.eyeHeight = 1.6f;
        this.boundingBox = new BoundingBox(position, 0.6f, 1.8f);
    }

    public void initializeCamera() {
        camera.setYaw(0);
        camera.setPitch(0);
        updateCamera();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void update(Input input, float delta) {
        handleKeyboard(input, delta);
        super.update(delta);
        updateCamera();
    }

    public void handleMouseInput(Input input) {
        if (!input.isMouseLocked()) {
            return;
        }

        Vector2d mouseDelta = input.getMouseDelta();
        if (mouseDelta.x != 0 || mouseDelta.y != 0) {
            float deltaYaw = (float) mouseDelta.x * mouseSensitivity;
            float deltaPitch = (float) mouseDelta.y * mouseSensitivity;

            camera.setYaw(camera.getYaw() + deltaYaw);
            camera.setPitch(camera.getPitch() + deltaPitch);
            markDirty(true);
        }
    }

    private void handleKeyboard(Input input, float delta) {
        Vector3f moveDirection = new Vector3f();
        float yawRad = (float) Math.toRadians(camera.getYaw());

        if (input.isKeyPressed(GLFW.GLFW_KEY_W)) {
            moveDirection.x += Math.sin(yawRad);
            moveDirection.z -= Math.cos(yawRad);
        }
        if (input.isKeyPressed(GLFW.GLFW_KEY_S)) {
            moveDirection.x -= Math.sin(yawRad);
            moveDirection.z += Math.cos(yawRad);
        }
        if (input.isKeyPressed(GLFW.GLFW_KEY_A)) {
            moveDirection.x -= Math.cos(yawRad);
            moveDirection.z -= Math.sin(yawRad);
        }
        if (input.isKeyPressed(GLFW.GLFW_KEY_D)) {
            moveDirection.x += Math.cos(yawRad);
            moveDirection.z += Math.sin(yawRad);
        }

        if (moveDirection.length() > 0) {
            moveDirection.normalize();
            
            float acceleration = onGround ? GROUND_ACCELERATION : AIR_ACCELERATION;
            velocity.x += moveDirection.x * acceleration * delta;
            velocity.z += moveDirection.z * acceleration * delta;
            
            Vector2f horizontalVelocity = new Vector2f(velocity.x, velocity.z);
            if (horizontalVelocity.lengthSquared() > MAX_SPEED * MAX_SPEED) {
                horizontalVelocity.normalize().mul(MAX_SPEED);
                velocity.x = horizontalVelocity.x;
                velocity.z = horizontalVelocity.y;
            }
        }

        if (input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && onGround) {
            velocity.y = JUMP_VELOCITY;
            onGround = false;
        }

        double scrollY = input.getScrollY();
        if (scrollY != 0) {
            inventory.scrollSelection((int) -scrollY);
            markDirty(true);
        }

        for (java.util.Map.Entry<Integer, Integer> entry : KEY_TO_SLOT_MAP.entrySet()) {
            if (input.isKeyPressed(entry.getKey())) {
                inventory.setSelectedSlot(entry.getValue());
                markDirty(true);
            }
        }

        if (input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            handleBlockBreak();
        }
        if (input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            handleBlockPlace();
        }
    }

    private void handleBlockBreak() {
        Vector3f eyePosition = new Vector3f(position);
        eyePosition.y += eyeHeight;
        Vector3f direction = getLookDirection();
        RaycastResult result = Raycast.cast(world, eyePosition, direction, 5.0f);
        if (result.isHit()) {
            GlobalPalette palette = GlobalPalette.getInstance();
            Registry registry = Registry.getInstance();
            Block airBlock = registry.get(BlockType.AIR.getNamespacedId());
            int airStateId = palette.getStateId(airBlock.getDefaultState());
            world.setBlockState(result.getBlockPosition().x, result.getBlockPosition().y, result.getBlockPosition().z, airStateId);
        }
    }

    private void handleBlockPlace() {
        ItemStack selectedStack = inventory.getSelectedItem();
        if (selectedStack == null || selectedStack.isEmpty()) {
            return;
        }

        Vector3f eyePosition = new Vector3f(position);
        eyePosition.y += eyeHeight;
        Vector3f direction = getLookDirection();
        RaycastResult result = Raycast.cast(world, eyePosition, direction, 5.0f);
        if (result.isHit()) {
            Vector3i placePos = new Vector3i(result.getBlockPosition()).add(result.getFaceNormal());
            if (world.canMoveTo(new Vector3f(placePos.x, placePos.y, placePos.z), boundingBox.getHeight())) {
                String blockId = selectedStack.getItem().getBlockNamespacedID();
                if (blockId != null) {
                    GlobalPalette palette = GlobalPalette.getInstance();
                    Registry registry = Registry.getInstance();
                    Block block = registry.get(blockId);
                    if (block != null) {
                        int stateId = palette.getStateId(block.getDefaultState());
                        world.setBlockState(placePos.x, placePos.y, placePos.z, stateId);
                        selectedStack.remove(1);
                        markDirty(true);
                    }
                }
            }
        }
    }

    private Vector3f getLookDirection() {
        float yawRad = (float) Math.toRadians(camera.getYaw());
        float pitchRad = (float) Math.toRadians(camera.getPitch());
        return new Vector3f(
                (float) (Math.sin(yawRad) * Math.cos(pitchRad)),
                (float) (-Math.sin(pitchRad)),
                (float) (-Math.cos(yawRad) * Math.cos(pitchRad))
        );
    }

    private void updateCamera() {
        Vector3f eyePosition = new Vector3f(position);
        eyePosition.y += eyeHeight;
        camera.setPosition(eyePosition);
        camera.update();
    }

    public void loadFromPlayerIndex(PlayerIndex playerIndex) {
        if (playerIndex == null) {
            return;
        }

        position.set(playerIndex.posX, playerIndex.posY, playerIndex.posZ);
        camera.setYaw(playerIndex.yaw);
        camera.setPitch(playerIndex.pitch);
        setHealth(playerIndex.health);
        inventory.setSelectedSlot(playerIndex.selectedSlot);

        if (playerIndex.hotbar != null) {
            ItemStack[] hotbar = inventory.getHotbar();
            for (int i = 0; i < Math.min(playerIndex.hotbar.size(), hotbar.length); i++) {
                ItemStackData stackData = playerIndex.hotbar.get(i);
                if (stackData != null && stackData.itemId != null && stackData.count != null) {
                    Item item = Item.getItem(stackData.itemId);
                    if (item != null) {
                        hotbar[i] = new ItemStack(item, stackData.count);
                    }
                } else {
                    hotbar[i] = null;
                }
            }
        }

        updateCamera();
    }
}

package com.hackpanel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class HackPanelMod implements ClientModInitializer {
    public static boolean fly, speed, clickTp;
    public static final float[] FLY_SPEEDS = {0.05f, 0.1f, 0.25f, 0.5f};
    public static int flyIdx = 1;
    public static final double MAX_GROUND_SPEED = 0.5; // bloques/tick

    private static KeyBinding openKey, tpKey;
    private static boolean flyWasOn;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackpanel.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.hackpanel"));
        tpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hackpanel.clicktp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.hackpanel"));

        ClientTickEvents.END_CLIENT_TICK.register(HackPanelMod::tick);
    }

    private static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        while (openKey.wasPressed()) mc.setScreen(new PanelScreen());

        if (clickTp) {
            while (tpKey.wasPressed()) clickTeleport(p);
        } else {
            while (tpKey.wasPressed()) { /* vaciar */ }
        }

        PlayerAbilities ab = p.getAbilities();
        if (fly) {
            ab.allowFlying = true;
            ab.setFlySpeed(FLY_SPEEDS[flyIdx]);
            flyWasOn = true;
        } else if (flyWasOn) {
            flyWasOn = false;
            ab.setFlySpeed(0.05f);
            if (!p.isCreative() && !p.isSpectator()) {
                ab.allowFlying = false;
                ab.flying = false;
            }
        }

        if (speed && p.isOnGround() && !p.isFallFlying()
                && (p.input.movementForward != 0 || p.input.movementSideways != 0)) {
            Vec3d v = p.getVelocity();
            double hs = Math.hypot(v.x, v.z);
            if (hs > 0.0001 && hs < MAX_GROUND_SPEED) {
                double scale = Math.min(1.15, MAX_GROUND_SPEED / hs);
                p.setVelocity(v.x * scale, v.y, v.z * scale);
            }
        }
    }

    private static void clickTeleport(ClientPlayerEntity p) {
        HitResult hit = p.raycast(100.0, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult bhr = (BlockHitResult) hit;
        Vec3d pos = Vec3d.ofBottomCenter(bhr.getBlockPos().offset(bhr.getSide()));
        double y = bhr.getSide() == net.minecraft.util.math.Direction.DOWN ? pos.y - 1 : pos.y;
        teleport(p, pos.x, y, pos.z);
    }

    public static void teleport(ClientPlayerEntity p, double x, double y, double z) {
        // Usa /tp: funciona en mundos propios con trucos activados u operador.
        p.networkHandler.sendChatCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f", x, y, z));
    }
}

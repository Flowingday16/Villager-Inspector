package com.villagerinspector.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastHelper {

    public static Villager getTargetedVillager(Minecraft client, double maxDistance) {
        if (client == null || client.player == null || client.level == null) {
            return null;
        }

        // Direct crosshair pick if already targeted
        if (client.crosshairPickEntity instanceof Villager villager) {
            return villager;
        }

        Entity camera = client.getCameraEntity() != null ? client.getCameraEntity() : client.player;
        Vec3 eyePos = camera.getEyePosition(1.0F);
        Vec3 viewVec = camera.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.scale(maxDistance));
        AABB searchBox = camera.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
            camera,
            eyePos,
            reachVec,
            searchBox,
            entity -> entity instanceof Villager && entity.isAlive(),
            maxDistance * maxDistance
        );

        if (hitResult != null && hitResult.getEntity() instanceof Villager villager) {
            return villager;
        }

        return null;
    }
}

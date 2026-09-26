package com.villagerinspector.client;

import com.villagerinspector.data.VillagerStatusData;
import com.villagerinspector.diagnosis.VillagerDiagnosisEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

public class VillagerClientCache {
    private static VillagerStatusData currentStatus = null;
    private static long lastSeenTimestamp = 0;
    private static final long FADE_TIMEOUT_MS = 600;

    public static void setTargetedVillager(Minecraft client, Villager clientVillager) {
        if (clientVillager != null && clientVillager.isAlive()) {
            Villager targetVillager = clientVillager;
            ServerLevel serverLevel = null;

            if (client != null && client.getSingleplayerServer() != null && client.level != null) {
                try {
                    serverLevel = client.getSingleplayerServer().getLevel(client.level.dimension());
                    if (serverLevel != null) {
                        Entity serverEntity = serverLevel.getEntity(clientVillager.getId());
                        if (serverEntity instanceof Villager serverVillager) {
                            targetVillager = serverVillager;
                        }
                    }
                } catch (Throwable ignored) {}
            }

            currentStatus = VillagerDiagnosisEngine.diagnose(
                targetVillager,
                client != null ? client.level : null,
                serverLevel
            );
            lastSeenTimestamp = System.currentTimeMillis();
        }
    }

    public static VillagerStatusData getCurrentStatus() {
        if (currentStatus == null) {
            return null;
        }

        if (System.currentTimeMillis() - lastSeenTimestamp > FADE_TIMEOUT_MS) {
            currentStatus = null;
            return null;
        }

        return currentStatus;
    }

    public static void clear() {
        currentStatus = null;
        lastSeenTimestamp = 0;
    }
}

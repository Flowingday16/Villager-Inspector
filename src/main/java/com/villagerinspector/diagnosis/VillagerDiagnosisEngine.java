package com.villagerinspector.diagnosis;

import com.villagerinspector.data.OverallStatus;
import com.villagerinspector.data.VillagerIssue;
import com.villagerinspector.data.VillagerStatusData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class VillagerDiagnosisEngine {

    public static VillagerStatusData diagnose(Villager villager, Level clientLevel, ServerLevel serverLevel) {
        if (villager == null) return null;

        int entityId = villager.getId();
        BlockPos villagerPos = villager.blockPosition();
        VillagerData data = villager.getVillagerData();
        int level = data.level();
        int xp = villager.getVillagerXp();
        int maxXp = VillagerData.getMaxXpPerLevel(level);

        String profName;
        try {
            profName = data.profession().value().name().getString();
        } catch (Throwable t) {
            profName = Component.translatable("hud.villagerinspector.villager").getString();
        }

        boolean isNoneOrNitwit = data.profession().is(VillagerProfession.NONE) ||
                                 data.profession().is(VillagerProfession.NITWIT);

        Level activeWorld = (serverLevel != null) ? serverLevel : (clientLevel != null ? clientLevel : villager.level());

        // 1. Bed Detection (HOME)
        BlockPos bedPos = null;
        try {
            bedPos = villager.getBrain().getMemory(MemoryModuleType.HOME).map(GlobalPos::pos).orElse(null);
        } catch (Throwable ignored) {}

        // Fallback: Check Server POI Manager
        if (bedPos == null && serverLevel != null) {
            try {
                bedPos = serverLevel.getPoiManager().findClosest(
                    holder -> holder.is(PoiTypes.HOME),
                    pos -> true,
                    villagerPos,
                    32,
                    PoiManager.Occupancy.ANY
                ).orElse(null);
            } catch (Throwable ignored) {}
        }

        // Fallback: Scan surrounding blocks for bed
        if (bedPos == null && activeWorld != null) {
            BlockPos closest = null;
            double closestDistSq = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(villagerPos.offset(-20, -6, -20), villagerPos.offset(20, 6, 20))) {
                try {
                    BlockState state = activeWorld.getBlockState(p);
                    if (PoiTypes.hasPoi(state)) {
                        Optional<Holder<PoiType>> poiOpt = PoiTypes.forState(state);
                        if (poiOpt.isPresent() && poiOpt.get().is(PoiTypes.HOME)) {
                            double dSq = p.distSqr(villagerPos);
                            if (dSq < closestDistSq) {
                                closestDistSq = dSq;
                                closest = p.immutable();
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
            if (closest != null) {
                bedPos = closest;
            }
        }
        boolean hasBed = (bedPos != null);

        // 2. Workstation Detection (JOB_SITE)
        BlockPos workstationPos = null;
        try {
            workstationPos = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).map(GlobalPos::pos).orElse(null);
            if (workstationPos == null) {
                workstationPos = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).map(GlobalPos::pos).orElse(null);
            }
        } catch (Throwable ignored) {}

        Predicate<Holder<PoiType>> jobSitePred = null;
        try {
            jobSitePred = data.profession().value().heldJobSite();
        } catch (Throwable ignored) {}

        // Fallback: Check Server POI Manager for matching job site
        if (!isNoneOrNitwit && workstationPos == null && serverLevel != null && jobSitePred != null) {
            try {
                workstationPos = serverLevel.getPoiManager().findClosest(
                    jobSitePred,
                    pos -> true,
                    villagerPos,
                    32,
                    PoiManager.Occupancy.ANY
                ).orElse(null);
            } catch (Throwable ignored) {}
        }

        // Fallback: Scan surrounding blocks for matching job site
        if (!isNoneOrNitwit && workstationPos == null && activeWorld != null && jobSitePred != null) {
            BlockPos closest = null;
            double closestDistSq = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(villagerPos.offset(-20, -6, -20), villagerPos.offset(20, 6, 20))) {
                try {
                    BlockState state = activeWorld.getBlockState(p);
                    if (PoiTypes.hasPoi(state)) {
                        Optional<Holder<PoiType>> poiOpt = PoiTypes.forState(state);
                        if (poiOpt.isPresent() && jobSitePred.test(poiOpt.get())) {
                            double dSq = p.distSqr(villagerPos);
                            if (dSq < closestDistSq) {
                                closestDistSq = dSq;
                                closest = p.immutable();
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
            if (closest != null) {
                workstationPos = closest;
            }
        }
        boolean hasWorkstation = isNoneOrNitwit || (workstationPos != null) || (level > 1 || xp > 0);

        // 3. Health & Panic Detection
        float health = villager.getHealth();
        float maxHealth = villager.getMaxHealth();
        boolean isPanicked = false;
        try {
            isPanicked = villager.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE) ||
                         villager.getLastHurtByMob() != null;
            if (!isPanicked && activeWorld != null) {
                List<Monster> hostiles = activeWorld.getEntitiesOfClass(
                    Monster.class,
                    villager.getBoundingBox().inflate(10.0),
                    m -> m.isAlive() && m.getTarget() == villager
                );
                if (!hostiles.isEmpty()) {
                    isPanicked = true;
                }
            }
        } catch (Throwable ignored) {}

        boolean isBaby = villager.isBaby();
        int ageTicks = villager.getAge();

        // 4. Food & Breeding Check
        int foodPoints = 0;
        try {
            SimpleContainer inv = villager.getInventory();
            if (inv != null) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.is(Items.BREAD)) {
                        foodPoints += stack.getCount() * 4; // 3 bread = 12 points
                    } else if (stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT)) {
                        foodPoints += stack.getCount(); // 12 carrots/potatoes = 12 points
                    }
                }
            }
        } catch (Throwable ignored) {}

        boolean canBreed = (foodPoints >= 12) && !isBaby && (ageTicks == 0);

        // 5. Trades Check
        int lockedTradesCount = 0;
        int totalTradesCount = 0;
        try {
            MerchantOffers offers = villager.getOffers();
            if (offers != null) {
                totalTradesCount = offers.size();
                for (MerchantOffer offer : offers) {
                    if (offer.isOutOfStock()) {
                        lockedTradesCount++;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 6. Issue Collection
        List<VillagerIssue> issues = new ArrayList<>();

        // Danger criteria
        if (health / Math.max(maxHealth, 1.0f) <= 0.40f) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.DANGER,
                Component.translatable("issue.villagerinspector.low_health", String.format("%.0f", health), String.format("%.0f", maxHealth)).getString()
            ));
        }

        if (isPanicked) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.DANGER,
                Component.translatable("issue.villagerinspector.panicked").getString()
            ));
        }

        // Warning criteria
        if (!isNoneOrNitwit && !hasWorkstation) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.WARNING,
                Component.translatable("issue.villagerinspector.no_workstation").getString()
            ));
        }

        if (!hasBed) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.WARNING,
                Component.translatable("issue.villagerinspector.no_bed").getString()
            ));
        }

        if (totalTradesCount > 0 && lockedTradesCount == totalTradesCount) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.WARNING,
                Component.translatable("issue.villagerinspector.all_trades_locked").getString()
            ));
        } else if (lockedTradesCount > 0) {
            issues.add(new VillagerIssue(
                VillagerIssue.Severity.WARNING,
                Component.translatable("issue.villagerinspector.some_trades_locked", lockedTradesCount, totalTradesCount).getString()
            ));
        }

        if (!isBaby && !canBreed) {
            if (foodPoints < 12) {
                issues.add(new VillagerIssue(
                    VillagerIssue.Severity.WARNING,
                    Component.translatable("issue.villagerinspector.insufficient_food", foodPoints).getString()
                ));
            } else if (ageTicks > 0) {
                issues.add(new VillagerIssue(
                    VillagerIssue.Severity.WARNING,
                    Component.translatable("issue.villagerinspector.breeding_cooldown", ageTicks / 20).getString()
                ));
            }
        }

        // Calculate Overall Status
        OverallStatus status = OverallStatus.OPTIMAL;
        for (VillagerIssue issue : issues) {
            if (issue.getSeverity() == VillagerIssue.Severity.DANGER) {
                status = OverallStatus.DANGER;
                break;
            } else if (issue.getSeverity() == VillagerIssue.Severity.WARNING) {
                status = OverallStatus.WARNING;
            }
        }

        return new VillagerStatusData(
            entityId,
            villagerPos,
            profName,
            isNoneOrNitwit,
            level,
            xp,
            maxXp,
            hasBed,
            bedPos,
            hasWorkstation,
            workstationPos,
            health,
            maxHealth,
            isPanicked,
            isBaby,
            ageTicks,
            foodPoints,
            canBreed,
            lockedTradesCount,
            totalTradesCount,
            issues,
            status
        );
    }
}


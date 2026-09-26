package com.villagerinspector.data;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class VillagerStatusData {
    private final int entityId;
    private final BlockPos villagerPos;
    private final String professionName;
    private final boolean isNoneOrNitwit;
    private final int level;
    private final int xp;
    private final int maxXp;

    private final boolean hasBed;
    private final BlockPos bedPos;
    private final boolean hasWorkstation;
    private final BlockPos workstationPos;

    private final float health;
    private final float maxHealth;
    private final boolean isPanicked;
    private final boolean isBaby;
    private final int ageTicks;

    private final int foodPoints;
    private final boolean canBreed;

    private final int lockedTradesCount;
    private final int totalTradesCount;

    private final List<VillagerIssue> issues;
    private final OverallStatus overallStatus;

    public VillagerStatusData(
        int entityId,
        BlockPos villagerPos,
        String professionName,
        boolean isNoneOrNitwit,
        int level,
        int xp,
        int maxXp,
        boolean hasBed,
        BlockPos bedPos,
        boolean hasWorkstation,
        BlockPos workstationPos,
        float health,
        float maxHealth,
        boolean isPanicked,
        boolean isBaby,
        int ageTicks,
        int foodPoints,
        boolean canBreed,
        int lockedTradesCount,
        int totalTradesCount,
        List<VillagerIssue> issues,
        OverallStatus overallStatus
    ) {
        this.entityId = entityId;
        this.villagerPos = villagerPos;
        this.professionName = professionName;
        this.isNoneOrNitwit = isNoneOrNitwit;
        this.level = level;
        this.xp = xp;
        this.maxXp = maxXp;
        this.hasBed = hasBed;
        this.bedPos = bedPos;
        this.hasWorkstation = hasWorkstation;
        this.workstationPos = workstationPos;
        this.health = health;
        this.maxHealth = maxHealth;
        this.isPanicked = isPanicked;
        this.isBaby = isBaby;
        this.ageTicks = ageTicks;
        this.foodPoints = foodPoints;
        this.canBreed = canBreed;
        this.lockedTradesCount = lockedTradesCount;
        this.totalTradesCount = totalTradesCount;
        this.issues = issues != null ? issues : new ArrayList<>();
        this.overallStatus = overallStatus;
    }

    public int getEntityId() { return entityId; }
    public BlockPos getVillagerPos() { return villagerPos; }
    public String getProfessionName() { return professionName; }
    public boolean isNoneOrNitwit() { return isNoneOrNitwit; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getMaxXp() { return maxXp; }
    public boolean hasBed() { return hasBed; }
    public BlockPos getBedPos() { return bedPos; }
    public boolean hasWorkstation() { return hasWorkstation; }
    public BlockPos getWorkstationPos() { return workstationPos; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public boolean isPanicked() { return isPanicked; }
    public boolean isBaby() { return isBaby; }
    public int getAgeTicks() { return ageTicks; }
    public int getFoodPoints() { return foodPoints; }
    public boolean canBreed() { return canBreed; }
    public int getLockedTradesCount() { return lockedTradesCount; }
    public int getTotalTradesCount() { return totalTradesCount; }
    public List<VillagerIssue> getIssues() { return issues; }
    public OverallStatus getOverallStatus() { return overallStatus; }
}

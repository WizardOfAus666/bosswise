package com.bosswise.model;

import java.util.Collections;
import java.util.List;

public class GroupRolePlan {
    private final String roleName;
    private final String roleDescription;
    private final String keyEquipmentFocus;
    private final String specWeaponRecommendation;
    private final String supplyAdjustments;
    private final int expectedKillsPerTrip;
    private final long estimatedSplitProfitPerHour;
    private final List<String> duties;

    public GroupRolePlan(
            String roleName,
            String roleDescription,
            String keyEquipmentFocus,
            String specWeaponRecommendation,
            String supplyAdjustments,
            int expectedKillsPerTrip,
            long estimatedSplitProfitPerHour,
            List<String> duties) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.keyEquipmentFocus = keyEquipmentFocus;
        this.specWeaponRecommendation = specWeaponRecommendation;
        this.supplyAdjustments = supplyAdjustments;
        this.expectedKillsPerTrip = expectedKillsPerTrip;
        this.estimatedSplitProfitPerHour = estimatedSplitProfitPerHour;
        this.duties = duties != null ? duties : Collections.emptyList();
    }

    public String getRoleName() { return roleName; }
    public String getRoleDescription() { return roleDescription; }
    public String getKeyEquipmentFocus() { return keyEquipmentFocus; }
    public String getSpecWeaponRecommendation() { return specWeaponRecommendation; }
    public String getSupplyAdjustments() { return supplyAdjustments; }
    public int getExpectedKillsPerTrip() { return expectedKillsPerTrip; }
    public long getEstimatedSplitProfitPerHour() { return estimatedSplitProfitPerHour; }
    public List<String> getDuties() { return duties; }

    public String getFormattedSplitProfit() {
        if (estimatedSplitProfitPerHour >= 1_000_000) {
            return String.format("%.2fM GP/hr (split)", estimatedSplitProfitPerHour / 1_000_000.0);
        } else if (estimatedSplitProfitPerHour >= 1_000) {
            return String.format("%dK GP/hr (split)", estimatedSplitProfitPerHour / 1_000);
        }
        return estimatedSplitProfitPerHour + " GP/hr";
    }
}

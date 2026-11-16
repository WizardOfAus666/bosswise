package com.bosswise.service;

import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.LootDropItem;
import com.bosswise.model.LootSession;
import com.bosswise.model.MonsterProfitProfile;
import com.bosswise.model.OptimizedGearSetup;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class ExportService {

    public String exportGearSetupAsJson(OptimizedGearSetup setup) {
        if (setup == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"boss\": \"").append(setup.getBoss() != null ? setup.getBoss().getName() : "Custom").append("\",\n");
        sb.append("  \"combatStyle\": \"").append(setup.getCombatStyle().name()).append("\",\n");
        sb.append("  \"tier\": \"").append(setup.getTierName()).append("\",\n");
        sb.append("  \"totalPrice\": ").append(setup.getTotalPrice()).append(",\n");
        sb.append("  \"equipment\": {\n");

        boolean first = true;
        for (Map.Entry<EquipmentSlot, EquipmentItem> entry : setup.getEquipment().entrySet()) {
            if (!first) sb.append(",\n");
            first = false;
            EquipmentItem item = entry.getValue();
            sb.append("    \"").append(entry.getKey().name()).append("\": {\n");
            sb.append("      \"id\": ").append(item.getItemId()).append(",\n");
            sb.append("      \"name\": \"").append(item.getName()).append("\",\n");
            sb.append("      \"price\": ").append(item.getPrice()).append(",\n");
            sb.append("      \"tier\": ").append(item.getTier()).append("\n");
            sb.append("    }");
        }
        sb.append("\n  }\n}");
        return sb.toString();
    }

    public String exportGearSetupAsCsv(OptimizedGearSetup setup) {
        if (setup == null) return "Slot,Item ID,Item Name,Price,Tier\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Slot,Item ID,Item Name,Price,Tier\n");
        for (Map.Entry<EquipmentSlot, EquipmentItem> entry : setup.getEquipment().entrySet()) {
            EquipmentItem item = entry.getValue();
            sb.append(entry.getKey().getDisplayName()).append(",")
                    .append(item.getItemId()).append(",")
                    .append("\"").append(item.getName()).append("\",")
                    .append(item.getPrice()).append(",")
                    .append(item.getTier()).append("\n");
        }
        return sb.toString();
    }

    public String exportInventoryAsJson(InventorySetup setup) {
        if (setup == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"tier\": \"").append(setup.getTierName()).append("\",\n");
        sb.append("  \"usedSlots\": ").append(setup.getUsedSlots()).append(",\n");
        sb.append("  \"totalCost\": ").append(setup.getTotalCost()).append(",\n");
        sb.append("  \"items\": [\n");

        boolean first = true;
        for (InventoryItem item : setup.getItems()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    {\n");
            sb.append("      \"id\": ").append(item.getItemId()).append(",\n");
            sb.append("      \"name\": \"").append(item.getName()).append("\",\n");
            sb.append("      \"quantity\": ").append(item.getQuantity()).append(",\n");
            sb.append("      \"category\": \"").append(item.getCategory()).append("\",\n");
            sb.append("      \"unitPrice\": ").append(item.getUnitPrice()).append("\n");
            sb.append("    }");
        }
        sb.append("\n  ]\n}");
        return sb.toString();
    }

    public String exportLootSessionAsJson(LootSession session) {
        if (session == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"monster\": \"").append(session.getMonsterName()).append("\",\n");
        sb.append("  \"kills\": ").append(session.getKills()).append(",\n");
        sb.append("  \"duration\": \"").append(session.getFormattedDuration()).append("\",\n");
        sb.append("  \"totalLoot\": ").append(session.getTotalLootValue()).append(",\n");
        sb.append("  \"supplyCosts\": ").append(session.getEstimatedSupplyCosts()).append(",\n");
        sb.append("  \"netProfit\": ").append(session.getNetProfit()).append(",\n");
        sb.append("  \"profitPerHour\": ").append(session.getProfitPerHour()).append(",\n");
        sb.append("  \"drops\": [\n");

        boolean first = true;
        for (LootDropItem drop : session.getDrops()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("    {\n");
            sb.append("      \"id\": ").append(drop.getItemId()).append(",\n");
            sb.append("      \"name\": \"").append(drop.getName()).append("\",\n");
            sb.append("      \"quantity\": ").append(drop.getQuantity()).append(",\n");
            sb.append("      \"unitPrice\": ").append(drop.getUnitPrice()).append(",\n");
            sb.append("      \"totalValue\": ").append(drop.getTotalValue()).append(",\n");
            sb.append("      \"rarity\": \"").append(drop.getRarity().name()).append("\"\n");
            sb.append("    }");
        }
        sb.append("\n  ]\n}");
        return sb.toString();
    }

    public String exportLootSessionAsCsv(LootSession session) {
        if (session == null) return "Item ID,Item Name,Quantity,Unit Price,Total Value,Rarity\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Item ID,Item Name,Quantity,Unit Price,Total Value,Rarity\n");
        for (LootDropItem drop : session.getDrops()) {
            sb.append(drop.getItemId()).append(",")
                    .append("\"").append(drop.getName()).append("\",")
                    .append(drop.getQuantity()).append(",")
                    .append(drop.getUnitPrice()).append(",")
                    .append(drop.getTotalValue()).append(",")
                    .append(drop.getRarity().getLabel()).append("\n");
        }
        return sb.toString();
    }

    public boolean copyToClipboard(String content) {
        if (content == null) return false;
        try {
            StringSelection selection = new StringSelection(content);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

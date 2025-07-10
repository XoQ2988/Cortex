package me.xoq.cortex.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.xoq.cortex.CortexClient.mc;

public class InventoryUtils {
    private InventoryUtils() { }

    public static List<ItemStack> getHotbarStacks() {
        PlayerInventory inv = mc.player.getInventory();
        List<ItemStack> hot = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            hot.add(inv.getStack(i));
        }
        return hot;
    }

    public static int getSelectedHotbarSlot() {
        return mc.player.getInventory().getSelectedSlot();
    }

    public static void setSelectedHotbarSlot(int slot) {
        PlayerInventory inv = mc.player.getInventory();
        inv.setSelectedSlot(slot);
        mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot));
    }

    public static Object2IntMap<RegistryEntry<Enchantment>> getEnchantments(ItemStack stack) {
        Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>();

        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> entries =
                stack.getItem() == Items.ENCHANTED_BOOK
                        ? stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
                            .getEnchantmentEntries()
                        : stack.getEnchantments().getEnchantmentEntries();

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : entries) {
            enchantments.put(entry.getKey(), entry.getIntValue());
        }

        return enchantments;
    }

    public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty()) return 0;

        return getEnchantmentLevel(getEnchantments(stack), enchantmentKey);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantments.object2IntEntrySet()) {
            if (entry.getKey().matchesKey(enchantment)) return entry.getIntValue();
        }
        return 0;
    }
}

package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.ItemStackTooltipEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.util.InventoryUtils;
import me.xoq.cortex.util.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BetterTooltips extends Module {
    public BetterTooltips() {
        super("better-tooltips", "");
    }

    private static final int[] AXOLOTL_COLORS = new int[] {
            0xFFC7EC, 0x8C6C50, 0xFAD41B, 0xE8F7Fb, 0xB6B5FE
    };

    @EventListener
    private void getItemStackTooltipData(ItemStackTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Durability
        if (stack.isDamageable()) {
            int max = stack.getMaxDamage();
            int dmg = stack.getDamage();
            int remaining = max - dmg;
            int pct = Math.round(remaining * 100f / max);

            int unbreaking = InventoryUtils.getEnchantmentLevel(stack, Enchantments.UNBREAKING);
            int effectiveUses = remaining * (unbreaking + 1);

            Text durabilityText = Text.literal("")
                    // Durability:
                    .append(Text.literal("Durability: ").formatted(Formatting.GRAY))
                    // XX%
                    .append(Text.literal(pct + "%")
                            .formatted(pct < 10 ? Formatting.RED : pct < 30 ? Formatting.YELLOW : Formatting.GREEN))
                    // " ("
                    .append(Text.literal(" (").formatted(Formatting.GRAY))
                    // either "remaining" or "~effective"
                    .append(Text.literal(unbreaking > 0 ? "~" + effectiveUses : String.valueOf(remaining))
                            .formatted(unbreaking > 0 ? Formatting.AQUA: Formatting.GRAY))
                    // " left)"
                    .append(Text.literal(" left)").formatted(Formatting.GRAY));

            event.appendEnd(durabilityText);
        }

        // Axolotl Variant
        AxolotlEntity.Variant axolotlVariant = stack.get(DataComponentTypes.AXOLOTL_VARIANT);
        if (axolotlVariant != null) {
            Text variantText =  Text.literal("Variant: ").formatted(Formatting.GRAY)
                    .append(Utils.nameToTitle(axolotlVariant.getId()))
                    .setStyle(Style.EMPTY.withColor(AXOLOTL_COLORS[axolotlVariant.getIndex()]));

            event.appendEnd(variantText);
        }

        // Food Stats
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            Text hungerText = Text.literal("Restores: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.valueOf(foodComponent.nutrition())).formatted(Formatting.GREEN));

            Text satText = Text.literal("Saturation: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.1f", foodComponent.saturation())).formatted(Formatting.GOLD));

            event.appendEnd(hungerText);
            event.appendEnd(satText);
        }
    }
}

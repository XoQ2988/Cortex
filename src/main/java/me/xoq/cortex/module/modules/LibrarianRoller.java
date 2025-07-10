package me.xoq.cortex.module.modules;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockInteractEvent;
import me.xoq.cortex.event.entity.EntityInteractEvent;
import me.xoq.cortex.event.misc.OpenScreenEvent;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
import me.xoq.cortex.setting.EnumSetting;
import me.xoq.cortex.setting.Setting;
import me.xoq.cortex.util.ChatUtils;
import me.xoq.cortex.util.InventoryUtils;
import me.xoq.cortex.util.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;

import java.util.Optional;

import static me.xoq.cortex.CortexClient.mc;

public class LibrarianRoller extends Module {
    public LibrarianRoller() {
        super("librarian-roller", "");
    }

    private final Setting<EnchantmentType> enchantment = registerSetting(
            new EnumSetting.Builder<EnchantmentType>()
                    .name("enchantment")
                    .description("Which enchantment on the book to target (or Any)")
                    .enumClass(EnchantmentType.class)
                    .defaultValue(EnchantmentType.ANY)
                    .build()
    );

    private final Setting<Boolean> onlyMax = registerSetting(
            new BoolSetting.Builder()
                    .name("only-max-level")
                    .description("Only accept trades if the book's enchantment is at max level")
                    .defaultValue(true)
                    .build()
    );

    public enum EnchantmentType {
        ANY("Any", null),
        EFFICIENCY("Efficiency", Enchantments.EFFICIENCY),
        PROTECTION("Protection", Enchantments.PROTECTION),
        ;

        public final String title;
        public final RegistryKey<Enchantment> enchantment;

        EnchantmentType(String title, RegistryKey<Enchantment> enchantment) {
            this.title = title;
            this.enchantment = enchantment;
        }
    }

    private VillagerEntity villager;
    private BlockPos lecternPos;
    private int stage;

    @Override
    protected void onEnable() {
        villager = null;
        lecternPos = null;
        stage = 0;
    }

    @EventListener
    private void onInteractEntity(EntityInteractEvent event) {
        if (villager != null) return;
        if (!(event.getTarget() instanceof VillagerEntity v)) return;

        villager = v;
        event.cancel();

        ChatUtils.info("Targeting " + villager.getName().getString());
    }

    @EventListener
    private void onInteractBlock(BlockInteractEvent event) {
        if (mc.world == null || villager == null) return;

        BlockPos pos = event.getHitResult().getBlockPos();
        if (lecternPos != null) return;

        if (!(mc.world.getBlockState(pos).getBlock() == Blocks.LECTERN)) return;

        lecternPos = pos;
        event.cancel();

        ChatUtils.info("Targeting Lectern at " + pos.toShortString());
    }

    @EventListener
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof MerchantScreen merchantScreen)) return;

        if (lecternPos == null && villager != null) {
            event.cancel();
            return;
        }

        if (stage < 5) event.cancel();
    }

    @EventListener
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null || villager == null || lecternPos == null) return;

        switch (stage) {
            // Break block
            case 0 -> {
                if (mc.world.getBlockState(lecternPos).getBlock() != Blocks.AIR) {
                    mc.interactionManager.attackBlock(lecternPos, Direction.UP);
                    ChatUtils.info("Breaking lectern...");
                    stage = 1;
                }
            }

            // Check if block is broken
            case 1 -> {
                if (mc.world.getBlockState(lecternPos).isAir()) {
                    ChatUtils.info("Lectern broken");
                    stage = 2;
                }
            }

            // Wait for villager to have no profession
            case 2 -> {
                Optional<RegistryKey<VillagerProfession>> prof = villager.getVillagerData().profession().getKey();
                if (prof.isPresent() && prof.get() == VillagerProfession.NONE) {
                    ChatUtils.info("Profession cleared");
                    stage = 3;
                }
            }

            // Place lectern
            case 3 -> {
                int slot = InventoryUtils.getHotbarStacks().stream()
                        .map(ItemStack::getItem)
                        .toList()
                        .indexOf(Blocks.LECTERN.asItem());

                if (slot < 0) {
                    ChatUtils.warn("No lectern in hotbar! Disabling.");
                    disable();
                    return;
                }

                int previous = InventoryUtils.getSelectedHotbarSlot();
                Utils.place(lecternPos, slot);
                InventoryUtils.setSelectedHotbarSlot(previous);

                stage = 4;
                ChatUtils.info("Placed lectern");
            }

            // Wait for villager to become librarian
            case 4 -> {
                Optional<RegistryKey<VillagerProfession>> prof = villager.getVillagerData().profession().getKey();
                if (prof.isPresent() && prof.get() == VillagerProfession.LIBRARIAN) {
                    ChatUtils.info("Became librarian");
                    stage = 5;
                }
            }

            // Interact with villager
            case 5 -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.interactionManager.interactEntity(mc.player, villager, Hand.MAIN_HAND);
                stage = 6;
            }

            // Scan offers
            case 6 -> {
                if (!(mc.currentScreen instanceof MerchantScreen screen)) return;
                MerchantScreenHandler handler = screen.getScreenHandler();
                TradeOfferList offers = handler.getRecipes();

                for (int i = 0; i < offers.size(); i++) {
                    TradeOffer offer = offers.get(i);
                    ItemStack result = offer.getSellItem();

                    // only enchanted books
                    if (result.getItem() != Items.ENCHANTED_BOOK) continue;

                    // pull out all enchantments on this book
                    var enchantmentEntries = result.getEnchantments().getEnchantmentEntries();

                    // find the level for our desired enchantment type
                    int level = 0;
                    for (var entry : enchantmentEntries) {
                        RegistryEntry<Enchantment> e = entry.getKey();
                        int lvl = entry.getIntValue();
                        if (e.matchesKey(enchantment.get().enchantment)) {
                            level = lvl;
                            break;
                        }
                    }

                    boolean matchesType = enchantment.get() == EnchantmentType.ANY
                            || level > 0;
                    boolean matchesMax  = !onlyMax.get()
                            || level == offer.getFirstBuyItem().

                    if (matchesType && matchesMax) {
                        // select this offer
                        int recipeSlot = i + handler.getRecipeSlotStartIndex();
                        handler.onSlotClick(handler.syncId, recipeSlot, 0, SlotActionType.PICKUP, mc.player);

                        // take the result
                        int outputSlot = handler.getOutputSlot(); // e.g. 2, or use handler.getTradeResultSlotIndex()
                        handler.onSlotClick(handler.syncId, outputSlot, 0, SlotActionType.QUICK_MOVE, mc.player);

                        ChatUtils.info("✅ Rolled " +
                                enchantment.get().title +
                                " lvl " + level);
                        stage = 7;
                        return;
                    }
                }

                // no match → refresh
                int refreshSlot = handler.getRefreshSlot(); // usually 0
                handler.onSlotClick(handler.syncId, refreshSlot, 0, SlotActionType.PICKUP, mc.player);
                ChatUtils.info("🔄 No match—refreshing trades");
                stage = 5;
            }
        }
    }
}

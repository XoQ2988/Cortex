package me.xoq.cortex.module.modules;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.block.BlockInteractEvent;
import me.xoq.cortex.event.entity.EntityInteractEvent;
import me.xoq.cortex.event.misc.OpenScreenEvent;
import me.xoq.cortex.event.misc.TickEvent;
import me.xoq.cortex.module.Module;
import me.xoq.cortex.setting.BoolSetting;
import me.xoq.cortex.setting.EnumSetting;
import me.xoq.cortex.setting.IntSetting;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
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

    private final Setting<Integer> priceThreshold = registerSetting(
            new IntSetting.Builder()
                    .name("price-threshold")
                    .description("Maximum allowed cost as a percentage of the optimal price (100 = optimal)")
                    .defaultValue(150)
                    .min(100).max(500)
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
    private String status;

    @Override
    protected void onEnable() {
        villager = null;
        lecternPos = null;
        stage = -1;
        status = null;
    }

    @EventListener
    private void onInteractEntity(EntityInteractEvent event) {
        if (!isEnabled() || villager != null) return;
        if (!(event.getTarget() instanceof VillagerEntity v)) return;

        villager = v;
        event.cancel();

        status = "Villager at " + villager.getBlockPos().toShortString();
    }

    @EventListener
    private void onInteractBlock(BlockInteractEvent event) {
        if (mc.world == null || !isEnabled() || villager == null) return;

        BlockPos pos = event.getHitResult().getBlockPos();
        if (lecternPos != null) return;

        if (!(mc.world.getBlockState(pos).getBlock() == Blocks.LECTERN)) return;

        lecternPos = pos;
        stage = 1;
        event.cancel();

        status  = "Lectern at " + pos.toShortString();
    }

    @EventListener
    private void onOpenScreen(OpenScreenEvent.Pre event) {
        if (!(event.getScreen() instanceof MerchantScreen)) return;

        if (villager != null && lecternPos == null) {event.cancel();}
    }

    @EventListener
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null || villager == null || lecternPos == null) return;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof MerchantScreen)) return;

        switch (stage) {
            // Break block
            case 1 -> {
                if (mc.world.getBlockState(lecternPos).isAir()) {
                    stage = 2;
                } else {
                    Utils.breakBlock(lecternPos);
                }
            }

            // Wait for villager to have no profession
            case 2 -> {
                Optional<RegistryKey<VillagerProfession>> prof = villager.getVillagerData().profession().getKey();
                if (prof.isPresent() && prof.get() == VillagerProfession.NONE) {
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
                    ChatUtils.warn("No lectern in hotbar!");
                    disable();
                    return;
                }

                int previous = InventoryUtils.getSelectedHotbarSlot();
                Utils.place(lecternPos, slot);
                InventoryUtils.setSelectedHotbarSlot(previous);

                stage = 4;
            }

            // Wait for villager to become librarian
            case 4 -> {
                Optional<RegistryKey<VillagerProfession>> prof = villager.getVillagerData().profession().getKey();
                if (prof.isPresent() && prof.get() == VillagerProfession.LIBRARIAN) {
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

                boolean found = false;

                for (TradeOffer offer : offers) {
                    ItemStack book = offer.getSellItem();
                    if (!book.isOf(Items.ENCHANTED_BOOK)) continue;

                    int cost = offer.getOriginalFirstBuyItem().getCount();
                    int pct = priceThreshold.get();

                    Object2IntMap<RegistryEntry<Enchantment>> enchants = InventoryUtils.getEnchantments(book);
                    for (var entry : Object2IntMaps.fastIterable(enchants)) {
                        RegistryEntry<Enchantment> registryEntry = entry.getKey();

                        EnchantmentType target = enchantment.get();
                        if (target != EnchantmentType.ANY && !registryEntry.matchesKey(target.enchantment)) {
                            status = "Skip: " + registryEntry.getIdAsString() + " not target";
                            continue;
                        }

                        int allowed = getPrice(registryEntry, pct);
                        if (cost > allowed) {
                            status = "Skip: cost " + cost + " > " + allowed;
                            continue;
                        }

                        int level = entry.getIntValue();
                        int max = registryEntry.value().getMaxLevel();
                        if (onlyMax.get() && level != max) {
                            status = "Skip: level " + level + " > " + max + " for " + registryEntry.getIdAsString();
                            continue;
                        }

                        ChatUtils.info("Got " + registryEntry.getIdAsString() + " " + level + " for " + cost);
                        found = true;
                        break;
                    }
                    if (found) break;
                }

                if (found) {
                    disable();
                    stage = -1;
                    status = null;
                } else {
                    screen.close();
                    stage = 1;
                }
            }

            default -> { }
        }
    }

    @Override
    protected String getStatus() {
        return !isEnabled() ? null : "Stage " + stage + ": " + status;
    }

    private static int getOptimalPrice(RegistryEntry<Enchantment> entry) {
        int max = entry.value().getMaxLevel();
        int price = 2 + 3 * max;

        if (entry.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) return price * 2;
        return price;
    }

    public static int getPrice(RegistryEntry<Enchantment> entry, int percent) {
        int basePrice = getOptimalPrice(entry);
        int allowed = (int) Math.ceil(basePrice * percent / 100.0);
        return Math.min(allowed, 64);
    }
}

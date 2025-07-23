package me.xoq.cortex.util;

import me.xoq.cortex.event.EventListener;
import me.xoq.cortex.event.misc.TickEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static me.xoq.cortex.CortexClient.mc;

public class Utils {
    public static boolean breaking;
    private static boolean breakingThisTick;

    public static String nameToTitle(String name) {
        name = name.replace("-", " ");

        return Arrays.stream(name.split("[^A-Za-z0-9]+"))
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    String lower = s.toLowerCase();
                    return Character.toUpperCase(lower.charAt(0))
                            + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
    }

    public static String keyToString(int key) {
        if (key < 0) {
            return "Unbound";
        }

        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, 0);
        if (inputKey != InputUtil.UNKNOWN_KEY) {
            return prettifyKeyName(inputKey.getLocalizedText().getString());
        }

        return "Unknown";
    }

    /**
     * Try to turn something like "key.keyboard.left_shift" or "GLFW_KEY_SPACE"
     * into "Left Shift" or "Space".
     */
    private static String prettifyKeyName(String raw) {
        // Strip off any namespace or dots
        String simple = raw.contains(".")
                ? raw.substring(raw.lastIndexOf('.') + 1)
                : raw;

        simple = simple.replace('_', ' ');

        return Arrays.stream(simple.split(" "))
                .map(s -> {
                    String lower = s.toLowerCase(Locale.ROOT);
                    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
    }

    public static void place(BlockPos blockPos, int slot) {
        place(blockPos, Direction.UP, slot);
    }

    public static void place(BlockPos blockPos, Direction side, int slot) {
        if (mc.player == null) return;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockHitResult hitResult = new BlockHitResult(hitPos, side, blockPos, false);

        InventoryUtils.setSelectedHotbarSlot(slot);
        interact(hitResult);
    }

    public static void interact(BlockHitResult blockHitResult) {
        if (mc.player == null || mc.interactionManager == null) return;

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);

        if (result.isAccepted())
            mc.player.swingHand(Hand.MAIN_HAND);
    }

    public static boolean breakBlock(BlockPos blockPos) {
        if (!canBreak(blockPos, mc.world.getBlockState(blockPos))) return false;

        BlockPos pos = blockPos instanceof BlockPos.Mutable ? new BlockPos(blockPos) : blockPos;

        Direction direction = getDirection(pos);
        if (mc.interactionManager.isBreakingBlock()) {
            mc.interactionManager.updateBlockBreakingProgress(pos, direction);
        } else {
            mc.getNetworkHandler().sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
            );
        }

        mc.getNetworkHandler().sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,  pos, Direction.UP)
        );

        mc.player.swingHand(Hand.MAIN_HAND);

        breaking = true;
        breakingThisTick = true;

        return true;
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    public static Direction getDirection(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        if ((double) pos.getY() > eyesPos.y) {
            if (mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable()) return Direction.DOWN;
            else return mc.player.getHorizontalFacing().getOpposite();
        }
        if (!mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable()) return mc.player.getHorizontalFacing().getOpposite();
        return Direction.UP;
    }
}

package io.sneery.detominer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CubeBreaker {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            ResourceKey<Enchantment> DETONATE_KEY = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("detominer", "detonate"));
            Registry<Enchantment> enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Enchantment detonate = enchantmentRegistry.getValue(DETONATE_KEY);
            var detonateEntry = enchantmentRegistry.get(enchantmentRegistry.getId(detonate));

            ItemStack tool = player.getMainHandItem();
            if (detonate != null && detonateEntry.map(entry -> EnchantmentHelper.getItemEnchantmentLevel(entry, tool)).orElse(0) > 0) {
                int detonateLevel = detonateEntry.map(entry -> EnchantmentHelper.getItemEnchantmentLevel(entry, tool)).orElse(0);

                if(ConfigManager.config.only_mine_with_proper_tool)
                {
                    if (isBlockAllowed(state.getBlock(), tool) && tool.isCorrectToolForDrops(state)) {
                        return mining_when_sneaking(world, player, pos, detonateLevel);
                    }
                } else {
                    if (isBlockAllowed(state.getBlock(), tool)) {
                        return mining_when_sneaking(world, player, pos, detonateLevel);
                    }
                }
            }
            return true;
        });
    }

    private static boolean mining_when_sneaking(Level world, Player player, BlockPos pos, int detonateLevel) {
        if (ConfigManager.config.disable_detonate_when_sneaking) {
            if (detonateLevel > 0 && !player.isShiftKeyDown()) {
                breakCube(world, (ServerPlayer) player, pos, detonateLevel);
                return false;
            }
        } else {
            if (detonateLevel > 0) {
                breakCube(world, (ServerPlayer) player, pos, detonateLevel);
                return false;
            }
        }
        return true;
    }

    private static void breakCube(Level world, ServerPlayer player, BlockPos center, int detonateLevel) {
        DetominerConfig.BlockBreak blockBreak = ConfigManager.config.block_break;
        DetominerConfig.Level levelConfig = switch (detonateLevel) {
            case 3 -> blockBreak.detonate_level_3;
            case 2 -> blockBreak.detonate_level_2;
            default -> blockBreak.detonate_level_1;
        };

        int xSize = levelConfig.x;
        int ySize = levelConfig.y;
        int zSize = levelConfig.z;

        int xOffset = -(xSize / 2);
        int yOffset = -(ySize / 2);
        int zOffset = -(zSize / 2);

        float yaw = player.getYRot() % 360;
        float pitch = player.getXRot();

        int fx = 0, fy = 0, fz = 0;
        int ux = 0, uy = 1, uz = 0;
        int rx = 1, ry = 0, rz = 0;

        if (pitch > 45) {
            fx = 0; fy = -1; fz = 0;
            rx = 1; ry = 0; rz = 0;
            ux = 0; uy = 0; uz = 1;
        } else if (pitch < -45) {
            fx = 0; fy = 1; fz = 0;
            rx = 1; ry = 0; rz = 0;
            ux = 0; uy = 0; uz = -1;
        } else {
            if (yaw < 0) yaw += 360;
            if (yaw >= 315 || yaw < 45) {
                fx = 0; fy = 0; fz = 1;
                rx = 1; ry = 0; rz = 0;
                ux = 0; uy = 1; uz = 0;
            } else if (yaw >= 45 && yaw < 135) {
                fx = -1; fy = 0; fz = 0;
                rx = 0; ry = 0; rz = -1;
                ux = 0; uy = 1; uz = 0;
            } else if (yaw >= 135 && yaw < 225) {
                fx = 0; fy = 0; fz = -1;
                rx = -1; ry = 0; rz = 0;
                ux = 0; uy = 1; uz = 0;
            } else {
                fx = 1; fy = 0; fz = 0;
                rx = 0; ry = 0; rz = 1;
                ux = 0; uy = 1; uz = 0;
            }
        }

        mineHelper(world, player, center);

        for (int lx = 0; lx < xSize; lx++) {
            for (int ly = 0; ly < ySize; ly++) {
                for (int lz = 0; lz < zSize; lz++) {
                    int dx = (lx + xOffset) * rx + (ly + yOffset) * ux + (lz + zOffset) * fx;
                    int dy = (lx + xOffset) * ry + (ly + yOffset) * uy + (lz + zOffset) * fy;
                    int dz = (lx + xOffset) * rz + (ly + yOffset) * uz + (lz + zOffset) * fz;
                    BlockPos target = center.offset(dx, dy, dz);
                    if (target.equals(center)) continue;
                    if (mineHelper(world, player, target)) return;
                }
            }
        }
    }

    private static boolean mineHelper(Level world, ServerPlayer player, BlockPos target) {
        if (!world.isEmptyBlock(target)) {
            var state = world.getBlockState(target);
            var block = state.getBlock();
            var tool = player.getMainHandItem();
            var slot = player.getEquipmentSlotForItem(tool);

            if(ConfigManager.config.only_mine_with_proper_tool)
            {
                if (isBlockAllowed(block, tool) && tool.isCorrectToolForDrops(state)) {
                    literalMine(world, player, target, state, block, tool, slot);
                }
            } else {
                if (isBlockAllowed(block, tool)) {
                    literalMine(world, player, target, state, block, tool, slot);
                }
            }
        }
        return false;
    }

    private static void literalMine(Level world, ServerPlayer player, BlockPos target, BlockState state, Block block, ItemStack tool, EquipmentSlot slot) {
        block.playerWillDestroy(world, target, state, player);
        if (!world.isClientSide() && !player.isCreative()) {
            block.playerDestroy(world, player, target, state, world.getBlockEntity(target), tool);
            tool.hurtAndBreak(1, player, slot);
        }
        world.levelEvent(2001, target, Block.getId(state));
        world.removeBlock(target, false);
    }

    private static String getToolTypeKey(ItemStack tool) {
        if (tool.is(net.minecraft.tags.ItemTags.PICKAXES)) return "#minecraft:pickaxes";
        if (tool.is(net.minecraft.tags.ItemTags.AXES)) return "#minecraft:axes";
        if (tool.is(net.minecraft.tags.ItemTags.SHOVELS)) return "#minecraft:shovels";
        if (tool.is(net.minecraft.tags.ItemTags.HOES)) return "#minecraft:hoes";
        if (tool.is(net.minecraft.world.item.Items.SHEARS)) return "minecraft:shears";
        return null;
    }

    private static boolean isBlockAllowed(Block block, ItemStack tool) {
        String toolKey = getToolTypeKey(tool);
        if (toolKey == null) return false;
        java.util.List<String> allowed = ConfigManager.config.allowed_blocks.get(toolKey);
        if (allowed == null) return false;
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        return allowed.contains(blockId);
    }
}
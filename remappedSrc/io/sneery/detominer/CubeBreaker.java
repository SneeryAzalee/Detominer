package io.sneery.detominer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class CubeBreaker {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            RegistryKey<Enchantment> DETONATE_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("detominer", "detonate"));
            Registry<Enchantment> enchantmentRegistry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
            Enchantment detonate = enchantmentRegistry.get(DETONATE_KEY);
            var detonateEntry = enchantmentRegistry.getEntry(enchantmentRegistry.getRawId(detonate));

            ItemStack tool = player.getMainHandStack();
            if (detonate != null && detonateEntry.map(entry -> EnchantmentHelper.getLevel(entry, tool)).orElse(0) > 0) {
                int detonateLevel = detonateEntry.map(entry -> EnchantmentHelper.getLevel(entry, tool)).orElse(0);

                if(ConfigManager.config.only_mine_with_proper_tool)
                {
                    if (isBlockAllowed(state.getBlock(), tool) && tool.isSuitableFor(state)) {
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

    private static boolean mining_when_sneaking(World world, PlayerEntity player, BlockPos pos, int detonateLevel) {
        if (ConfigManager.config.disable_detonate_when_sneaking) {
            if (detonateLevel > 0 && !player.isSneaking()) {
                breakCube(world, (ServerPlayerEntity) player, pos, detonateLevel);
                return false;
            }
        } else {
            if (detonateLevel > 0) {
                breakCube(world, (ServerPlayerEntity) player, pos, detonateLevel);
                return false;
            }
        }
        return true;
    }

    private static void breakCube(World world, ServerPlayerEntity player, BlockPos center, int detonateLevel) {
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

        float yaw = player.getYaw() % 360;
        float pitch = player.getPitch();

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
                    BlockPos target = center.add(dx, dy, dz);
                    if (target.equals(center)) continue;
                    if (mineHelper(world, player, target)) return;
                }
            }
        }
    }

    private static boolean mineHelper(World world, ServerPlayerEntity player, BlockPos target) {
        if (!world.isAir(target)) {
            var state = world.getBlockState(target);
            var block = state.getBlock();
            var tool = player.getMainHandStack();

            if(ConfigManager.config.only_mine_with_proper_tool)
            {
                if (isBlockAllowed(block, tool) && tool.isSuitableFor(state)) {
                    literalMine(world, player, target, state, block, tool);
                }
            } else {
                if (isBlockAllowed(block, tool)) {
                    literalMine(world, player, target, state, block, tool);
                }
            }
        }
        return false;
    }

    private static void literalMine(World world, ServerPlayerEntity player, BlockPos target, BlockState state, Block block, ItemStack tool) {
        block.onBreak(world, target, state, player);
        if (!world.isClient() && !player.isCreative()) {
            block.afterBreak(world, player, target, state, world.getBlockEntity(target), tool);
            if (tool.getMaxDamage() - tool.getDamage() > 1) {
                tool.damage(1, player);
            } else {
                player.sendEquipmentBreakStatus(tool.getItem(), net.minecraft.entity.EquipmentSlot.MAINHAND);
                player.setStackInHand(player.getActiveHand(), ItemStack.EMPTY);
            }
        }
        world.syncWorldEvent(2001, target, Block.getRawIdFromState(state));
        world.removeBlock(target, false);
    }

    private static String getToolTypeKey(ItemStack tool) {
        if (tool.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES)) return "#minecraft:pickaxes";
        if (tool.isIn(net.minecraft.registry.tag.ItemTags.AXES)) return "#minecraft:axes";
        if (tool.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS)) return "#minecraft:shovels";
        if (tool.isIn(net.minecraft.registry.tag.ItemTags.HOES)) return "#minecraft:hoes";
        if (tool.isOf(net.minecraft.item.Items.SHEARS)) return "minecraft:shears";
        return null;
    }

    private static boolean isBlockAllowed(Block block, ItemStack tool) {
        String toolKey = getToolTypeKey(tool);
        if (toolKey == null) return false;
        java.util.List<String> allowed = ConfigManager.config.allowed_blocks.get(toolKey);
        if (allowed == null) return false;
        String blockId = net.minecraft.registry.Registries.BLOCK.getId(block).toString();
        return allowed.contains(blockId);
    }
}

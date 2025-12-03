package io.sneery.detominer;

import java.util.*;

public class DetominerConfig {
    public boolean disable_detonate_when_sneaking = true;
    public boolean only_mine_with_proper_tool = true;
    public BlockBreak block_break = new BlockBreak();
    public Map<String, List<String>> allowed_blocks = new LinkedHashMap<>();

    public DetominerConfig() {
        allowed_blocks.put("#minecraft:pickaxes", Arrays.asList(
                "minecraft:stone",
                "minecraft:andesite",
                "minecraft:granite",
                "minecraft:diorite",
                "minecraft:deepslate",
                "minecraft:tuff",
                "minecraft:netherrack",
                "minecraft:blackstone",
                "minecraft:crimson_nylium",
                "minecraft:warped_nylium",
                "minecraft:basalt",
                "minecraft:end_stone",
                "minecraft:obsidian",
                "minecraft:coal_ore",
                "minecraft:deepslate_coal_ore",
                "minecraft:iron_ore",
                "minecraft:deepslate_iron_ore",
                "minecraft:copper_ore",
                "minecraft:deepslate_copper_ore",
                "minecraft:gold_ore",
                "minecraft:deepslate_gold_ore",
                "minecraft:redstone_ore",
                "minecraft:deepslate_redstone_ore",
                "minecraft:emerald_ore",
                "minecraft:deepslate_emerald_ore",
                "minecraft:lapis_ore",
                "minecraft:deepslate_lapis_ore",
                "minecraft:diamond_ore",
                "minecraft:deepslate_diamond_ore",
                "minecraft:nether_gold_ore",
                "minecraft:nether_quartz_ore",
                "minecraft:sandstone",
                "minecraft:red_sandstone",
                "minecraft:dripstone_block",
                "minecraft:raw_iron_block",
                "minecraft:raw_gold_block",
                "minecraft:raw_copper_block",
                "minecraft:terracotta",
                "minecraft:red_terracotta",
                "minecraft:orange_terracotta",
                "minecraft:yellow_terracotta",
                "minecraft:brown_terracotta",
                "minecraft:white_terracotta",
                "minecraft:light_gray_terracotta",
                "minecraft:magma_block"
        ));
        allowed_blocks.put("#minecraft:axes", Arrays.asList(
                ""
        ));
        allowed_blocks.put("#minecraft:shovels", Arrays.asList(
                "minecraft:dirt",
                "minecraft:grass_block",
                "minecraft:gravel",
                "minecraft:sand",
                "minecraft:clay",
                "minecraft:coarse_dirt",
                "minecraft:mud",
                "minecraft:mycelium",
                "minecraft:podzol",
                "minecraft:red_sand",
                "minecraft:snow_block",
                "minecraft:soul_sand",
                "minecraft:soul_soil"
        ));
        allowed_blocks.put("#minecraft:hoes", Arrays.asList(
                ""
        ));
        allowed_blocks.put("minecraft:shears", Arrays.asList(
                ""
        ));
    }

    public static class BlockBreak {
        public Level detonate_level_1 = new Level(3, 3, 1);
        public Level detonate_level_2 = new Level(3, 3, 2);
        public Level detonate_level_3 = new Level(3, 3, 3);
    }

    public static class Level {
        public int x, y, z;
        public Level(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public Level() {}
    }
}

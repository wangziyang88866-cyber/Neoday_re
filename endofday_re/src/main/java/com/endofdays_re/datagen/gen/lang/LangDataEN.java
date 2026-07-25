package com.endofdays_re.datagen.gen.lang;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;


public class LangDataEN extends LanguageProvider {
    public LangDataEN(PackOutput output, String locale) {
        super(output, ModUtils.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        // --- [ 1. Core AI Behavior & Day Stage Config ] ---
        add("endofdays_re.day.tooltip", "Enable Time");
        add("endofdays_re.end.day.tooltip", "End Time");
        add("endofdays_re.day.game.enable", "Game Enabled");
        add("endofdays_re.day.entity.attribute", "Entity Attributes");
        add("endofdays_re.day.entity.goal.place_block", "Place Block Goal");
        add("endofdays_re.day.entity.goal.break_block", "Break Block Goal");
        add("endofdays_re.day.entity.goal.follow", "Follow Goal");
        add("endofdays_re.day.entity.goal.place_tnt", "Place TNT Goal");
        add("endofdays_re.day.entity.goal.use.fishing", "Use Fishing Rod Goal");
        add("endofdays_re.day.entity.goal.use.trident", "Use Trident Goal");
        add("endofdays_re.day.entity.goal.use.bow", "Use Bow Goal");
        add("endofdays_re.day.entity.goal.use.shield", "Use Shield Goal");
        add("endofdays_re.day.entity.goal.use.place_fluid", "Place Fluid Goal");
        add("endofdays_re.day.entity.immune.lava", "Immune to Lava");
        add("endofdays_re.day.entity.goal.use.jump", "Jump Goal");
        add("endofdays_re.day.entity.rebirth", "Rebirth");
        add("endofdays_re.day.entity.replace", "Replace Target");
        add("endofdays_re.day.entity.immune.campfire", "Immune to Campfire");
        add("endofdays_re.day.entity.immune.asphyxia", "Immune to Suffocation");
        add("endofdays_re.day.entity.goal.use.pearls", "Use Ender Pearl Goal");
        add("endofdays_re.day.entity.spawn.spawn_tacz", "Spawn Gun-Wielding Zombie");
        add("endofdays_re.day.entity.spawn.spawn_fly", "Ride Phantom");
        add("endofdays_re.day.entity.use.ride", "Use Zombie Stack");
        add("endofdays_re.day.entity.use.dispenser", "Use Dispenser");
        add("endofdays_re.day.entity.immune.sun", "Immune to Sunlight");
        add("endofdays_re.day.entity.spawn.spawn_equip", "Equip Armor");
        add("endofdays_re.day.entity.use.potions", "Throw Potions");
        add("endofdays_re.day.entity.bark.barker_vehicle", "Destroy Vehicle [Boat - Minecart]");
        add("endofdays_re.day.entity.spawn.entity_climb", "Wall Climbing");
        add("endofdays_re.day.entity.use.picked_target_container", "Steal Container Items");
        add("endofdays_re.day.entity.use.break_target_block", "Break Target Block");
        add("endofdays_re.day.entity.spawn.spawn_gigantic", "Giant Spawn Chance");
        add("endofdays_re.day.level.enable_temp", "World Desolation Activation Time");

        // --- [ 2. Enable/Disable Config Switches (Config Tooltips) ] ---
        add("endofdays_re.enable.entity.goal", "Enable AI");
        add("endofdays_re.enable.entity.attribute", "Enable Attributes");
        add("endofdays_re.enable.entity.use.place_block", "Enable Place Block");
        add("endofdays_re.enable.entity.use.place_tnt", "Enable Place TNT");
        add("endofdays_re.enable.entity.use.place_fluid", "Enable Place Fluid");
        add("endofdays_re.enable.entity.fly", "Enable Flight");
        add("endofdays_re.enable.entity.target", "Enable Target");
        add("endofdays_re.enable.entity.follow", "Enable Follow");
        add("endofdays_re.enable.entity.jump", "Enable Jump");
        add("endofdays_re.enable.replace.entity.drop", "Enable Drop");
        add("endofdays_re.enable.replace.entity", "Enable Replace");
        add("endofdays_re.enable.entity.use.fishing", "Enable Fishing Rod Use");
        add("endofdays_re.enable.entity.use.trident", "Enable Trident Use");
        add("endofdays_re.enable.entity.use.shield", "Enable Shield Use");
        add("endofdays_re.enable.entity.use.bow", "Enable Bow Use");
        add("endofdays_re.enable.entity.immune.lava", "Enable Lava Immunity");
        add("endofdays_re.enable.entity.immune.campfire", "Enable Campfire Immunity");
        add("endofdays_re.enable.entity.immune.asphyxia", "Enable Suffocation Immunity");
        add("endofdays_re.enable.entity.rebirth", "Enable Rebirth");
        add("endofdays_re.enable.entity.goal.use.pearls", "Enable Ender Pearl Use");
        add("endofdays_re.enable.entity.gigantic_follow", "Enable Giant Zombie Follow Target");
        add("endofdays_re.enable.entity.immune.sun", "Enable Sunlight Immunity");
        add("endofdays_re.enable.spawn.replace_entity", "Enable Entity Replacement");
        add("endofdays_re.enable.entity.use.break_block", "Enable Block Breaking");
        add("endofdays_re.enable.entity.use.dispenser_enable", "Enable Dispenser");
        add("endofdays_re.enable.entity.use.potions_enable", "Enable Potion Throwing");
        add("endofdays_re.enable.entity.use.ride_enable", "Enable Entity Stacking");
        add("endofdays_re.enable.spawn.enable_spawn", "Enable Entity Spawning");
        add("endofdays_re.enable.entity.spawn.spawn_tacz_enable", "Enable Gun-Wielding Zombie Spawn");
        add("endofdays_re.enable.entity.bark.barker_vehicle_enable", "Enable Vehicle Destruction [Minecart/Boat]");
        add("endofdays_re.enable.entity.spawn.gigantic_enable", "Enable Giant Zombie Spawn");
        add("endofdays_re.enable.entity.equip", "Enable Equipment");
        add("endofdays_re.enable.spawn.entity_climb", "Enable Wall Climbing");
        add("endofdays_re.enable.use.break_target_block", "Enable Target Block Breaking");
        add("endofdays_re.enable.use.picked_target_container", "Enable Target Container Stealing");
        add("endofdays_re.enable.level.enable_temp", "Enable World Desolation");

        // --- [ 3. Probability, Range & General Numerical Config ] ---
        add("endofdays_re.lang.probability", "Probability");
        add("endofdays_re.lang.tag", "Tag");
        add("endofdays_re.lang.item.id", "Item ID");
        add("endofdays_re.lang.attribute.id", "Attribute ID");
        add("endofdays_re.lang.entity.ids", "Entity ID List");
        add("endofdays_re.common.value.tooltip", "Value");
        add("endofdays_re.common.min.tooltip", "Min Value");
        add("endofdays_re.common.max.tooltip", "Max Value");
        add("endofdays_re.common.follow_range", "Follow Range");
        add("endofdays_re.common.use.probability.tnt", "Use TNT Probability");
        add("endofdays_re.common.spawner.probability.tnt_zombie", "Spawn TNT Zombie Probability");
        add("endofdays_re.common.use.probability.fishing", "Use Fishing Rod Probability");
        add("endofdays_re.common.spawner.probability.fishing_zombie", "Spawn Fishing Rod Zombie Probability");
        add("endofdays_re.common.use.probability.trident", "Use Trident Probability");
        add("endofdays_re.common.spawner.probability.trident_zombie", "Spawn Trident Zombie Probability");
        add("endofdays_re.common.use.probability.bow", "Use Bow Probability");
        add("endofdays_re.common.spawner.probability.bow_zombie", "Spawn Bow Zombie Probability");
        add("endofdays_re.common.use.probability.shield", "Use Shield Probability");
        add("endofdays_re.common.spawner.probability.shield_zombie", "Spawn Shield Zombie Probability");
        add("endofdays_re.common.use.probability.pearls", "Use Ender Pearl Probability");
        add("endofdays_re.common.spawner.probability.pearls", "Spawn Ender Pearl Zombie Probability");
        add("endofdays_re.common.probability.jump", "Zombie Jump Probability");
        add("endofdays_re.common.float.title", "General Range Config");
        add("endofdays_re.common.int.title", "General Other Config");
        add("endofdays_re.common.spawner.place_block_zombie", "Spawn Block-Holding Zombie Probability");
        add("endofdays_re.common.spawner.tacz", "Spawn Gun-Wielding Zombie Probability");
        add("endofdays_re.common.spawner.dispenser", "Spawn Dispenser Zombie Probability");
        add("endofdays_re.common.spawner.ride", "Spawn Stacker Probability");
        add("endofdays_re.common.spawner.break", "Spawn Pickaxe Zombie Probability");

        // --- [ 4. HUD, Message & UI Translations ] ---
        add("endofday.screen.enable", "Show HUD");
        add("endofday.screen.enable.join", "Show Join Message");
        add("endofday.screen.join.key", "Message");
        add("endofday.screen.x", "Screen X Coordinate");
        add("endofday.screen.y", "Screen Y Coordinate");
        add("endofdays_re.join.key", "<values:[【End of Days】 :You have successfully loaded the mod. If you encounter any issues, please contact me in the group. Group number: 680332596\n:【Default config screen key is: J key:】],colors:[#A8E6CF:#FFB6C1:#FFD700:#9370DB:#FFD700]>");
        add("endofdays_re.join.key_buttom", "<values:[➤:Configure Mod],colors:[#ffffff:#FFD700],click:[/endofdays_re screen set config],hover:[Click to open config screen],bold:[true]>");
        add("endofdays_re.join.key_buttom_1", "<values:[➤:Modify Days],colors:[#ffffff:#FFD700],click:[/endofdays_re screen set day],hover:[Click to open UI],bold:[true]>");
        add("endofdays_re.lang.msg", "Output Message");
        add("endofdays_re.lang.pre", "Prerequisite");
        add("endofdays_re.lang.mode", "Output Mode");
        add("endofdays_re.lang.weight", "Weight");
        add("endofdays_re.day.time", "Trigger Tick");
        add("endofdays_re.screen.title", "End of Days Configuration");
        add("endofdays_re.key.screen", "Open Config Screen");
        add("endofdays_re.hud.day", "Day: %s");
        add("endofdays_re.hud.money", "Balance: ");
        add("endofdays_re.hud.currency_unit", " CR");
        add("endofdays_re.hud.low_balance", "Insufficient Balance!");

        // --- [ 5. Event Alert Messages ] ---
        add("endofdays_re.event.moon.msg", "<values:[A Blood Moon rises],colors:[#8B0000]>");
        add("endofdays_re.event.next.moon.msg", "<values:[A Blood Moon will occur tonight. Be careful.],colors:[#8B0000]>");
        add("endofdays_re.event.moon.msg.1", "<values:[The Blood Moon sets, the undead burn away.],colors:[#DCDCDC]>");
        add("endofdays_re.event.next.moon.title", "<values:[Blood Moon Warning],colors:[#8B0000]>");
        add("endofdays_re.event.day.msg", "<values:[This is your ${day} day of survival.],colors:[#DCDCDC:#FFFFFF:#DCDCDC]>");
        add("endofdays_re.event.neight.msg", "<values:[Night has fallen. You should find a safe shelter.],colors:[#FFFFFF]>");
        add("message." + ModUtils.MODID + ".heavy_injury_active", "§cYou are seriously injured! Healing is greatly reduced.");
        add("endofdays_re.message.healing_reduced", "I feel very weak...");

        // --- [ 6. Medical Items & Potion Effects (Complete) ] ---
        add("item.endofdays_re.quicksand_bucket", "Liquid Quicksand Bucket");
        add("item.endofdays_re.bandage", "Bandage");
        add("item.endofdays_re.standard_medkit", "Medkit");
        add("item.endofdays_re.medical_bandage", "Medical Bandage");
        add("message.bandage.healed", "The bandage treated your wound");
        add("tooltip.bandage.description", "Basic medical supplies for emergency treatment");
        add("tooltip.bandage.heal_amount", "Heal Amount: %s points");
        add("tooltip.bandage.use_time", "Use Time: %s seconds");
        add("message.medical_bandage.healed", "The medical bandage restored %s health");
        add("message.medical_bandage.stopped_bleeding", "The medical bandage stopped the severe bleeding");
        add("message.medical_bandage.cleared_effects", "The medical bandage cleared all negative effects");
        add("message.medical_bandage.no_need", "You do not need to use a medical bandage right now");
        add("tooltip.medical_bandage.description", "Advanced medical supplies, providing comprehensive treatment effects");
        add("tooltip.medical_bandage.heal_amount", "Instant Heal: %s health");
        add("tooltip.medical_bandage.clears_effects", "Clears all negative status effects");
        add("tooltip.medical_bandage.regeneration", "Provides multi-level regeneration effect");
        add("tooltip.medical_bandage.absorption", "Grants absorption and resistance");
        add("tooltip.medical_bandage.use_time", "Use Time: %s seconds");

        add("effect." + ModUtils.MODID + ".bleeding", "Bleeding");
        add("effect." + ModUtils.MODID + ".stun", "Stun");
        add("effect." + ModUtils.MODID + ".fracture", "Fracture");
        add("effect." + ModUtils.MODID + ".lacerate", "Laceration");
        add("effect." + ModUtils.MODID + ".heavy_injury", "Heavy Injury");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.description", "§4[ Critical Injury ]");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.ability_1", "§cAnti-Heal: Greatly weakens or suppresses natural regeneration and potion effects");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.ability_2", "§cVulnerability: All damage taken is significantly increased");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.detail", "The wound reaches deep into the bone; simple bandaging is no longer effective.");


        // --- [ 8. Special Block & Structure Translations ] ---
        add("block." + ModUtils.MODID + ".corpse_zombie", "Zombie Corpse");
        add("container.corpse_zombie", "Zombie Corpse");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.description", "§7A highly decomposed carcass, seemingly still holding the remnants of its former life.");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_1", "§6Loot: Right-click to open inventory and scavenge supplies");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_2", "§cHazard: Looting has a chance to awaken the corpse");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_3", "§eClean: Use a flint & steel, fire charge, or TNT to completely incinerate it");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.detail", "§4Warning: Corpses left unattended for too long may mutate and rise again.");
        add("block." + ModUtils.MODID + ".barbed_wire_fence", "Barbed Wire Fence");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.description", "§7A metal defensive structure with sharp barbs.");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_1", "§cBarbs: Causes percentage damage based on max health when passing through");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_2", "§4Infection: High chance to cause bleeding, low chance for laceration or heavy injury");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_3", "§eImpediment: Significantly reduces movement speed when passing through");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.detail", "§8\"It won't stop everyone, but it will make everyone slow down and bleed out.\"");
        add("block." + ModUtils.MODID + ".spike_block", "Steel Spike");
        add("tooltip." + ModUtils.MODID + ".spike_block.description", "§7A crude but deadly trap.");
        add("tooltip." + ModUtils.MODID + ".spike_block.ability_1", "§cImpale: Deals percentage damage based on max health to entities standing on it");
        add("tooltip." + ModUtils.MODID + ".spike_block.ability_2", "§eIgnores Armor: The spike's damage affects the body directly");
        add("tooltip." + ModUtils.MODID + ".spike_block.detail", "§8\"Don't look down; the danger at your feet is far more lethal.\"");

        // --- [ 9. Comprehensive Invasion Config System ] ---
        add("config.endofdays_re.category.invasion", "Environmental Evolution: Invasion Config System");

        // --- [ 10. Custom Spawner System ] ---
        add("config.endofdays_re.category.spawner", "Custom Spawner System");
        add("config.endofdays_re.spawner.enable", "Enable Custom Spawner");
        add("config.endofdays_re.spawner.check_interval", "Check Interval (Ticks)");
        add("config.endofdays_re.spawner.max_groups", "Max Groups");
        add("config.endofdays_re.spawner.max_per_group", "Max per Group");
        add("config.endofdays_re.spawner.max_total_entities", "Max Total Entities (-1 for unlimited)");
        add("config.endofdays_re.spawner.spawn_range", "Horizontal Spawn Range");
        add("config.endofdays_re.spawner.spawn_range.min", "Min Horizontal Distance (blocks)");
        add("config.endofdays_re.spawner.spawn_range.max", "Max Horizontal Distance (blocks)");
        add("config.endofdays_re.spawner.vertical_range", "Vertical Spawn Range");
        add("config.endofdays_re.spawner.vertical_range.min", "Min Vertical Distance (blocks)");
        add("config.endofdays_re.spawner.vertical_range.max", "Max Vertical Distance (blocks)");
        add("config.endofdays_re.spawner.spawn_time", "Active Day Range");
        add("config.endofdays_re.spawner.spawn_time.start", "Start Day (from which day)");
        add("config.endofdays_re.spawner.spawn_time.end", "End Day (-1 for unlimited)");
        add("config.endofdays_re.spawner.allowed_dimensions", "Dimension Whitelist");
        add("config.endofdays_re.spawner.entity_configs", "Entity Config List");
        add("config.endofdays_re.spawner.entity.key", "Config Key");
        add("config.endofdays_re.spawner.entity.entity_id", "Entity ID");
        add("config.endofdays_re.spawner.entity.weight", "Spawn Weight");
        add("config.endofdays_re.spawner.entity.nbt_tag", "NBT Tag");
        add("config.endofdays_re.spawner.entity.attributes", "Attribute Config");
        add("config.endofdays_re.spawner.attribute.header", "Attribute Config Item");
        add("config.endofdays_re.spawner.attribute.id", "Attribute ID");
        add("config.endofdays_re.spawner.attribute.formula", "Formula");
        add("config.endofdays_re.spawner.entity.equipments", "Equipment Config");
        add("config.endofdays_re.spawner.equipment.header", "Equipment Config Item");
        add("config.endofdays_re.spawner.equipment.item_id", "Item ID");
        add("config.endofdays_re.spawner.equipment.slot", "Equipment Slot");
        add("config.endofdays_re.spawner.equipment.probability", "Equip Probability");
        add("config.endofdays_re.invasion.max_time", "Global Invasion Cooldown (24000 = 1 day)");
        add("config.endofdays_re.invasion.list", "Registered Invasion Events List");
        add("config.endofdays_re.common.key", "Unique Key");
        add("config.endofdays_re.invasion.weight", "Spawn Weight");
        add("config.endofdays_re.invasion.dim", "Effective Dimensions");
        add("config.endofdays_re.invasion.max_entity", "Max Entities per Invasion");
        add("config.endofdays_re.invasion.pos_max", "Max Spawn Point Attempts");
        add("config.endofdays_re.common.probability", "Trigger Success Probability (0.0-1.0)");
        add("config.endofdays_re.common.min_waves", "Min Waves per Invasion");
        add("config.endofdays_re.common.max_waves", "Max Waves per Invasion");
        add("config.endofdays_re.invasion.pos_range", "Spawn Position Config Layer");
        add("config.endofdays_re.invasion.pos_range.min", "Spawn Position: Min Radius");
        add("config.endofdays_re.invasion.pos_range.max", "Spawn Position: Max Radius");
        add("config.endofdays_re.invasion.time_range", "Trigger Time Window Layer");
        add("config.endofdays_re.invasion.time_range.min", "Trigger Window: Start Tick");
        add("config.endofdays_re.invasion.time_range.max", "Trigger Window: End Tick");
        add("config.endofdays_re.invasion.entities", "Entity Configuration List");
        add("config.endofdays_re.entity.id", "Entity ID");
        add("config.endofdays_re.entity.tag", "Extra NBT Tags (JSON)");
        add("config.endofdays_re.entity.count", "Wave Count Control Layer");
        add("config.endofdays_re.entity.count.min", "Min Spawn per Wave");
        add("config.endofdays_re.entity.count.max", "Max Spawn per Wave");
        add("config.endofdays_re.entity.effects", "Entity Potion Effect List");
        add("config.endofdays_re.effect.header", "Potion Effect Specific Config");
        add("config.endofdays_re.effect.id", "Effect ID");
        add("config.endofdays_re.effect.time", "Potion Duration Layer");
        add("config.endofdays_re.effect.time.min", "Effect Duration: Min");
        add("config.endofdays_re.effect.time.max", "Effect Duration: Max");
        add("config.endofdays_re.common.min_lv", "Min Effect Level (0 is Level I)");
        add("config.endofdays_re.common.max_lv", "Max Effect Level");
        add("config.endofdays_re.effect.show_particle", "Show Potion Particles");
        add("config.endofdays_re.entity.attributes", "Entity Attribute Modification List");
        add("config.endofdays_re.attribute.header", "Attribute Bonus Config");
        add("config.endofdays_re.attribute.id", "Attribute Name (e.g., generic.max_health)");
        add("config.endofdays_re.attribute.evl", "Attribute Formula (EVL String)");
        add("config.endofdays_re.entity.armor", "Entity Equipment / Held Item List");
        add("config.endofdays_re.armor.header", "Equipment Item Config");
        add("config.endofdays_re.armor.id", "Equipment Item ID");
        add("config.endofdays_re.armor.slot", "Equipment Slot");
        add("config.endofdays_re.armor.on_drop", "Allow Drop on Death");
        add("config.endofdays_re.armor.durability", "Item Durability (0-100)");

        // --- [ 10. Various Module Configurations (Armor, Attr, Market, Drop) ] ---
        add("config.endofdays_re.title", "End of Days - Modular Configuration");
        add("config.endofdays_re.category.armor", "Zombie Armor Config");
        add("config.endofdays_re.armor_list", "Armor Config List");
        add("config.endofdays_re.armor_spawn_max", "Max Armor Attempts");
        add("config.endofdays_re.armor.chance", "Spawn Chance (0.0-1.0)");
        add("config.endofdays_re.armor.enchanted", "Allow Enchantment");
        add("config.endofdays_re.armor.day", "Start Day");
        add("config.endofdays_re.armor.end_day", "End Day");
        add("config.endofdays_re.armor.tag", "NBT Tag (JSON)");
        add("config.endofdays_re.armor.enchants", "Enchantment Config Table");
        add("config.endofdays_re.enchant.id", "Enchantment ID");
        add("config.endofdays_re.enchant.chance", "Enchantment Probability");
        add("config.endofdays_re.enchant.min_level", "Min Level");
        add("config.endofdays_re.enchant.max_level", "Max Level");

        add("config.endofdays_re.category.attribute", "Entity Attribute Boost Config");
        add("config.endofdays_re.attribute_list", "Attribute Adjustment List");
        add("config.endofdays_re.attr.key", "Config Key");
        add("config.endofdays_re.attr.id", "Attribute ID");
        add("config.endofdays_re.attr.entity_id", "Target Entity ID");
        add("config.endofdays_re.attr.value", "Boost Formula/Value");
        add("config.endofdays_re.attr.value.tooltip", "Supports variables: BASE_HEALTH, day, etc.");
        add("config.endofdays_re.attr.start", "Start Day");
        add("config.endofdays_re.attr.end", "End Day");
        add("config.endofdays_re.attr.max_limit", "Attribute Boost Cap");

        add("config.endofdays_re.category.market", "Black Market Config");
        add("config.endofdays_re.market_list", "Item List");
        add("config.endofdays_re.market.key", "Unique Key");
        add("config.endofdays_re.market.id", "Item ID");
        add("config.endofdays_re.market.count", "Transaction Quantity");
        add("config.endofdays_re.market.price", "Base Price (Coins)");
        add("config.endofdays_re.market.limit", "Stock Limit (-1 for infinite)");
        add("config.endofdays_re.market.info", "Item Display Name (Supports § color codes)");
        add("config.endofdays_re.market.mode", "Transaction Mode (Buy/Sell)");
        add("config.endofdays_re.market.weight", "Random Weight");

        add("config.endofdays_re.category.drop_living", "Mob Drops (Direct Death)");
        add("config.endofdays_re.category.drop_corpse", "Corpse Loot (Right-click Corpse)");
        add("config.endofdays_re.drop_list", "Drop Config List");
        add("config.endofdays_re.drop.lang", "Category Display Name");
        add("config.endofdays_re.drop.entities", "Affected Entity List");
        add("config.endofdays_re.drop.day", "Start Day");
        add("config.endofdays_re.drop.end", "End Day");
        add("config.endofdays_re.drop.items", "Drop Item Details");
        add("config.endofdays_re.item.lang", "Item Name");
        add("config.endofdays_re.item.id", "Item ID");
        add("config.endofdays_re.item.weight", "Random Weight");
        add("config.endofdays_re.item.min", "Min Quantity");
        add("config.endofdays_re.item.max", "Max Quantity");
        add("config.endofdays_re.item.chance", "Extra Drop Chance (0-1)");
        add("config.endofdays_re.item.tag", "NBT Tag");

        add("config.endofdays_re.common.scan_interval", "Block Scan Interval (seconds)");
        add("config.endofdays_re.common.temperature", "Current World Base Temperature");
        add("config.endofdays_re.common.max_money", "Player Money Cap");
        add("config.endofdays_re.common.use_currency", "Enable Currency System");
        add("config.endofdays_re.common.smelt_blacklist", "Lumic Smelter Blacklist");

        add("config.endofdays_re.category.probability", "Spawn/Trigger/Other Settings");
        add("config.endofdays_re.common.probability_list", "Trigger Probability Details List");
        add("config.endofdays_re.common.value", "Current Value/Expression");
        add("config.endofdays_re.common.min", "Allowed Minimum");
        add("config.endofdays_re.common.max", "Allowed Maximum");

        add("config.endofdays_re.common.target_list", "Target Redirection");
        add("config.endofdays_re.target.mob", "Attacker Entity ID");
        add("config.endofdays_re.target.victim", "Target Entity ID");

        add("config.endofdays_re.category.limit_setting", "Limits & Death Penalty Config");
        add("config.endofdays_re.common.limit_percent", "Hardcore Mode Life-Saving Fee Ratio (0-1)");
        add("config.endofdays_re.common.limit_min_cost", "Life-Saving Min Balance Requirement");
        add("config.endofdays_re.common.normal_death_cost", "Normal Mode Death Penalty Ratio (0-1)");

        add("config.endofdays_re.common.replace_map", "Entity Spawn Replacement Map / Block Break AI Blacklist");
        add("config.endofdays_re.common.replace_list", "Global Entity Replacement List");
        add("config.endofdays_re.replace.original", "Original Entity ID");
        add("config.endofdays_re.replace.target", "Replacement Entity ID");
        add("config.endofdays_re.common.ban_list", "Block Break/Interact Blacklist");
        add("config.endofdays_re.common.sync_interval", "Packet Sync Interval (Ticks)");
        add("config.endofdays_re.common.default_money", "Player Initial Money");
        add("config.endofdays_re.common.market_max_count", "Black Market Daily Item Limit (-1 for all)");

        add("config.endofdays_re.category.ai", "Zombie Behavior AI Settings");
        add("config.endofdays_re.common.block_break_list", "Active Block Break List");
        add("config.endofdays_re.common.block_break.tooltip", "Supports regex: prefix for regex pattern matching.");
        add("config.endofdays_re.common.equip_chest_list", "Steal/Open Container Config");

        add("config.endofdays_re.category.tacz", "TACZ Gun Extension Config");
        add("config.endofdays_re.tacz.id", "Gun Item ID");
        add("config.endofdays_re.tacz.fire_mode", "Fire Mode (AUTO/SEMI/BURST)");
        add("config.endofdays_re.tacz.ammo", "Check Ammo (Consume Ammo)");
        add("config.endofdays_re.tacz.weight", "Zombie Equip Weight");
        add("config.endofdays_re.tacz.radius", "Max Perception/Range Radius");
        add("config.endofdays_re.tacz.speed", "Movement Speed Multiplier when Holding Gun");
        add("config.endofdays_re.tacz.attack_speed", "Zombie Shooting Interval (Ticks)");

        add("config.endofdays_re.category.day_settings", "Day Stage Config");
        add("config.endofdays_re.day_list", "Function Progression List");
        add("config.endofdays_re.day.key", "Internal Key");
        add("config.endofdays_re.day.start", "Start Day");
        add("config.endofdays_re.day.end", "End Day");
        add("config.endofdays_re.day.lang_key", "Function Translation Key");
        add("config.endofdays_re.day.default_value", "Default Value");

        add("config.endofdays_re.category.bloodmoon", "Blood Moon & Dimension Settings");
        add("config.endofdays_re.bloodmoon.enable", "Enable Blood Moon System");
        add("config.endofdays_re.bloodmoon.weight", "Blood Moon Initial Weight Modifier");
        add("config.endofdays_re.bloodmoon.probability", "Daily Probability Increment");
        add("config.endofdays_re.bloodmoon.chat_show", "Blood Moon Start Message (Chat)");
        add("config.endofdays_re.bloodmoon.sleep", "Allow Sleep During Blood Moon");
        add("config.endofdays_re.bloodmoon.spawn_weight", "Blood Moon Spawn Rate Multiplier");

        add("config.endofdays_re.category.module_enable", "Game Module Master Control");
        add("config.endofdays_re.common.enable", "Module Status (On/Off)");
        add("config.endofdays_re.common.lang_key", "Internal Translation Key");
        add("config.endofdays_re.common.default_value", "Mod Default Status");

        // --- [ Miscellaneous ] ---
        add("endofdays_re.drop.add.success", "Item added successfully");
        add("endofdays_re.bp.blood.msg", "<values:[Blood Moon Chance: ${bp}],colors:[#DCDCDC:map(${bp}>0.15,#90EE90,#FFFFFF):#DCDCDC]>");

        // --- UI Categories & Globals ---
        add("config.endofdays_re.category.screen", "Screen & Particle Effects");
        add("config.endofdays_re.screen.showHud", "§bShow HUD Info");
        add("config.endofdays_re.screen.isShowJoin", "Show Join Server Message");
        add("config.endofdays_re.screen.isTitleShow", "Show Centered Title");
        add("config.endofdays_re.screen.joinTime", "Title Fade-in Duration (Ticks)");
        add("config.endofdays_re.screen.ShowTime", "Title Stay Duration (Ticks)");
        add("config.endofdays_re.screen.OutTime", "Title Fade-out Duration (Ticks)");
        add("config.endofdays_re.screen.TitleNightShow", "Show Title at Night");
        add("config.endofdays_re.screen.showParticles", "Enable Potion/Environment Particles");
        add("config.endofdays_re.screen.showDamage", "Enable Damage Indicator");
        add("config.endofdays_re.screen.showHeal", "Enable Heal Indicator");
        add("config.endofdays_re.screen.entityBlacklist", "Entity Blacklist for Indicators");

        // --- Subcategory Titles ---
        add("config.endofdays_re.screen.sub.heal", "§a✚ Heal Style Config");
        add("config.endofdays_re.screen.sub.dmg", "§c⚔ Damage Style Config");

        // --- Heal Style Details (HealStyleConfig) ---
        add("config.endofdays_re.screen.heal.mainColor", "Heal Main Color");
        add("config.endofdays_re.screen.heal.prefix", "Heal Prefix (e.g., +)");
        add("config.endofdays_re.screen.heal.prefixColor", "Prefix Color");
        add("config.endofdays_re.screen.heal.playerSuffix", "Player Heal Suffix (e.g., HP)");
        add("config.endofdays_re.screen.heal.enableBold", "Bold Heal Numbers");

        add("config.endofdays_re.screen.dmg.enableBold", "Bold Damage Numbers (Global)");
        add("config.endofdays_re.screen.dmg.showCrit", "Show Critical Hit Indicator");
        add("config.endofdays_re.screen.dmg.critColor", "Critical Hit Text Color");
        add("config.endofdays_re.screen.dmg.critPrefix", "Critical Hit Prefix Text");
        add("config.endofdays_re.screen.dmg.critSuffix", "Critical Hit Suffix Text");

        add("config.endofdays_re.screen.dmg.showSource", "Show Damage Source Prefix (P/M)");
        add("config.endofdays_re.screen.dmg.pPrefix", "Player Damage Prefix (Default P)");
        add("config.endofdays_re.screen.dmg.mPrefix", "Mob Damage Prefix (Default M)");

        String[] attrs = {"fire", "magic", "light", "freeze", "wither", "fall", "arrow"};
        String[] attrNames = {"Fire", "Magic", "Lightning", "Freeze", "Wither", "Fall", "Ranged"};
        for (int i = 0; i < attrs.length; i++) {
            add("config.endofdays_re.screen.dmg.show." + attrs[i], "Show " + attrNames[i] + " Damage Style");
            add("config.endofdays_re.screen.dmg.color." + attrs[i], attrNames[i] + " Damage Main Color");
            add("config.endofdays_re.screen.dmg.suffix." + attrs[i], attrNames[i] + " Icon Suffix");
        }

        add("config.endofdays_re.screen.dmg.showBleeding", "Show Bleeding Effect (🩸)");
        add("config.endofdays_re.screen.dmg.bleedingColor", "Bleeding Text Color");
        add("config.endofdays_re.screen.dmg.showStun", "Show Stun Effect (💫)");
        add("config.endofdays_re.screen.dmg.stunPrefix", "Stun Text Prefix");
        add("config.endofdays_re.screen.dmg.showLacerate", "Show Laceration Effect (✖)");
        add("config.endofdays_re.screen.dmg.lacerateColor", "Laceration Text Color");
        add("config.endofdays_re.screen.dmg.laceratePrefix", "Laceration Text Prefix");
        add("config.endofdays_re.screen.dmg.showFracture", "Show Fracture Effect (🦴)");
        add("config.endofdays_re.screen.dmg.fractureColor", "Fracture Text Color");
        add("config.endofdays_re.screen.dmg.fractureSuffix", "Fracture Text Suffix");

        // Defaults
        add("config.endofdays_re.screen.dmg.defColor", "Normal Damage Default Color");
        add("config.endofdays_re.screen.dmg.defSuffix", "Normal Damage Default Suffix (⚔)");
        add("config.endofdays_re.category.common_main", "Base Settings");

        add("config.endofdays_re.category.economy", "Economy Settings");
        add("endofdays_re.common.spawner.spawn_tnt_zombie_ca", "Spawn TNT Zombie Probability");
        add("endofdays_re.day.level.spawn_tnt_zombie", "Spawn TNT Zombie");
        add("endofdays_re.enable.level.spawn_tnt_zombie", "Enable TNT Zombie");

        // Categories & Main Lists
        add("config.endofdays_re.spawner.rules_list", "Spawn Rule Definition List");

        // --- 1. Base Properties ---
        add("config.endofdays_re.spawner.rule_name", "Rule Name");
        add("config.endofdays_re.spawner.mobs", "Entity ID List to Spawn");
        add("config.endofdays_re.spawner.weights", "Corresponding Entity Weights (Must match IDs)");
        add("config.endofdays_re.spawner.tags", "Scoreboard Tags");
        add("config.endofdays_re.spawner.mobs_from_biome", "MobCategory Value");
        add("config.endofdays_re.spawner.mobs_from_bime_a", "Gets biome's mob group, then spawns from that group. Can leave empty.");

        // --- 2. Rate & Count ---
        add("config.endofdays_re.spawner.chance", "Spawn Probability per Second (0.0-1.0)");
        add("config.endofdays_re.spawner.attempts", "Attempts per Second");
        add("config.endofdays_re.spawner.min", "Min Spawn per Attempt");
        add("config.endofdays_re.spawner.max", "Max Spawn per Attempt");
        add("config.endofdays_re.spawner.group_distance", "Group Spread Distance (-1 default)");

        // --- 3. Environment & Distance Conditions ---
        add("config.endofdays_re.spawner.dims", "Effective Dimensions (Namespace:Path)");
        add("config.endofdays_re.spawner.min_dist", "Min Horizontal Distance (from player)");
        add("config.endofdays_re.spawner.max_dist", "Max Horizontal Distance (from player)");
        add("config.endofdays_re.spawner.v_min_dist", "Min Vertical Distance (-1 disabled)");
        add("config.endofdays_re.spawner.v_max_dist", "Max Vertical Distance (-1 disabled)");
        add("config.endofdays_re.spawner.min_day", "Start Survival Day");
        add("config.endofdays_re.spawner.max_day", "End Survival Day");
        add("config.endofdays_re.spawner.min_height", "Min Spawn Height (Y)");
        add("config.endofdays_re.spawner.max_height", "Max Spawn Height (Y)");

        // --- 4. Lighting Settings ---
        add("config.endofdays_re.spawner.check_block_light", "Check Block Light Level");
        add("config.endofdays_re.spawner.min_block_light", "Min Block Light Level");
        add("config.endofdays_re.spawner.max_block_light", "Max Block Light Level");
        add("config.endofdays_re.spawner.check_sky_light", "Check Sky Light Level");
        add("config.endofdays_re.spawner.min_sky_light", "Min Sky Light Level");
        add("config.endofdays_re.spawner.max_sky_light", "Max Sky Light Level");
        add("config.endofdays_re.spawner.is_night", "Only Spawn at Night");
        add("tooltip.config.is_light", "§eNight Only: §rWhen enabled, entities only spawn at night. §7(Note: Sky light checks are disabled when this is active)§r");
        add("tooltip.config.min_sky_light", "Min Sky Light: Limits the sky light level (0-15). 15 = fully open sky.");
        add("tooltip.config.max_sky_light", "Max Sky Light: Limits the sky light level (0-15). 0 = fully covered.");
        add("tooltip.config.sky_light_hint", "Hint: Sky light differs from block light; even at midnight, sky light is 15 in open areas.");

        // --- 5. Flags ---
        add("config.endofdays_re.spawner.sturdy", "Requires Solid Top Surface");
        add("config.endofdays_re.spawner.sturdy.tooltip", "When enabled, mobs only spawn on full solid blocks (e.g., full bricks, upside-down stairs), not on slabs or transparent blocks.");
        add("config.endofdays_re.spawner.valid_spawn", "Strict Collision Check (ValidSpawn)");
        add("config.endofdays_re.spawner.valid_spawn.tooltip", "Mimics vanilla spawn logic: prevents spawning on glass, fences, leaves, etc.");
        add("config.endofdays_re.spawner.force_surface", "Force Spawn on Surface");
        add("config.endofdays_re.spawner.in_liquid", "Must Spawn in Liquid (Water or Lava)");
        add("config.endofdays_re.spawner.in_water", "Must Spawn in Water");
        add("config.endofdays_re.spawner.no_restrictions", "Ignore All Restrictions (Force Spawn)");
        add("config.endofdays_re.spawner.in_lava", "Must Spawn in Lava");
        add("config.endofdays_re.spawner.in_air", "Must Spawn in Air (for flying mobs)");

        // --- 6. Caps ---
        add("config.endofdays_re.spawner.max_this", "Entity Cap for this Rule (-1 unlimited)");
        add("config.endofdays_re.spawner.max_local", "Entity Cap within 128-block Radius");
        add("config.endofdays_re.spawner.max_total", "Global Entity Cap");
        add("config.endofdays_re.spawner.max_hostile", "Hostile Mob Cap");
        add("config.endofdays_re.spawner.max_peaceful", "Passive Mob Cap");
        add("config.endofdays_re.spawner.max_neutral", "Neutral Mob Cap");
        add("config.endofdays_re.common.enable_stuck_day", "Enable Day Retention");
        add("config.endofdays_re.common.retention_interval", "Day Retention Interval");
        add("config.endofdays_re.common.enable_corpse", "Enable Corpse Spawning");
        add("config.endofdays_re.common.corpse_max", "Max Corpses per Area");

        add("gtm.endofdays_re.title", "Doomsday Trading Terminal");
        add("gtm.endofdays_re.button", "Refresh");
        add("gtm.endofdays_re.button_1", "Exit");
    }
}
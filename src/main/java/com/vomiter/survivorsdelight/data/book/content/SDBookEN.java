package com.vomiter.survivorsdelight.data.book.content;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.SDItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.data.book.SDPatchouliCategoryProvider;
import com.vomiter.survivorsdelight.data.book.SDPatchouliEntryProvider;
import com.vomiter.survivorsdelight.data.book.TFCGuide;
import com.vomiter.survivorsdelight.data.book.builder.CategoryJson;
import com.vomiter.survivorsdelight.data.book.builder.EntryJson;
import com.vomiter.survivorsdelight.data.book.builder.TextBuilder;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import vectorwing.farmersdelight.common.registry.ModItems;

public final class SDBookEN {
    public static final String LANG = "en_us";
    private SDBookEN() {}
    private static ResourceLocation getId(Item item){
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static void run(SDPatchouliCategoryProvider cats, SDPatchouliEntryProvider entries, GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(), cats);
        generator.addProvider(event.includeClient(), entries);
    }

    public static void accept(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        SDPatchouliCategoryProvider cats = new SDPatchouliCategoryProvider(output);
        SDPatchouliEntryProvider entries = new SDPatchouliEntryProvider(output);

        cats.setLang(LANG);
        entries.setLang(LANG);

        // Category
        var icon1 = getId(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER).get());
        cats.category(
                CategoryJson.builder("survivors_delight")
                        .setName("Survivor's Delight")
                        .setDescription("Survive in a Terrafirmacraft world with Farmer's Delight.")
                        .setIcon(icon1.toString())
                        .setSortnum(10)
                        .build()
        );

        int sortNum = 0;

        // Entry 1
        var text1_1 = TextBuilder.of("You can use a skillet to ").link("mechanics/heating", "heat things up.")
                .appendWithSpace("Skillets can be used as a placed block or as a held item.")
                .appendWithSpace("Skillets made of steel and better material can be used as a melee weapon to deal ").link(TFCGuide.Mechanics.MECHANICS_DAMAGE_TYPES, "crushing damage.");

        var text1_2 = TextBuilder.of("To use a skillet as a held item, hold a skillet in your main hand and a food ingredient in your off-hand, and stand close to a heat source.")
                .appendWithSpace("Then hold $(item)$(k:key.use)$() to heat one item from the ingredient stack.")
                .appendWithSpace("This process consumes the skillet's durability.");

        var text1_3 = TextBuilder.of("A placed skillet can hold up to 8 of the same ingredient and heat them all.")
                .appendWithSpace("This is a more efficient way to cook food.")
                .appendWithSpace("However, it may consume extra durability if the heat source temperature is close to the skillet's ").link(TFCGuide.Mechanics.MECHANICS_ANVILS_WORKING, "melting point");


        var text1_4 = TextBuilder.of("Valid metal material for a skillet includes ")
                        .thing("copper, cast iron, steel, black steel, red steel and blue steel.")
                        .appendWithSpace("A skillet made of better material is more durable.")
                        .appendWithSpace("A skillet made of ")
                        .thing("copper").appendWithSpace("requires extra steps before it becomes a usable cooking utensil.");

        var text1_5 = TextBuilder.of("A skillet stops being usable as a cooking utensil when only one point of durability remains.")
                        .appendWithSpace("A placed skillet will also drop as an item in this state.")
                        .appendWithSpace("Most of skillets can be reforged to restore their full durability, or melted back into their source material without any loss.")
                        .appendWithSpace("Unlike others, a copper skillet only needs re-welding with a layer of lining to become usable again.");

        entries.entry(
                EntryJson.builder("skillet")
                        .setName("Skillet")
                        .setCategory("tfc:survivors_delight")
                        .setIcon(icon1)
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .addTextPage(text1_1.toString())
                        .addTextPage(text1_2.toString())
                        .addTextPage(text1_3.toString())
                        .addSingleBlockPage("Skillet variants", "#survivorsdelight:skillets")

                    .addTextPage("Make A Skillet", "make_a_skillet", text1_4.toString())
                        .addAnvilRecipe(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "anvil/skillet_head/steel"), "A metal double sheet is forged into a skillet head.")
                        .addWeldingRecipe(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "welding/unfinished_skillet/steel"), "The skillet head is welded with a rod of the same metal.")
                        .addCraftingRecipe(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "crafting/skillet/steel"), SDUtils.RLUtils.build(SurvivorsDelight.MODID, "crafting/skillet/farmer"), "Assemble A Skillet")
                        .addAnvilRecipe(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "anvil/skillet_lining/silver"), "A special lining for copper skillets prevents toxic copper ions from leaching into food. This can be made with $(thing)silver$() or $(thing)tin$(). A copper skillet must be welded with a lining before it becomes a usable cooking utensil.")

                    .addTextPage("Repair A Skillet", "repair", text1_5.toString())
                        .extraRecipeMapping(SDTags.ItemTags.SKILLETS, 0)
                        .build()
        );

        // Entry 2
        entries.entry(
                EntryJson.builder("stove")
                        .setName("Stove")
                        .setCategory("tfc:survivors_delight")
                        .setIcon("farmersdelight:stove")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .addTextPage(
                                "A stove is an advanced way to harness $(l:mechanics/heating)heat$(). "
                                + "This block accepts $(thing)logs$(), $(thing)coal$(), and $(thing)charcoal$() as fuel for cooking or for providing heat to other cooking devices. "
                                + "When it's lit but not actively cooking, fuel consumption is minimal."
                        )
                        .addSingleBlockPage("Stove", "farmersdelight:stove")
                        .extraRecipeMapping(ModItems.STOVE, 0)
                        .build()
                        );

        // Entry 3
        TextBuilder text3_1 = TextBuilder.of("A cabinet is a wooden container used to preserve food and store cooking utensils.")
                                .appendWithSpace("Use $(item)$(k:key.use)$() on it with a ")
                                .link(TFCGuide.Mechanics.MECHANICS_LAMPS_TALLOW, "bucket of tallow")
                                .appendWithSpace("to treat it.")
                                .appendWithSpace("A treated cabinet can ")
                                .link(TFCGuide.Mechanics.MECHANICS_DECAY, "preserve food")
                                .appendWithSpace("for longer.");
        entries.entry(
                EntryJson.builder("cabinet")
                        .setName("Cabinet")
                        .setCategory("tfc:survivors_delight")
                        .setIcon("survivorsdelight:planks/cabinet/ash")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .addTextPage(text3_1.toString())
                        .addSingleBlockPage("Cabinet variants", "#survivorsdelight:cabinets")
                        .extraRecipeMapping(SDTags.ItemTags.CABINETS, 0)
                        .build()
        );

        // Entry 4
        var text4_1 = TextBuilder
                .of("A cooking pot is an alternative to the ceramic pot.")
                .appendWithSpace("This device is designed to process food more efficiently.")
                .appendWithSpace("It can cook soup and boil eggs faster.")
                .appendWithSpace("However, it cannot be used for recipes that produce fluid.")
                .appendWithSpace("Besides ceramic pot recipes, there are also several recipes exclusive to the metal cooking pot.");

        var text4_2 = TextBuilder
                .of("A bucket button is located on the left side of the cooking pot's interaction menu.")
                .appendWithSpace("The button opens a barrel‑like menu, letting you put fluid into the cooking pot.")
                .appendWithSpace("The fluid can later be used in a pot or cooking‑pot recipe.");

        var text4_3 = TextBuilder
                .of("Some cooking pot outputs require a food container, which is often a bowl or a glass bottle.")
                .appendWithSpace("Both ceramic and wooden bowls are valid for bowl foods.")
                .appendWithSpace("All four types of glass bottles are valid for bottled drinks as well.");

        entries.entry(
                EntryJson.builder("cooking_pot")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("Cooking Pot")
                        .setIcon("farmersdelight:cooking_pot")
                        .addTextPage(text4_1.toString())
                        .addSingleBlockPage("Cooking Pot", "farmersdelight:cooking_pot")
                        .addTextPage(text4_2.toString())
                        .addTextPage(text4_3.toString())
                        .extraRecipeMapping(ModItems.COOKING_POT, 0)
                        .build());

        //Entry 5
        var text5_1 = TextBuilder.of("Farmer's Delight dishes sometimes grant special effects that help you survive in the harsh world.")
                .appendWithSpace("Note that rotten dishes provide no beneficial effects.");
        var text5_2 = TextBuilder.of("Nourishment prevents your hunger and thirst from decreasing.")
                .appendWithSpace("However, it stops working if you take damage and natural passive healing is active.")
                .appendWithSpace("It resumes when you return to full health, or when your hunger or thirst is too low for natural healing.");
        var text5_3 = TextBuilder.of("Comfort lets you recover health even when your hunger or thirst is too low for natural regeneration.")
                .appendWithSpace("It also boosts your healing rate to what you'd get at full hunger and thirst.")
                .appendWithSpace("It can work with Nourishment to help you survive difficult situations.");
        var text5_4 = TextBuilder.of("Workhorse allows horses or players to carry more very large or heavy items without becoming overburdened.")
                .append("It can be applied to a horse by feeding it a horse‑feed item if its familiarity is at least 35%.");

        entries.entry(
                EntryJson.builder("effects")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("Special Effects")
                        .setIcon(SDItems.EFFECT_NOURISHMENT.getId())
                        .addTextPage(text5_1.toString())
                        .addSpotlightPage(SDItems.EFFECT_NOURISHMENT.getId().toString(), text5_2.toString())
                        .addSpotlightPage(SDItems.EFFECT_COMFORT.getId().toString(), text5_3.toString())
                        .addSpotlightPage(SDItems.EFFECT_WORKHORSE.getId().toString(), text5_4.toString())
                        .build()
        );

        // Entry: 6 Rich Soil
        var text6_1 = TextBuilder.of("Rich Soil is a special type of soil that accelerates natural growth.")
                .appendWithSpace("It shortens the preparation time for saplings to grow.")
                .appendWithSpace("If the block above Rich Soil is air, it has a chance to randomly foods brown or red mushrooms.")
                .appendWithSpace("These mushrooms can later grow into full mushroom colonies over time.");


        var text6_2 = TextBuilder.of("Rich Soil can be tilled with a hoe to create Rich Soil Farmland.")
                .appendWithSpace("Crops planted on Rich Soil Farmland continues growing even when the ambient temperature or moisture slightly deviated from their normal growth range.");

        var text6_3 = TextBuilder.of("You can craft organic compost and place it in the world. The Compost will eventually become a rich soil block over time.")
                .appendWithSpace("Water, sky light, other rich soil blocks and mushrooms can accelerate the conversion.");


        entries.entry(
                EntryJson.builder("rich_soil")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("Rich Soil")
                        .setIcon(ModItems.RICH_SOIL_FARMLAND)
                        .addTextPage(text6_1.toString())
                        .addSingleBlockPage("Rich Soil", getId(ModItems.RICH_SOIL.get()).toString())
                        .addTextPage(text6_2.toString())
                        .addSingleBlockPage("Rich Soil Farmland", getId(ModItems.RICH_SOIL_FARMLAND.get()).toString())
                        .addTextPage(text6_3.toString())
                        .addCraftingRecipe(
                                SDUtils.RLUtils.build("crafting/misc/organic_compost"),
                                SDUtils.RLUtils.build("crafting/misc/organic_compost2"),
                                "Crafting An Organic Compost"
                        )
                        .extraRecipeMapping(ModItems.RICH_SOIL, 0)
                        .extraRecipeMapping(ModItems.RICH_SOIL_FARMLAND, 2)
                        .extraRecipeMapping(ModItems.ORGANIC_COMPOST, 4)
                        .build()
        );

        //Entry 7: Ham
        var text7_1 = TextBuilder.of("Ham is a large cut of meat from a Suidae. It can be a ")
                .link(TFCGuide.Mechanics.MECHANICS_ANIMAL_HUSBANDRY_PIG, "domestic pig")
                .appendWithSpace("or a ").link(TFCGuide.TheWorld.THE_WORLD_WILD_ANIMALS_RAMMING, "boar.")
                .appendWithSpace("You can only get hams when you kill the animal with ").link(TFCGuide.Mechanics.MECHANICS_DAMAGE_TYPES, "piercing damage.")
                .appendWithSpace("An animal can drop 2 hams at most.")
                .appendWithSpace("The drop chance is affected by its body size and familiarity (if any).");


        entries.entry(
                EntryJson.builder("ham")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("Ham")
                        .setIcon(ModItems.SMOKED_HAM)
                        .addTextPage(text7_1.toString())
                        .addSpotlightPage(BuiltInRegistries.ITEM.getKey(ModItems.HAM.get()).toString(), "A ham item.")
                        .extraRecipeMapping(ModItems.HAM, 0)
                        .extraRecipeMapping(ModItems.SMOKED_HAM, 0)
                        .build()
        );

        //Entry 8: Food Block Pie and Feast
        var text8_1 = TextBuilder.of("You can make placeable food blocks, including pies and feasts.")
                .appendWithSpace("Most feasts provide ").link(EntryJson.id("effects"), "nourishment").appendWithSpace("while pies provides speed effect.")
                .appendWithSpace("Feasts need you to use a bowl to take a serving to eat while pies can be eaten directly or cut into slices with a knife.");

        var text8_2 = TextBuilder.of("These blocks decay as normal foods, and when they get rotten, they also turn greenish.")
                .appendWithSpace("You could use special agents to make them food models, which never expire, although this also renders them inedible.");

        entries.entry(
                EntryJson.builder("pie_and_feast")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("Pie And Feast")
                        .setIcon(ModItems.HONEY_GLAZED_HAM_BLOCK)
                        .addTextPage(text8_1.toString())
                        .addSpotlightPage("tag:" + SDTags.ItemTags.FOOD_MODEL_COATING.location(), text8_2.toString())
                        .extraRecipeMapping(SDTags.ItemTags.PIE_BLOCKS, 0)
                        .extraRecipeMapping(SDTags.ItemTags.FEAST_BLOCKS, 0)
                        .build());


        /*
                entries.entry(
                EntryJson.builder("")
                        .setCategory("tfc:survivors_delight")
                        .setReadByDefault(true)
                        .setSortnum(++sortNum)
                        .setName("")
                        .setIcon("")
                        .addTextPage(
                                "A farmer's cabinet is an advanced way to $(l:mechanics/decay)store food$(). "
                                    + "You may press $(item)$(k:key.use)$() on it with tallow bucket or beeswax to treat it."
                                    + "A treated cabinet can preserve food for a longer period."
                        )
                        .build());

         */

        run(cats, entries, event);
    }
}

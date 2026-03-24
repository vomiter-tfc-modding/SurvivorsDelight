package com.vomiter.survivorsdelight.registry.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vomiter.survivorsdelight.registry.SDRecipeSerializers;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SDCuttingRecipe extends CuttingBoardRecipe {
    private final List<Output> outputs;

    public SDCuttingRecipe(String group, Ingredient ingredient, Ingredient tool, List<Output> outputs, Optional<SoundEvent> sound) {
        super(group, ingredient, tool, NonNullList.create(), sound);
        this.outputs = outputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return this.outputs.getFirst().getStack();
    }

    public @NotNull List<ItemStack> getResults() {
        return this.outputs.stream().map(Output::getStack).toList();
    }


    @Override
    public @NotNull NonNullList<ChanceResult> getRollableResults() {
        return outputs.stream()
                .flatMap(Output::toChanceResults)
                .collect(Collectors.toCollection(NonNullList::create));
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SDRecipeSerializers.SD_CUTTING.get();
    }



    public sealed interface Output {
        ResourceLocation getType();
        MapCodec<? extends Output> getCodec();
        StreamCodec<? super RegistryFriendlyByteBuf, ? extends Output> getStreamCodec();

        Stream<ChanceResult> toChanceResults();
        ItemStackProvider getISPResult(Level level);
        ItemStack getStack();

        Map<ResourceLocation, MapCodec<? extends Output>> CODEC_MAP = Map.of(
                StackOutput.TYPE, StackOutput.CODEC,
                ProviderOutput.TYPE, ProviderOutput.CODEC
        );

        Codec<Output> CODEC = Codec.lazyInitialized(() ->
                ResourceLocation.CODEC.dispatch(Output::getType, CODEC_MAP::get)
        );

        Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, ? extends Output>> STREAM_CODEC_MAP = Map.of(
                StackOutput.TYPE, StackOutput.STREAM_CODEC,
                ProviderOutput.TYPE, ProviderOutput.STREAM_CODEC
        );

        StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.of(
                (buf, output) -> {
                    ResourceLocation type = output.getType();
                    ResourceLocation.STREAM_CODEC.encode(buf, type);

                    StreamCodec<RegistryFriendlyByteBuf, ? extends Output> rawCodec = STREAM_CODEC_MAP.get(type);
                    if (rawCodec == null) {
                        throw new IllegalArgumentException("Unknown SDCuttingRecipe.Output type: " + type);
                    }

                    @SuppressWarnings("unchecked")
                    StreamCodec<RegistryFriendlyByteBuf, Output> codec =
                            (StreamCodec<RegistryFriendlyByteBuf, Output>) rawCodec;

                    codec.encode(buf, output);
                },
                buf -> {
                    ResourceLocation type = ResourceLocation.STREAM_CODEC.decode(buf);

                    StreamCodec<RegistryFriendlyByteBuf, ? extends Output> rawCodec = STREAM_CODEC_MAP.get(type);
                    if (rawCodec == null) {
                        throw new IllegalArgumentException("Unknown SDCuttingRecipe.Output type: " + type);
                    }

                    @SuppressWarnings("unchecked")
                    StreamCodec<RegistryFriendlyByteBuf, Output> codec =
                            (StreamCodec<RegistryFriendlyByteBuf, Output>) rawCodec;

                    return codec.decode(buf);
                }
        );
    }

    public record StackOutput(ItemStack stack, List<ResourceLocation> modifiers, float chance) implements Output {
        public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("survivorsdelight", "stack");

        public ItemStack getStack() { return stack; }

        public ItemStackProvider getISPResult(Level level) {
            List<ItemStackProvider> providers = new ArrayList<>();
            List<ItemStackModifier> parsedModifiers = modifiers.stream()
                    .map(rl -> SDUtils.decodeModifier(rl, new JsonObject(), level.registryAccess()))
                    .toList();
            return new ItemStackProvider(stack, parsedModifiers);
        }

        public static final MapCodec<StackOutput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(StackOutput::stack),
                ResourceLocation.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(StackOutput::modifiers),
                Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(StackOutput::chance)
        ).apply(instance, StackOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, StackOutput> STREAM_CODEC = StreamCodec.of(
                StackOutput::toNetwork, StackOutput::fromNetwork
        );

        @Override public ResourceLocation getType() { return TYPE; }
        @Override public MapCodec<? extends Output> getCodec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Output> getStreamCodec() {return STREAM_CODEC;}
        @Override
        public Stream<ChanceResult> toChanceResults() {
            return Stream.of(new ChanceResult(stack, chance));
        }

        private static StackOutput fromNetwork(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            List<ResourceLocation> modifiers = buf.readList(ResourceLocation.STREAM_CODEC);
            float chance = buf.readFloat();
            return new StackOutput(stack, modifiers, chance);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, StackOutput output) {
            ItemStack.STREAM_CODEC.encode(buf, output.stack());
            buf.writeCollection(output.modifiers(), ResourceLocation.STREAM_CODEC);
            buf.writeFloat(output.chance());
        }
    }

    public record ProviderOutput(ItemStackProvider provider) implements Output {
        public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("survivorsdelight", "provider");

        public static final MapCodec<ProviderOutput> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackProvider.CODEC.fieldOf("provider").forGetter(ProviderOutput::provider)
        ).apply(instance, ProviderOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ProviderOutput> STREAM_CODEC = StreamCodec.of(
            ProviderOutput::toNetwork, ProviderOutput::fromNetwork
        );

        public ItemStackProvider getISPResult(Level level){
            return provider;
        }

        public ItemStack getStack(){
            return provider.stack();
        }


        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public MapCodec<? extends Output> getCodec() { return CODEC; }

        @Override public StreamCodec<RegistryFriendlyByteBuf, ? extends Output> getStreamCodec() {return STREAM_CODEC;}
        @Override
        public Stream<ChanceResult> toChanceResults() {
            return Stream.of(new ChanceResult(provider.stack(), 1.0f));
        }

        private static ProviderOutput fromNetwork(RegistryFriendlyByteBuf buf) {
            ItemStackProvider provider = ItemStackProvider.STREAM_CODEC.decode(buf);
            return new ProviderOutput(provider);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, ProviderOutput output) {
            ItemStackProvider.STREAM_CODEC.encode(buf, output.provider());
        }
    }

    public static class Serializer implements RecipeSerializer<SDCuttingRecipe> {
        private static final MapCodec<SDCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(CuttingBoardRecipe::getGroup),
                // JSON 端仍然用 LIST_CODEC_NONEMPTY + 限制一個 ingredient
                Ingredient.LIST_CODEC_NONEMPTY.fieldOf("ingredients").flatXmap(ingredients -> {
                    if (ingredients.size() > 1) {
                        return DataResult.error(() -> "Too many ingredients for cutting recipe! Please define only one ingredient");
                    }
                    return DataResult.success(ingredients.getFirst());
                }, ingredient -> DataResult.success(List.of(ingredient))).forGetter(recipe -> recipe.getIngredients().getFirst()),
                Ingredient.CODEC.fieldOf("tool").forGetter(CuttingBoardRecipe::getTool),
                Output.CODEC.listOf().fieldOf("result").forGetter(SDCuttingRecipe::getOutputs),
                SoundEvent.DIRECT_CODEC.optionalFieldOf("sound").forGetter(CuttingBoardRecipe::getSoundEvent)
        ).apply(instance, SDCuttingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SDCuttingRecipe> STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

        @Override
        public @NotNull MapCodec<SDCuttingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SDCuttingRecipe> streamCodec() {
            return STREAM_CODEC;
        }


        private static SDCuttingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();

            // 食材與工具：用 Ingredient 的 STREAM_CODEC
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);

            // Output：用我們剛剛在 Output 介面裡定義的 STREAM_CODEC
            int count = buf.readVarInt();
            List<Output> outputs = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                outputs.add(Output.STREAM_CODEC.decode(buf));
            }

            Optional<SoundEvent> sound;
            sound = Optional.empty();
            return new SDCuttingRecipe(group, ingredient, tool, outputs, sound);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, SDCuttingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());

            Ingredient ingredient = recipe.getIngredients().getFirst();
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);

            Ingredient tool = recipe.getTool();
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, tool);

            List<Output> outputs = recipe.getOutputs();
            buf.writeVarInt(outputs.size());
            for (Output o : outputs) {
                Output.STREAM_CODEC.encode(buf, o);
            }
        }
    }
}

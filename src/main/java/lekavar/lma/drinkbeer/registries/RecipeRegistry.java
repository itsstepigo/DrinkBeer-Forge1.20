package lekavar.lma.drinkbeer.registries;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.recipes.BrewingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeRegistry {
    public static class Type {
        public static final RecipeType<BrewingRecipe> BREWING = RecipeType.register(DrinkBeer.MOD_ID + ":brewing");
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DrinkBeer.MOD_ID);
    public static final RegistryObject<RecipeSerializer<BrewingRecipe>> BREWING = RECIPE_SERIALIZERS.register("brewing", BrewingRecipe.Serializer::new);
}

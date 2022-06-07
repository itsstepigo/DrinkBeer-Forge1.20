package lekavar.lma.drinkbeer.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemStackHelper {
    public static boolean isAirOrEmpty(ItemStack stack)
    {
        return stack == ItemStack.EMPTY || stack.getItem().equals(Items.AIR);
    }
}

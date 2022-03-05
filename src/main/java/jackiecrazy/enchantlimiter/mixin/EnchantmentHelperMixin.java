package jackiecrazy.enchantlimiter.mixin;

import jackiecrazy.enchantlimiter.EnchantLimiter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    /**
     * @author Jackiecrazy
     */
    @Overwrite()
    public static void setEnchantments(Map<Enchantment, Integer> enchMap, ItemStack stack) {
        ListNBT listnbt = new ListNBT();
        double accumulated = 0;
        double max = EnchantLimiter.getTotalEnchantPoints(stack);
        //iterate over all negative values first to make space
        for (Map.Entry<Enchantment, Integer> entry : enchMap.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                int i = entry.getValue();
                //only iterating negatives
                if (EnchantLimiter.getRequiredEnchantPoints(enchantment, i) >= 0) continue;
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putString("id", String.valueOf((Object) Registry.ENCHANTMENT.getKey(enchantment)));
                compoundnbt.putShort("lvl", (short) i);
                listnbt.add(compoundnbt);
                if (stack.getItem() instanceof EnchantedBookItem) {
                    EnchantedBookItem.addEnchantment(stack, new EnchantmentData(enchantment, i));
                }
                accumulated += EnchantLimiter.getRequiredEnchantPoints(enchantment, i);
            }
        }
        //iterate over all positive values, and add as much as the enchantability allows
        for (Map.Entry<Enchantment, Integer> entry : enchMap.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                int i = entry.getValue();
                //negatives have already been iterated
                if (EnchantLimiter.getRequiredEnchantPoints(enchantment, i) < 0) continue;
                while (i >= 1) {
                    if (accumulated + EnchantLimiter.getRequiredEnchantPoints(enchantment, i) > max) {
                        i--;
                    } else break;
                }
                if (i <= 0) continue;
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putString("id", String.valueOf((Object) Registry.ENCHANTMENT.getKey(enchantment)));
                compoundnbt.putShort("lvl", (short) i);
                listnbt.add(compoundnbt);
                if (stack.getItem() instanceof EnchantedBookItem) {
                    EnchantedBookItem.addEnchantment(stack, new EnchantmentData(enchantment, i));
                }
                accumulated += EnchantLimiter.getRequiredEnchantPoints(enchantment, i);
            }
        }

        if (listnbt.isEmpty()) {
            stack.removeChildTag("Enchantments");
        } else if (stack.getItem() != Items.ENCHANTED_BOOK) {
            stack.setTagInfo("Enchantments", listnbt);
        }
    }
}

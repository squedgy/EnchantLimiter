package jackiecrazy.enchantlimiter;

import net.java.games.input.Component;
import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EnchantLimiter.MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)//by marking it high, this will appear AFTER ench desc
    public static void tooltip(ItemTooltipEvent e) {
        if (e.getItemStack().isEnchanted() || e.getItemStack().isEnchantable() || e.getItemStack().getItem() instanceof EnchantedBookItem) {
            final double used = EnchantLimiter.getUsedEnchantPoints(e.getItemStack());
            final double total = EnchantLimiter.getTotalEnchantPoints(e.getItemStack());
            e.getToolTip().add(new TranslationTextComponent("enchantlimiter.points", (used > total ? TextFormatting.RED : "") + "" + used + "" + TextFormatting.RESET + "/" + total));
        }
        if (e.getItemStack().getItem() instanceof EnchantedBookItem) {
            if(Screen.hasShiftDown())
            insertDescriptionTooltips(e.getToolTip(), e.getItemStack());
            else e.getToolTip().add(new TranslationTextComponent("enchantlimiter.shift"));
        }
    }

    private static void insertDescriptionTooltips(List<ITextComponent> tips, ItemStack stack) {

        final Iterator<Map.Entry<Enchantment, Integer>> enchants = EnchantmentHelper.getEnchantments(stack).entrySet().iterator();
        while (enchants.hasNext()) {
            final Map.Entry<Enchantment, Integer> entry = enchants.next();
            Enchantment enchant=entry.getKey();
            final ListIterator<ITextComponent> tooltips = tips.listIterator();
            while (tooltips.hasNext()) {
                final ITextComponent component = tooltips.next();
                if (component instanceof TranslationTextComponent && ((TranslationTextComponent) component).getKey().equals(enchant.getName())) {
                    final LimiterConfig.EnchantInfo info = LimiterConfig.map.getOrDefault(enchant, LimiterConfig.DEFAULT);
                    tooltips.add(new TranslationTextComponent("enchantlimiter.tip", info.getBase(), info.getIncrement(), entry.getValue(), EnchantLimiter.getRequiredEnchantPoints(enchant, entry.getValue())));
                    if (enchants.hasNext()) {
                        tooltips.add(new StringTextComponent(""));
                    }
                    break;
                }
            }
        }
    }
}

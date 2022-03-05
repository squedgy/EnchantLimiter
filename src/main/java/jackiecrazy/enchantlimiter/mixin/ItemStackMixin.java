package jackiecrazy.enchantlimiter.mixin;

import jackiecrazy.enchantlimiter.EnchantLimiter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract void addEnchantment(Enchantment ench, int level);

    @Inject(cancellable = true, at = @At("HEAD"), method = "addEnchantment", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addEnchantment(Enchantment ench, int level, CallbackInfo ci) {
        ItemStack i = (ItemStack) (Object) this;
        if (EnchantLimiter.getTotalEnchantPoints(i) - EnchantLimiter.getUsedEnchantPoints(i) < EnchantLimiter.getRequiredEnchantPoints(ench, level)) {
            if (level > 1)
                addEnchantment(ench, level - 1);
            ci.cancel();
        }
    }
}

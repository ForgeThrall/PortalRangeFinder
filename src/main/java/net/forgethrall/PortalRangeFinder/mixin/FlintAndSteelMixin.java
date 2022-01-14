package net.forgethrall.PortalRangeFinder.mixin;

import net.forgethrall.PortalRangeFinder.ClientInitializer;
import net.forgethrall.PortalRangeFinder.PortalVisualizer;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelMixin {
	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	private void init(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);
		if(world.isClient() && blockState.getBlock() instanceof NetherPortalBlock) {
			ClientInitializer.portals.add(new PortalVisualizer(context.getWorld(), blockPos));
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}

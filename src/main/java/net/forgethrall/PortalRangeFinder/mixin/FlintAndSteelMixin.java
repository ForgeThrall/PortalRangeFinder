package net.forgethrall.PortalRangeFinder.mixin;

import net.forgethrall.PortalRangeFinder.ClientInitializer;
import net.forgethrall.PortalRangeFinder.Polyhedron;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.item.FlintAndSteelItem;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelMixin {
	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	private void init(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);
		if(world.isClient() && blockState.getBlock() instanceof NetherPortalBlock) {
			BlockLocating.Rectangle portalRect = BlockLocating.getLargestRectangle(blockPos, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> world.getBlockState(pos) == blockState);

			Vec3d portalOrigin = new Vec3d(portalRect.lowerLeft.getX(), portalRect.lowerLeft.getY(), portalRect.lowerLeft.getZ());
			int xWidth, yWidth;
			if(blockState.get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X) {
				xWidth = portalRect.width;
				yWidth = 1;
			} else {
				xWidth = 1;
				yWidth = portalRect.width;
			}
			Vec3d portalSize = new Vec3d(xWidth , portalRect.height, yWidth);

			ClientInitializer.polyhedra.add(Polyhedron.rectangularCuboid(portalOrigin, portalSize));

			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}

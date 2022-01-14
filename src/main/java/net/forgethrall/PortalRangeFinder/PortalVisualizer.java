package net.forgethrall.PortalRangeFinder;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;

public class PortalVisualizer {
	private RegistryKey<World> portalDimension;
	private Polyhedron framePoly;
	private Polyhedron rangePoly;

	public PortalVisualizer(World world, BlockPos portalBlockPos) {
		BlockState blockState = world.getBlockState(portalBlockPos);
		BlockLocating.Rectangle portalRect = BlockLocating.getLargestRectangle(portalBlockPos, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> world.getBlockState(pos) == blockState);

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

		this.portalDimension = world.getRegistryKey();
		int rangeRadius, rangeBottom, rangeTop;
		if(portalDimension == World.OVERWORLD){
			rangeRadius = 16;
			rangeBottom = 0;
			rangeTop = 256;
		} else {
			rangeRadius = 128;
			rangeBottom = -64;
			rangeTop = 320;
		}

		this.framePoly = Polyhedron.rectangularCuboid(portalOrigin, portalSize);
		Vec3d rangeOrigin = new Vec3d(portalOrigin.x - rangeRadius,rangeBottom, portalOrigin.z - rangeRadius);
		Vec3d rangeSize = new Vec3d(portalSize.x + 2 * rangeRadius, rangeTop, portalSize.z + 2* rangeRadius);
		this.rangePoly = Polyhedron.rectangularCuboid(rangeOrigin, rangeSize);
	}

	public void render(WorldRenderContext context) {
		if(context.world().getRegistryKey() == portalDimension) {
			framePoly.render(context);
		} else {
			rangePoly.render(context);
		}
	}
}

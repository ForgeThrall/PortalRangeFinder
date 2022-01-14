package net.forgethrall.PortalRangeFinder;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;

import java.util.HashSet;

public class PortalVisualizer {
	private RegistryKey<World> portalDimension;
	Mesh portalMesh;
	Mesh rangeMesh;

	BlockPos portalOrigin;
	Vec3i portalSize;

	public PortalVisualizer(World world, BlockPos portalBlockPos) {
		BlockState blockState = world.getBlockState(portalBlockPos);
		BlockLocating.Rectangle portalRect = BlockLocating.getLargestRectangle(portalBlockPos, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> world.getBlockState(pos) == blockState);

		this.portalOrigin = portalRect.lowerLeft;
		int xWidth, yWidth;
		if(blockState.get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X) {
			xWidth = portalRect.width;
			yWidth = 1;
		} else {
			xWidth = 1;
			yWidth = portalRect.width;
		}
		this.portalSize = new Vec3i(xWidth , portalRect.height, yWidth);

		this.portalDimension = world.getRegistryKey();
//		int rangeRadius, rangeBottom, rangeTop;
//		if(portalDimension == World.OVERWORLD){
//			rangeRadius = 16;
//			rangeBottom = 0;
//			rangeTop = 256;
//		} else {
//			rangeRadius = 128;
//			rangeBottom = -64;
//			rangeTop = 320;
//		}

		generateFrameMesh();
//		generateRangeMesh();
	}

	public void render(WorldRenderContext context) {
		if(context.world().getRegistryKey() == portalDimension) {
			portalMesh.render(context);
		} else {
			rangeMesh.render(context);
		}
	}

	private void generateFrameMesh() {
		HashSet<Vec3d[]> tris = new HashSet<>();
		HashSet<Vec3d[]> lines = new HashSet<>();

		Vec3d v000 = new Vec3d(portalOrigin.getX(), portalOrigin.getY(), portalOrigin.getZ());
		Vec3d v100 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY(), portalOrigin.getZ());
		Vec3d v001 = new Vec3d(portalOrigin.getX(), portalOrigin.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v101 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v010 = new Vec3d(portalOrigin.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ());
		Vec3d v110 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ());
		Vec3d v011 = new Vec3d(portalOrigin.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v111 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ() + portalSize.getZ());

		tris.add(new Vec3d[]{v000, v100, v101});
		tris.add(new Vec3d[]{v000, v101, v001});
		tris.add(new Vec3d[]{v000, v001, v011});
		tris.add(new Vec3d[]{v000, v011, v010});
		tris.add(new Vec3d[]{v000, v010, v110});
		tris.add(new Vec3d[]{v000, v110, v100});

		tris.add(new Vec3d[]{v111, v110, v010});
		tris.add(new Vec3d[]{v111, v010, v011});
		tris.add(new Vec3d[]{v111, v011, v001});
		tris.add(new Vec3d[]{v111, v001, v101});
		tris.add(new Vec3d[]{v111, v101, v100});
		tris.add(new Vec3d[]{v111, v100, v110});

		lines.add(new Vec3d[]{v000, v100});
		lines.add(new Vec3d[]{v100, v101});
		lines.add(new Vec3d[]{v101, v001});
		lines.add(new Vec3d[]{v001, v000});

		lines.add(new Vec3d[]{v111, v011});
		lines.add(new Vec3d[]{v011, v010});
		lines.add(new Vec3d[]{v010, v110});
		lines.add(new Vec3d[]{v110, v111});

		lines.add(new Vec3d[]{v000, v010});
		lines.add(new Vec3d[]{v001, v011});
		lines.add(new Vec3d[]{v101, v111});
		lines.add(new Vec3d[]{v100, v110});

		this.portalMesh = new Mesh(tris, lines);
	}
}

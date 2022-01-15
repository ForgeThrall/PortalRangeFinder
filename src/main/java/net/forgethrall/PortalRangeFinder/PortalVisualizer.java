package net.forgethrall.PortalRangeFinder;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashSet;

public class PortalVisualizer {
	private final RegistryKey<World> portalDimension;
	boolean[][][] voxels;

	BlockPos portalOrigin;
	Vec3i portalSize;

	Mesh portalMesh;
	Mesh rangeMesh;

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

		// nether range
//		voxels = new boolean[264][256][264];

		// overworld range
		voxels = new boolean[33][384][33];

		for(boolean[][] a : voxels) {
			for(boolean[] b : a) {
				Arrays.fill(b, true);
			}
		}

		generateFrameMesh();
		generateRangeMesh();
	}

	public void render(WorldRenderContext context) {
		if(context.world().getRegistryKey() == portalDimension) {
			portalMesh.render(context);
		} else {
			rangeMesh.render(context);
		}
	}

	private void generateFrameMesh() {
		HashSet<Vec3d[]> quads = new HashSet<>();
		HashSet<Vec3d[]> lines = new HashSet<>();

		Vec3d v000 = new Vec3d(portalOrigin.getX(), portalOrigin.getY(), portalOrigin.getZ());
		Vec3d v100 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY(), portalOrigin.getZ());
		Vec3d v001 = new Vec3d(portalOrigin.getX(), portalOrigin.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v101 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v010 = new Vec3d(portalOrigin.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ());
		Vec3d v110 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ());
		Vec3d v011 = new Vec3d(portalOrigin.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ() + portalSize.getZ());
		Vec3d v111 = new Vec3d(portalOrigin.getX() + portalSize.getX(), portalOrigin.getY() + portalSize.getY(), portalOrigin.getZ() + portalSize.getZ());

		quads.add(new Vec3d[]{v000, v100, v101, v001});
		quads.add(new Vec3d[]{v000, v001, v011, v010});
		quads.add(new Vec3d[]{v000, v010, v110, v100});

		quads.add(new Vec3d[]{v111, v110, v010, v011});
		quads.add(new Vec3d[]{v111, v011, v001, v101});
		quads.add(new Vec3d[]{v111, v101, v100, v110});

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

		this.portalMesh = new Mesh(quads, lines);
	}

	private void generateRangeMesh() {
		generateRangeMeshOverworld();
	}

	private void generateRangeMeshOverworld() {
		HashSet<Vec3d[]> quads = new HashSet<>();
		HashSet<Vec3d[]> lines = new HashSet<>();

		int x0 = MathHelper.floor(portalOrigin.getX()*8-128);
		int y0 = -64;
		int z0 = MathHelper.floor(portalOrigin.getZ()*8-128);
		for(int x = 0; x < voxels.length; x++) {
			for(int y = 0; y < voxels[0].length; y++) {
				for(int z = 0; z < voxels[0][0].length; z++) {
					if(!voxels[x][y][z]) continue;

					Vec3d v000 = new Vec3d(x0+x*8,   y0+y,   z0+z*8);
					Vec3d v100 = new Vec3d(x0+x*8+8, y0+y,   z0+z*8);
					Vec3d v001 = new Vec3d(x0+x*8,   y0+y,   z0+z*8+8);
					Vec3d v101 = new Vec3d(x0+x*8+8, y0+y,   z0+z*8+8);
					Vec3d v010 = new Vec3d(x0+x*8,   y0+y+1, z0+z*8);
					Vec3d v110 = new Vec3d(x0+x*8+8, y0+y+1, z0+z*8);
					Vec3d v011 = new Vec3d(x0+x*8,   y0+y+1, z0+z*8+8);
					Vec3d v111 = new Vec3d(x0+x*8+8, y0+y+1, z0+z*8+8);

					if(x+1 >= voxels.length || !voxels[x+1][y][z]){
						quads.add(new Vec3d[]{v100, v110, v111, v101});
					}
					if(x-1 < 0 || !voxels[x-1][y][z]) {
						quads.add(new Vec3d[]{v000, v001, v011, v010});
					}
					if(y+1 >= voxels[0].length || !voxels[x][y+1][z]) {
						quads.add(new Vec3d[]{v010, v011, v111, v110});
					}
					if(y-1 < 0 || !voxels[x][y-1][z]) {
						quads.add(new Vec3d[]{v000, v100, v101, v001});
					}
					if(z+1 >= voxels[0][0].length || !voxels[x][y][z+1]) {
						quads.add(new Vec3d[]{v001, v101, v111, v011});
					}
					if(z-1 < 0 || !voxels[x][y][z-1]) {
						quads.add(new Vec3d[]{v000, v010, v110, v100});
					}
				}
			}
		}

		rangeMesh = new Mesh(quads, lines);
	}
}

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
		final int xWidth, yWidth;
		if(blockState.get(Properties.HORIZONTAL_AXIS) == Direction.Axis.X) {
			xWidth = portalRect.width;
			yWidth = 1;
		} else {
			xWidth = 1;
			yWidth = portalRect.width;
		}
		this.portalSize = new Vec3i(xWidth , portalRect.height, yWidth);

		this.portalDimension = world.getRegistryKey();

		if(portalDimension == World.OVERWORLD) {
			voxels = new boolean[256+portalSize.getX()][256][256+portalSize.getY()];
		} else {
			voxels = new boolean[32+portalSize.getX()][384][32+portalSize.getZ()];
		}

		for(boolean[][] a : voxels) {
			for(boolean[] b : a) {
				Arrays.fill(b, true);
			}
		}

		this.portalMesh = new Mesh();
		this.rangeMesh = new Mesh();
		generateFrameMesh();
		generateRangeMesh();
	}

	public void render(WorldRenderContext context) {
		if(context.world().getRegistryKey() == portalDimension) {
			portalMesh.render(context);
		} else if(context.world().getRegistryKey() == World.NETHER || context.world().getRegistryKey() == World.OVERWORLD) {
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

		this.portalMesh.setMesh(quads, lines);
	}

	public void generateRangeMesh() {
		HashSet<Vec3d[]> quads = new HashSet<>();
		HashSet<Vec3d[]> lines = new HashSet<>();

		final double scale, x0, y0, z0;
		if(portalDimension == World.OVERWORLD) {
			scale = 1.0/8;
			x0 = portalOrigin.getX()*scale - 16;
			y0 = 0;
			z0 = portalOrigin.getZ()*scale - 16;
		} else {
			scale = 8;
			x0 = portalOrigin.getX()*scale-128;
			y0 = -64;
			z0 = portalOrigin.getZ()*scale-128;
		}

		int[] dims = new int[]{voxels.length, voxels[0].length, voxels[0][0].length};
		// Sweep over the three axes
		// d is the principle axis, u and v are the scanning axes
		for(int d = 0; d < 3; d++) {
			int u = (d+1)%3;
			int v = (d+2)%3;

			int[] xyz = new int[]{0, 0, 0};
			int[] step = new int[]{0, 0, 0};
			step[d] = 1;

			boolean[][] mask = new boolean[dims[u]][dims[v]];

			for(xyz[d] = -1; xyz[d] < dims[d]; ) {
				// Compute mask
				for(xyz[u] = 0; xyz[u] < dims[u]; xyz[u]++)
				for(xyz[v] = 0; xyz[v] < dims[v]; xyz[v]++) {
					// starts before the first plane, comparing ahead
					mask[xyz[u]][xyz[v]] =
							(xyz[d] >= 0 && voxels[xyz[0]][xyz[1]][xyz[2]]) !=
							(xyz[d]+1 < dims[d] && voxels[xyz[0] + step[0]][xyz[1] + step[1]][xyz[2] + step[2]]);
				}
				if(d == 0 && xyz[d] == -1) {
					ClientInitializer.LOGGER.info(Arrays.deepToString(voxels[0]));
					ClientInitializer.LOGGER.info(Arrays.deepToString(mask));
				}
				xyz[d]++;

				// generate outline from mask
				for(int j = -1; j < dims[v]; j++)
				for(int i = 0; i < dims[u]; i++) {
					if((j >= 0 && mask[i][j]) == (j+1 < dims[v] && mask[i][j+1])) continue;
					xyz[u] = i;
					xyz[v] = j+1;

					int w = 1;
					while(i+w < dims[u] && ((j >= 0 && mask[i+w][j]) != (j+1 < dims[v] && mask[i+w][j+1]))) w++;
					int[] du = new int[]{0, 0, 0};
					du[u] = w;

					lines.add(new Vec3d[]{
							new Vec3d(x0+(xyz[0]      )*scale, y0+xyz[1],       z0+(xyz[2]      )*scale),
							new Vec3d(x0+(xyz[0]+du[0])*scale, y0+xyz[1]+du[1], z0+(xyz[2]+du[2])*scale)
					});
					i += w-1;
				}

				// generate mesh from mask (destructive of the mask)
				for(int j = 0; j < dims[v]; j++)
				for(int i = 0; i < dims[u]; i++) {
					if(!mask[i][j]) continue;

					xyz[u] = i;
					xyz[v] = j;

					int w = 1;
					while(i+w < dims[u] && mask[i+w][j]) w++;

					int h;
					calc_height:
					for(h = 1; j+h < dims[v]; h++){
						for(int k = 0; k < w; k++) {
							if(!mask[i+k][j+h]) break calc_height;
						}
					}

					int[] du = new int[]{0, 0, 0};
					int[] dv = new int[]{0, 0, 0};
					du[u] = w;
					dv[v] = h;

					quads.add(new Vec3d[]{
							new Vec3d(x0+(xyz[0]            )*scale, y0+xyz[1],             z0+(xyz[2]            )*scale),
							new Vec3d(x0+(xyz[0]+du[0]      )*scale, y0+xyz[1]+du[1],       z0+(xyz[2]+du[2]      )*scale),
							new Vec3d(x0+(xyz[0]+du[0]+dv[0])*scale, y0+xyz[1]+du[1]+dv[1], z0+(xyz[2]+du[2]+dv[2])*scale),
							new Vec3d(x0+(xyz[0]      +dv[0])*scale, y0+xyz[1]      +dv[1], z0+(xyz[2]      +dv[2])*scale)
					});

					// clear the portion of the mask that's been covered
					for(int l = 0; l < w; l++) {
						for(int k = 0; k < h; k++) {
							mask[i+l][j+k] = false;
						}
					}
					i += w-1;
				}
			}
		}

		this.rangeMesh.setMesh(quads, lines);
	}

	public void interfereWith(PortalVisualizer other) {
		if(this.portalDimension != other.portalDimension) return;

		// todo: de-duplicate this from generateRangeMesh?
		final int x0, y0, z0, thisX0, thisZ0, otherX0, otherZ0;
		if(this.portalDimension == World.OVERWORLD) {
			y0 = 0;
			thisX0 = this.portalOrigin.getX() - 128;
			thisZ0 = this.portalOrigin.getZ() - 128;
			otherX0 = other.portalOrigin.getX() - 128;
			otherZ0 = other.portalOrigin.getZ() - 128;
		} else {
			y0 = -64;
			thisX0 = this.portalOrigin.getX()-16;
			thisZ0 = this.portalOrigin.getZ()-16;
			otherX0 = other.portalOrigin.getX()-16;
			otherZ0 = other.portalOrigin.getZ()-16;
		}
		x0 = Math.max(thisX0, otherX0);
		z0 = Math.max(thisZ0, otherZ0);

		Vec3i thisOrigin = new Vec3i(this.portalOrigin.getX(), this.portalOrigin.getY(), this.portalOrigin.getZ());
		Vec3i otherOrigin = new Vec3i(other.portalOrigin.getX(), other.portalOrigin.getY(), other.portalOrigin.getZ());

		int thisXOff  = Math.max(otherX0 - thisX0,  0);
		int otherXOff = Math.max(thisX0  - otherX0, 0);
		int thisZOff  = Math.max(otherZ0 - thisZ0,  0);
		int otherZOff = Math.max(thisZ0  - otherZ0, 0);

		for(int x = 0; x + thisXOff < this.voxels.length && x + otherXOff < other.voxels.length; x++)
		for(int y = 0; y < this.voxels[0].length && y < other.voxels[0].length; y++)
		for(int z = 0; z + thisZOff < this.voxels[0][0].length && z + otherZOff < other.voxels[0][0].length; z++) {
			if(!other.voxels[x+otherXOff][y][z+otherZOff]) continue;
			Vec3i pos = new Vec3i(x+x0, y+y0, z+z0);
			// todo: deal with equidistant points. MC does weird priority order with it
			if(pos.getSquaredDistance(thisOrigin) > pos.getSquaredDistance(otherOrigin)) {
				this.voxels[x+thisXOff][y][z+thisZOff] = false;
			} else {
				other.voxels[x+otherXOff][y][z+otherZOff] = false;
			}
		}

//		this.generateRangeMesh();
		other.generateRangeMesh();
	}
}

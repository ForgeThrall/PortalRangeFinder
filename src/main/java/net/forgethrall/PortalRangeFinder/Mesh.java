package net.forgethrall.PortalRangeFinder;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.HashSet;

public class Mesh {
	Vec3d[][] quads;
	Vec3d[][] lines;
	int red = 230, green = 200, blue = 200, alpha = 90;

	public void setColor(int r, int g, int b, int a) {
		red = r;
		green = g;
		blue = b;
		alpha = a;
	}

	public Mesh() {}

	public Mesh(Vec3d[][] quads, Vec3d[][] lines){
		this.quads = quads;
		this.lines = lines;
	}

	public Mesh(HashSet<Vec3d[]> quads, HashSet<Vec3d[]> lines){
		this(quads.toArray(Vec3d[][]::new), lines.toArray(Vec3d[][]::new));
	}

	public void setMesh(HashSet<Vec3d[]> quads, HashSet<Vec3d[]> lines){
		this.quads = quads.toArray(Vec3d[][]::new);
		this.lines = lines.toArray(Vec3d[][]::new);
	}

	public void render(WorldRenderContext context) {
		RenderSystem.disableTexture();
		RenderSystem.polygonOffset(-3f, -3f);
		RenderSystem.enablePolygonOffset();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);

		Vec3d cam = context.camera().getPos();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		//todo change this to a triangle_fan or quads?
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		for(Vec3d[] quad : quads) {
			Vec3d v0 = cam.relativize(quad[0]);
			Vec3d v1 = cam.relativize(quad[1]);
			Vec3d v2 = cam.relativize(quad[2]);
			Vec3d v3 = cam.relativize(quad[3]);
			bufferBuilder.vertex(v0.x, v0.y, v0.z).color(red, green, blue, alpha).next();
			bufferBuilder.vertex(v1.x, v1.y, v1.z).color(red, green, blue, alpha).next();
			bufferBuilder.vertex(v2.x, v2.y, v2.z).color(red, green, blue, alpha).next();
			bufferBuilder.vertex(v3.x, v3.y, v3.z).color(red, green, blue, alpha).next();
		}
		tessellator.draw();

		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(3F);
		bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		for(Vec3d[] line : lines) {
			Vec3d v0 = cam.relativize(line[0]);
			Vec3d v1 = cam.relativize(line[1]);
			Vec3f lineNormal = new Vec3f(v0.relativize(v1).normalize());
			bufferBuilder.vertex(v0.x, v0.y, v0.z)
					.color(red, green, blue, 255)
					.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
					.next();
			bufferBuilder.vertex(v1.x, v1.y, v1.z)
					.color(red, green, blue, 255)
					.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
					.next();
		}
		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.polygonOffset(0f, 0f);
		RenderSystem.disablePolygonOffset();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}
}

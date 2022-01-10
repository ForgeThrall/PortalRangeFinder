package net.forgethrall.PortalRangeFinder;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("modid");

	private static void testLineRender(WorldRenderContext context) {
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.multiplyPositionMatrix(context.matrixStack().peek().getPositionMatrix());
		RenderSystem.applyModelViewMatrix();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		Vec3f v1 = new Vec3f(8, -58, 8);
		v1.subtract(new Vec3f(context.camera().getPos()));
		Vec3f v2 = v1.copy();
		v2.add(3, 1, 1);

		Vec3f lineNormal = v2.copy();
		lineNormal.subtract(v1);
		lineNormal.normalize();

		RenderSystem.lineWidth(100F);
		int red = 255, grn = 30, blu = 1, alpha = 128;

		bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		bufferBuilder.vertex(v1.getX(), v1.getY(), v1.getZ())
				.color(red, grn, blu, alpha)
				.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
				.next();

		bufferBuilder.vertex(v2.getX(), v2.getY(), v2.getZ())
				.color(red, grn, blu, alpha)
				.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
				.next();
		tessellator.draw();

		RenderSystem.enableCull();
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	private static void testFaceRender(WorldRenderContext context) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.multiplyPositionMatrix(context.matrixStack().peek().getPositionMatrix());
		RenderSystem.applyModelViewMatrix();

		Vec3f v1 = new Vec3f(8, -58, 9);
		Vec3f v2 = new Vec3f(1, 0, 0);
		Vec3f v3 = new Vec3f(0, 1, 0);
		v1.subtract(new Vec3f(context.camera().getPos()));
		v2.add(v1);
		v3.add(v1);

		int red = 255, green = 30, blue = 200, alpha = 128;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(v1.getX(), v1.getY(), v1.getZ()).color(255, 0, 0, alpha).next();
		bufferBuilder.vertex(v2.getX(), v2.getY(), v2.getZ()).color(0, 0, 255, alpha).next();
		bufferBuilder.vertex(v3.getX(), v3.getY(), v3.getZ()).color(0, 255, 0, alpha).next();
		tessellator.draw();

		RenderSystem.enableCull();
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}


	private static final Polyhedron testPolyhedron1 = Polyhedron.cube(8, -57, 7, 4);
	private static final Polyhedron testPolyhedron2 = Polyhedron.cube(6, -56, 8, 4);

	private static void renderPolyhedron(WorldRenderContext context) {
		testPolyhedron1.render(context);
		testPolyhedron2.render(context);
	}

	@Override
	public void onInitializeClient() {
		testPolyhedron2.setColor(125, 220, 175, 90);
		Polyhedron.interact(testPolyhedron1, testPolyhedron2);
//		testPolyhedron1.slice(new Polyhedron.Plane(new Vec3d(-2, 1, 1), new Vec3d(7.5, -56, 8)));
//		testPolyhedron2.slice(new Polyhedron.Plane(new Vec3d(2, -1, -1), new Vec3d(7.5, -56, 8)));
		WorldRenderEvents.LAST.register(ClientInitializer::renderPolyhedron);
	}
}

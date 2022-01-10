package net.forgethrall.PortalRangeFinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("modid");


	private static void renderPolyhedron(WorldRenderContext context) {
		double sin = Math.sin((context.world().getTime() + context.tickDelta())/20.0);
		double cos = Math.cos((context.world().getTime() + context.tickDelta())/35.0);
		final Polyhedron testPolyhedron1 = Polyhedron.cube(8, -52+cos*3, 7, 4);
		final Polyhedron testPolyhedron2 = Polyhedron.cube(6, -51, 8+sin*4, 4);
		testPolyhedron2.setColor(125, 220, 175, 90);
		Polyhedron.interact(testPolyhedron1, testPolyhedron2);
		testPolyhedron1.render(context);
		testPolyhedron2.render(context);
	}

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.LAST.register(ClientInitializer::renderPolyhedron);
	}
}

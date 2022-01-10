package net.forgethrall.PortalRangeFinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("modid");

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
		WorldRenderEvents.LAST.register(ClientInitializer::renderPolyhedron);
	}
}

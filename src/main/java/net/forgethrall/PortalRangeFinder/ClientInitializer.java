package net.forgethrall.PortalRangeFinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class ClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("modid");

	public static Set<Polyhedron> polyhedra = new HashSet<>();

	private static void renderPolyhedrons(WorldRenderContext context) {
		for (Polyhedron polyhedron : polyhedra) {
			if(polyhedron == null) continue; // maybe not needed
			polyhedron.render(context);
		}
	}

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.LAST.register(ClientInitializer::renderPolyhedrons);
	}
}

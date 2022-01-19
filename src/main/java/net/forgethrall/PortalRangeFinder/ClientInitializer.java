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

	public static Set<PortalVisualizer> portals = new HashSet<>();
	private static final int[][] colors = new int[][]{{230, 200, 200, 90}, {30, 230, 120, 90}, {0xfa, 0x00f, 0x9, 90}};
	private static int c = 0;

	public static void addPortal(PortalVisualizer p) {
		for(PortalVisualizer p2 : portals) {
			p.interfereWith(p2);
		}
		p.rangeMesh.setColor(colors[c][0], colors[c][1], colors[c][2], colors[c][3]);
		p.portalMesh.setColor(colors[c][0], colors[c][1], colors[c][2], colors[c][3]);
		c = (c+1)%3;
		portals.add(p);
	}

	private static void renderPolyhedrons(WorldRenderContext context) {
		PortalVisualizer[] p = portals.toArray(PortalVisualizer[]::new);
		if(p.length > 0) p[0].render(context);
//		for (PortalVisualizer portal : portals) {
//			if(portal == null) continue; // maybe not needed
//			portal.render(context);
//		}
	}

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.LAST.register(ClientInitializer::renderPolyhedrons);
	}
}

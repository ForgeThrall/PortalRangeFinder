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
	public static PortalVisualizer latestPortal;
	private static final int[][] colors = new int[][]{{230, 200, 200, 50}, {30, 230, 120, 50}, {0xfa, 0x00f, 0x9, 50}};
	private static int c = 0;

	public static void addPortal(PortalVisualizer p) {
		for(PortalVisualizer p2 : portals) {
			p.interfereWith(p2);
		}
		p.generateRangeMesh();
		p.rangeMesh.setColor(colors[c][0], colors[c][1], colors[c][2], colors[c][3]);
		p.portalMesh.setColor(colors[c][0], colors[c][1], colors[c][2], colors[c][3]);
		c = (c+1)%3;
		portals.add(p);
		latestPortal = p;
	}

	public static void clear() {
		portals.clear();
		latestPortal = null;
	}

	private static void render(WorldRenderContext context) {
//		if(latestPortal != null) latestPortal.render(context);
		for (PortalVisualizer portal : portals) {
			if(portal == null) continue; // maybe not needed
			portal.render(context);
		}
	}

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.LAST.register(ClientInitializer::render);
	}
}

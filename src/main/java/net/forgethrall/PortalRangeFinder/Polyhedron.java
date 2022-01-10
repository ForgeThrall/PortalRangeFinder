package net.forgethrall.PortalRangeFinder;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.HashMap;
import java.util.HashSet;

public class Polyhedron {
	HashSet<HalfEdge> faceAnchors;
	private Vec3d origin;
	int red = 230, green = 200, blue = 200, alpha = 90;

	private Polyhedron(HashSet<HalfEdge> faceAnchors) {
		this.faceAnchors = faceAnchors;
	}

	/**
	 * Geeze. This sucked to write
	 */
	public static Polyhedron cube(double x, double y, double z, double r) {
		HalfEdge topNorth = new HalfEdge(new Vec3d(x+1+r, y+1+r, z+1+r), null, null);
		HalfEdge topWest  = new HalfEdge(new Vec3d(x-r,   y+1+r, z+1+r), topNorth, null);
		HalfEdge topSouth = new HalfEdge(new Vec3d(x-r,   y+1+r, z-r),   topWest,  null);
		HalfEdge topEast  = new HalfEdge(new Vec3d(x+1+r, y+1+r, z-r),   topSouth, null);
		topNorth.next = topEast;

		HalfEdge northTop  = new HalfEdge(new Vec3d(x-r,   y+1+r, z+1+r), null, null);
		HalfEdge northEast = new HalfEdge(new Vec3d(x+1+r, y+1+r, z+1+r), northTop,  null);
		HalfEdge northBot  = new HalfEdge(new Vec3d(x+1+r, y-r,   z+1+r), northEast, null);
		HalfEdge northWest = new HalfEdge(new Vec3d(x-r,   y-r,   z+1+r), northBot,  null);
		northTop.next = northWest;

		HalfEdge westTop   = new HalfEdge(new Vec3d(x-r, y+1+r, z-r), null, null);
		HalfEdge westNorth = new HalfEdge(new Vec3d(x-r, y+1+r, z+1+r), westTop,   null);
		HalfEdge westBot   = new HalfEdge(new Vec3d(x-r, y-r,   z+1+r), westNorth, null);
		HalfEdge westSouth = new HalfEdge(new Vec3d(x-r, y-r,   z-r),   westBot,   null);
		westTop.next = westSouth;

		HalfEdge southTop  = new HalfEdge(new Vec3d(x+1+r, y+1+r, z-r), null, null);
		HalfEdge southWest = new HalfEdge(new Vec3d(x-r,   y+1+r, z-r), southTop, null);
		HalfEdge southBot  = new HalfEdge(new Vec3d(x-r,   y-r,   z-r), southWest,null);
		HalfEdge southEast = new HalfEdge(new Vec3d(x+1+r, y-r,   z-r), southBot, null);
		southTop.next = southEast;

		HalfEdge eastTop   = new HalfEdge(new Vec3d(x+1+r, y+1+r, z+1+r), null, null);
		HalfEdge eastSouth = new HalfEdge(new Vec3d(x+1+r, y+1+r, z-r),   eastTop,   null);
		HalfEdge eastBot   = new HalfEdge(new Vec3d(x+1+r, y-r,   z-r),   eastSouth, null);
		HalfEdge eastNorth = new HalfEdge(new Vec3d(x+1+r, y-r,   z+1+r), eastBot,   null);
		eastTop.next = eastNorth;

		HalfEdge botNorth  = new HalfEdge(new Vec3d(x-r,   y-r, z+1+r), null, null);
		HalfEdge botEast   = new HalfEdge(new Vec3d(x+1+r, y-r, z+1+r), botNorth, null);
		HalfEdge botSouth  = new HalfEdge(new Vec3d(x+1+r, y-r, z-r),   botEast,  null);
		HalfEdge botWest   = new HalfEdge(new Vec3d(x-r,   y-r, z-r),   botSouth, null);
		botNorth.next = botWest;

		topNorth.twin = northTop;
		topWest.twin  = westTop;
		topSouth.twin = southTop;
		topEast.twin  = eastTop;

		northTop.twin  = topNorth;
		northEast.twin = eastNorth;
		northBot.twin  = botNorth;
		northWest.twin = westNorth;

		westTop.twin   = topWest;
		westNorth.twin = northWest;
		westBot.twin   = botWest;
		westSouth.twin = southWest;

		southTop.twin  = topSouth;
		southWest.twin = westSouth;
		southBot.twin  = botSouth;
		southEast.twin = eastSouth;

		eastTop.twin   = topEast;
		eastSouth.twin = southEast;
		eastBot.twin   = botEast;
		eastNorth.twin = northEast;

		botNorth.twin = northBot;
		botEast.twin  = eastBot;
		botSouth.twin = southBot;
		botWest.twin  = westBot;

		Polyhedron result = new Polyhedron(generateFaceAnchors(topNorth));
		result.origin = new Vec3d(x+.5, y+.5, z+.5);
		return result;
	}

	// todo fix this method name. "interfere"?
	public static void interact(Polyhedron p1, Polyhedron p2) {
		Plane slicer = new Plane(p1.origin.subtract(p2.origin), p1.origin.add(p2.origin).multiply(.5));
		p1.slice(slicer.inverse());
		p2.slice(slicer);
	}

	public void setColor(int red, int green, int blue, int alpha) {
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.alpha = alpha;
	}

	/**
	 * slices off any part of the polyhedron that is above the given plane,
	 * creating a new face on the boundary. Only valid on convex polyhedra
	 */
	public void slice(Plane slicer) {
		if(faceAnchors.isEmpty()) return;// this;
		HalfEdge rootEdge = sliceHelper(slicer, faceAnchors.iterator().next(), new HashMap<>());
		this.faceAnchors = generateFaceAnchors(rootEdge);
	}
	private HalfEdge sliceHelper(Plane slicer, HalfEdge currentEdge, HashMap<HalfEdge, HalfEdge> visitedEdges)
	{
		if(visitedEdges.containsKey(currentEdge)) return visitedEdges.get(currentEdge);

		HalfEdge result = new HalfEdge();
		visitedEdges.put(currentEdge, result);

		if(slicer.contains(currentEdge.vert)) { // finishes in bounds
			result.vert = currentEdge.vert;
			result.next = sliceHelper(slicer, currentEdge.next, visitedEdges);
			result.twin = sliceHelper(slicer, currentEdge.twin, visitedEdges);
		} else if(slicer.contains(currentEdge.twin.vert)) { // extends out of bounds
			result.vert =  slicer.intersection(currentEdge.twin.vert, currentEdge.vert);

			result.next = new HalfEdge();
			result.next.twin = new HalfEdge();
			result.next.twin.vert = result.vert;
			result.next.twin.twin = result.next;
			HalfEdge reentry = currentEdge.next;
			while(!slicer.contains(reentry.vert)){reentry = reentry.next;}

			result.next.next = sliceHelper(slicer, reentry, visitedEdges);
			result.twin =  sliceHelper(slicer, currentEdge.twin, visitedEdges);

			// recursion finished, now finish constructing the boundary edges
			result.next.vert = visitedEdges.get(reentry.twin).vert;
			visitedEdges.get(reentry.twin).next.twin.next = result.next.twin;
		} else { // entirely out of bounds
			HalfEdge insideEdge = findInsideEdge(slicer, currentEdge);
			if(insideEdge == null) return null;
			return sliceHelper(slicer, insideEdge, visitedEdges);
		}

		return result;
	}

	/**
	 * returns some connected edge with its vert behind the slicer plane, or null if none exist
	 */
	private static HalfEdge findInsideEdge(Plane slicer, HalfEdge edge) {
		return findInsideEdgeHelper(slicer, edge, new HashSet<>());
	}
	private static HalfEdge findInsideEdgeHelper(Plane slicer, HalfEdge currentEdge, HashSet<HalfEdge> visitedEdges) {
		if(visitedEdges.contains(currentEdge)) return null;
		if(slicer.contains(currentEdge.vert)) return currentEdge;
		visitedEdges.add(currentEdge);
		HalfEdge faceTraverse = findInsideEdgeHelper(slicer, currentEdge.next, visitedEdges);
		if(faceTraverse != null) return faceTraverse;
		return findInsideEdgeHelper(slicer, currentEdge.twin, visitedEdges);
	}

	/**
	 * finds one edge from each face of the mesh
	 */
	private static HashSet<HalfEdge> generateFaceAnchors(HalfEdge rootEdge) {
		return generateFaceAnchorsHelper(rootEdge, new HashSet<>(), new HashSet<>(), 0);
	}
	private static HashSet<HalfEdge> generateFaceAnchorsHelper(HalfEdge currentEdge, HashSet<HalfEdge> visitedEdges, HashSet<HalfEdge> faceAnchors, int d) {
		if(!visitedEdges.contains(currentEdge)) {
			visitedEdges.add(currentEdge);
			faceAnchors.add(currentEdge);
		}
		if(!visitedEdges.contains(currentEdge.next)) {
			visitedEdges.add(currentEdge.next);
			generateFaceAnchorsHelper(currentEdge.next, visitedEdges, faceAnchors, d+1);
		}
		if(!visitedEdges.contains(currentEdge.twin)) {
			generateFaceAnchorsHelper(currentEdge.twin, visitedEdges, faceAnchors, d+1);
		}
		return faceAnchors;
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

		//todo change this to a triangle_fan
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		for(HalfEdge rootEdge : faceAnchors.toArray(HalfEdge[]::new)) {
			Vec3d v0 = cam.relativize(rootEdge.vert);
			HalfEdge e = rootEdge;
			while(e.next != rootEdge) {
				Vec3d v1 = cam.relativize(e.vert);
				Vec3d v2 = cam.relativize(e.next.vert);
				bufferBuilder.vertex(v0.x, v0.y, v0.z).color(red, green, blue, 40).next();
				bufferBuilder.vertex(v1.x, v1.y, v1.z).color(red, green, blue, 40).next();
				bufferBuilder.vertex(v2.x, v2.y, v2.z).color(red, green, blue, 40).next();
				e = e.next;
			}
		}
		tessellator.draw();

		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(3F);
		bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		HashSet<HalfEdge> renderedEdges = new HashSet<>();
		for(HalfEdge rootEdge : faceAnchors.toArray(HalfEdge[]::new)) {
			HalfEdge e = rootEdge;
			while(e.next != rootEdge) {
				if(!renderedEdges.contains(e.twin)) {
					renderedEdges.add(e);
					Vec3d pos1 = cam.relativize(e.twin.vert);
					Vec3d pos2 = cam.relativize(e.vert);
					Vec3f lineNormal = new Vec3f(pos1.relativize(pos2).normalize());
					bufferBuilder.vertex(pos1.x, pos1.y, pos1.z)
							.color(red, green, blue, 255)
							.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
							.next();
					bufferBuilder.vertex(pos2.x, pos2.y, pos2.z)
							.color(red, green, blue, 255)
							.normal(lineNormal.getX(), lineNormal.getY(), lineNormal.getZ())
							.next();
				}
				e = e.next;
			}
		}
		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.polygonOffset(0f, 0f);
		RenderSystem.disablePolygonOffset();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();

	}

	protected static class HalfEdge {
		Vec3d vert; // destination vertex
		HalfEdge next; // next half-edge around the face
		HalfEdge twin; // oppositely oriented paired half-edge

		public HalfEdge() { }

		public HalfEdge(Vec3d vert, HalfEdge next, HalfEdge twin) {
			this.vert = vert;
			this.next = next;
			this.twin = twin;
		}
	}

	protected static class Plane {
		Vec3d normal;
		Vec3d origin;

		public Plane(Vec3d normal, Vec3d origin) {
			this.normal = normal.normalize();
			this.origin = origin;
		}

		public double distance(Vec3d v) {
			// https://mathinsight.org/distance_point_plane
			return normal.dotProduct(v) - normal.dotProduct(origin);
		}

		// The side that the normal points to is considered "outside"
		public boolean contains(Vec3d v) {
			return distance(v) < 0;
		}

		// returned values are only valid if the given line segment actually intersects the plane
		public Vec3d intersection(Vec3d v1, Vec3d v2) {
			Vec3d lineSeg = v2.subtract(v1);
			double d1 = distance(v1);
			double d2 = distance(v2);
			// The distances have opposite signs, so the total length
			// is the difference of their values, rather than the sum
			double lerp = d1/(d1-d2);
			return v1.add(lineSeg.multiply(lerp));
		}

		public Plane inverse() {
			return new Plane(normal.multiply(-1), origin);
		}
	}

}

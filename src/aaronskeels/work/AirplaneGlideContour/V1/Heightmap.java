package aaronskeels.work.AirplaneGlideContour.V1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import aaronskeels.work.AirplaneGlideContour.duynkdtree.FastKdTree;
import aaronskeels.work.AirplaneGlideContour.duynkdtree.PrioNode;

public class Heightmap {
	FastKdTree<Exemplar_Double> heightKDTree;
	List<Exemplar_Double> allHeights; // This is horribly optimized but that's what happens when you don't code your data structures from the ground up yourself. Should be a FastKdTree traversal implementation.
	double maxHeight;
	
	public Heightmap() {
		heightKDTree = new FastKdTree<>();
		allHeights = new ArrayList<>();
	}
	
	public void addHeight(double x, double y, double height) {
		Exemplar_Double ex = new Exemplar_Double(new double[] {x, y}, height);
		heightKDTree.add(ex);
		allHeights.add(ex);
		if (height > maxHeight)
			maxHeight = height;
	}
	
	public void addCone(double[] pos, double maxRadius, double maxHeight, double dTheta) {
		for (int radius = 0;radius < maxRadius;radius++) {
    		for (double theta = 0;theta < Math.PI*2d;theta += dTheta) {
    			double offsetX = radius * Math.cos(theta);
    			double offsetY = radius * Math.sin(theta);
    			double x = pos[0] + offsetX;
    			double y = pos[1] + offsetY;
    			double height = maxHeight * (1 - (double) radius/maxRadius);
    			addHeight(x, y, height);
    		}
    	}
	}
	
	public void draw(BufferedImage bi, Graphics2D g2d, double scale) {
		for (Exemplar_Double ex : allHeights) {
			double x = ex.getX();
			double y = ex.getY();
//			System.out.println("Starting: (" + x + "," + y + ")");
			x *= scale;
			y *= -scale;
//			System.out.println("Scale: (" + x + "," + y + ")");
			x += bi.getWidth()/2d;
			y += bi.getHeight()/2d;
//			System.out.println("Translate: (" + x + "," + y + ")");
			double colorPercent = ex.getValue() / maxHeight;
			Color color = new Color((float) colorPercent, (float) colorPercent, (float) colorPercent);
			g2d.setColor(color);
			if (x < 0 || x > bi.getWidth()-1 || y < 0 || y > bi.getHeight()-1)
				continue;
			g2d.drawRect((int) x, (int) y, 1, 1);
		}
	}
	
	public double getHeight(double x, double y) {
		//Retrieve available data
		Iterable<PrioNode<Exemplar_Double>> nearestNeighbors =  heightKDTree.search(new double[] {x, y}, 2);
		Iterator<PrioNode<Exemplar_Double>> nearestNeighborsIter = nearestNeighbors.iterator();
		Exemplar_Double p1 = null, p2 = null;
		while(nearestNeighborsIter.hasNext()
				&& (p1 == null || p2 == null)) {
			PrioNode<Exemplar_Double> node = nearestNeighborsIter.next();
			if (node.data.getX() == x && node.data.getX() == y)
				return node.data.getValue();
			
			if (p1 == null)
				p1 = node.data;
			else if (p2 == null)
				p2 = node.data;
		}
		//Handle retrieved Data
		//	If there aren't enough points to have 2 (linear interpolate), assume all points are equal to p1
		if (p2 == null)
			return p1.getValue();
		//	If there are enough points to have 2, interpolate.
		//		Linear interpolate X
		double interpHeight_X = Main.getLinearInterpolatedValue(p1.getX(), p2.getX(), p1.getValue(), p2.getValue(), x);
		//		Linear interpolate y
		double interpHeight_Y = Main.getLinearInterpolatedValue(p1.getY(), p2.getY(), p1.getValue(), p2.getValue(), y);
		//		Average
		return (interpHeight_X+interpHeight_Y)/2d;
	}
	
}

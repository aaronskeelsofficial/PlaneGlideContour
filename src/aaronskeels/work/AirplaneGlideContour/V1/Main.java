package aaronskeels.work.AirplaneGlideContour.V1;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class Main {
	public static final JFrame frame = new JFrame("Jett Plane Glide Contour Testing");
	
	public static void main(String[] args) {
//		//Setup frame config
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        
//        //Finally show frame
//        frame.setVisible(true);
        
        //Initialize vars
        Airplane plane = new Airplane(1, 1, 10, 0, 0, 100, 0);
        Heightmap heightmap = new Heightmap();
        //Flat heightmap
        heightmap.addHeight(0, 0, 0);
        //
        BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB); //Used for full path
        Graphics2D g2d = bi.createGraphics();
        BufferedImage bi2 = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB); //Used for endpoints
        Graphics2D g2d2 = bi2.createGraphics();
        GlideSimulation sim = new GlideSimulation();
        
        //Simulation V1 - Didn't work correct
//        for (double theta = -plane.assumedMaxBankAngleDeg;theta < plane.assumedMaxBankAngleDeg;theta += .5) {
//        	sim.simulate_V1(plane, theta, 1, heightmap);
//            sim.draw(bi, g2d, 2);
//            sim.drawLast(bi2, g2d2, 2);
//        }
        //Simulation V2 - Works but only if airplane is flying north
        if (true) {
	        boolean crossedMidlineThreshold_ccw = false;
	        int iterationCount_ccw = 100;
	        double timeBanking_ccw = 0;
	        while (!crossedMidlineThreshold_ccw && iterationCount_ccw-- > 0) {
	        	crossedMidlineThreshold_ccw = sim.simulate_V2(plane, .5, heightmap, timeBanking_ccw, plane.assumedMaxBankAngleDeg);
	        	sim.draw(bi, g2d, 2);
	        	sim.drawLast(bi2, g2d2, 2);
	        	timeBanking_ccw += 1;
	        }
	        boolean crossedMidlineThreshold_cw = false;
	        int iterationCount_cw = 100;
	        double timeBanking_cw = 0;
	        while (!crossedMidlineThreshold_cw && iterationCount_cw-- > 0) {
	        	crossedMidlineThreshold_cw = sim.simulate_V2(plane, .5, heightmap, timeBanking_cw, -plane.assumedMaxBankAngleDeg);
	        	sim.draw(bi, g2d, 2);
	        	sim.drawLast(bi2, g2d2, 2);
	        	timeBanking_cw += 1;
	        }
        }
        //
        plane.drawPlane(bi, g2d, 10);
        plane.drawPlane(bi2, g2d2, 10);
        g2d.dispose();
        g2d2.dispose();
        
        openMaximizedImage(bi);
        openMaximizedImage(bi2);
	}
	
	/*
	 * Misc Utilities
	 */
	
	public static double clamp(double value, double min, double max) {
		//Clamp values between a min and max (used because high-circle instability of points near edge often times go out of canvas bounds)
		return Math.min(Math.max(value, min), max);
	}
	
	private static final double DEG_TO_RAD = Math.PI / 180d;
	public static double degToRad(double angleInDeg) {
		return angleInDeg * DEG_TO_RAD;
	}
	private static final double RAD_TO_DEG = 180d / Math.PI;
	public static double radToDeg(double angleInRad) {
		return angleInRad * RAD_TO_DEG;
	}
	
	public static double getLinearInterpolatedValue(double x1, double x2, double y1, double y2, double x) {
		//Linear interpolation
		return y1 + (x-x1)*(y2-y1)/(x2-x1);
	}
	
	public static void openMaximizedImage(BufferedImage targetImage) {
		//Shortcut to open a bufferedimage in an easily zoomable/scrollable way
		JFrame frame = new JFrame("Image Preview");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        //Create JScroll
        JScrollPane pane = new JScrollPane();
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        //Create JPanel
        ZoomableJPanel panel = new ZoomableJPanel(targetImage);
        pane.setViewportView(panel);
        
        //Add MouseWheelListener for zooming
        pane.addMouseWheelListener(e -> {
            int notches = -e.getWheelRotation();
            boolean isControlDown = e.isControlDown();

            if (isControlDown) {
                double scaleFactor = Math.pow(1.05, notches);
                double newScale = panel.getScale() * scaleFactor;

                // Limit the scale to a reasonable range
                if (newScale > 0.1 && newScale < 10.0) {
                    panel.setScale(newScale);
                    Dimension scaledSize = new Dimension((int) (panel.getPreferredSize().width * panel.getScale()), (int) (panel.getPreferredSize().height * panel.getScale()));
                    panel.setPreferredSize(scaledSize);
                    pane.revalidate();
                }
            }
        });
        //Adjust scroll speed
        JScrollBar verticalScrollBar = pane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        JScrollBar horizontalScrollBar = pane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(20);
        
        frame.add(pane);
        
        frame.setVisible(true);
	}
	
	public static double unitCircleToPixel(double unitCircleValue, double pixelsPerUnitCircleRadius) {
		//Most values are handled via a -1->1 scale, and when drawing visually we need that converted to 0->pixelresolution
		// An important note is that the Unit Circle Radius is HALF the canvas resolution. That may make some calculations confusing.
		//Starts mapped -1->1
		double x = unitCircleValue;
		//Convert to -Pixel->Pixel
		x *= pixelsPerUnitCircleRadius;
		//Shift origin
		x += pixelsPerUnitCircleRadius;
		//Clamp
		x = clamp(x, 0, 2*pixelsPerUnitCircleRadius-1);
		return x;
	}
}
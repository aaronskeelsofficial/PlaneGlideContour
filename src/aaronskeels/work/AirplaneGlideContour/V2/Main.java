package aaronskeels.work.AirplaneGlideContour.V2;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import aaronskeels.work.AirplaneGlideContour.V1.Heightmap;
import aaronskeels.work.AirplaneGlideContour.V1.ZoomableJPanel;

/*
 * V2 fixes:
 *  - V1 had the issue where I tried my best to avoid coordinate space transformations on the plane and that breaks everything
 *  	This version properly handles coordinate space/vector rotations
 */

public class Main {
	public static final String DOWNLOAD_PATH = System.getProperty("user.home") + File.separator + "Downloads" + File.separator;
	public static final JFrame frame = new JFrame("Jett Plane Glide Contour Testing");
	
	public static void main(String[] args) {
//		//Setup frame config
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        
//        //Finally show frame
//        frame.setVisible(true);
        
        //Initialize vars
        Airplane plane = new Airplane(40, 250, 5, 0, 0, ftToM(20000), new XYUnitVector(1, 0));
        Heightmap heightmap = new Heightmap();
        //Flat heightmap
        heightmap.addHeight(0, 0, 0);
        //
        BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB); //Used for full path
        Graphics2D g2d = bi.createGraphics();
        BufferedImage bi2 = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB); //Used for endpoints
        Graphics2D g2d2 = bi2.createGraphics();
        GlideSimulation sim = new GlideSimulation();
        
        //Simulation V3
//        sim.simulate_V3(plane, 1, heightmap, 600, plane.assumedMaxBankAngleDeg);
        if (true) {
	        int iterationCount_ccw = 10000;
	        double timeBanking_ccw = 0;
	        GlideSimulationResult result_ccw = null;
	        while ((result_ccw == null || (!result_ccw.crossedMidline && !result_ccw.crashedStillTurning)) && iterationCount_ccw-- > 0) {
	        	result_ccw = sim.simulate_V3(plane, 1, heightmap, timeBanking_ccw, plane.assumedMaxBankAngleDeg);
	        	sim.draw(bi, g2d, 1d/1000d);
	        	sim.drawLast(bi2, g2d2, 1d/1000d);
	        	timeBanking_ccw += 10;
	        }
	        int iterationCount_cw = 10000;
	        double timeBanking_cw = 0;
	        GlideSimulationResult result_cw = null;
	        while ((result_cw == null || (!result_cw.crossedMidline && !result_cw.crashedStillTurning)) && iterationCount_cw-- > 0) {
	        	result_cw = sim.simulate_V3(plane, 1, heightmap, timeBanking_cw, -plane.assumedMaxBankAngleDeg);
	        	sim.draw(bi, g2d, 1d/1000d);
	        	sim.drawLast(bi2, g2d2, 1d/1000d);
	        	timeBanking_cw += 10;
	        }
        }
//        sim.draw(bi, g2d, 1d/1000d);
//    	sim.drawLast(bi2, g2d2, 1d/1000d);
        //
        plane.drawPlane(bi, g2d, 7);
        plane.drawPlane(bi2, g2d2, 7);
        g2d.dispose();
        g2d2.dispose();
        
        openMaximizedImage(bi);
        openMaximizedImage(bi2);
        
        try {
			ImageIO.write(bi, "png", new File(DOWNLOAD_PATH + "Glide_" + plane.assumedMaxBankAngleDeg + "_" + plane.getForwardUnitVector() + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private static final double FT_TO_M = .304799990246;
	public static double ftToM(double ft) {
		return ft*FT_TO_M;
	}
	public static double mToFt(double m) {
		return m/FT_TO_M;
	}
	
	public static double getLinearInterpolatedValue(double x1, double x2, double y1, double y2, double x) {
		//Linear interpolation
		return y1 + (x-x1)*(y2-y1)/(x2-x1);
	}
	
	private static final double KNOT_TO_MPS = .51 + 4d/900d;
	public static double knotToMps(double knot) {
		return knot * KNOT_TO_MPS;
	}
	public static double mpsToKnot(double mps) {
		return mps / KNOT_TO_MPS;
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
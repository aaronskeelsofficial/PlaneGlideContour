package aaronskeels.work.AirplaneGlideContour.V4;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import aaronskeels.work.AirplaneGlideContour.V1.Exemplar_Double;
import aaronskeels.work.AirplaneGlideContour.V2.XYUnitVector;
import aaronskeels.work.AirplaneGlideContour.V3.GlideSimulationResult;
import aaronskeels.work.AirplaneGlideContour.V3.Heightmap;
import aaronskeels.work.AirplaneGlideContour.V3.ImageCopyToClipboard;

/*
 * V4 fixes:
 *  - V3 was a single simulation. V4 aims to "animate" the simulation and test how many Hz can be achieved.
 */

public class Main {
	public static final String DOWNLOAD_PATH = System.getProperty("user.home") + File.separator + "Downloads" + File.separator;
	public static final JFrame frame = new JFrame("Jett Plane Glide Contour Testing");
	public static final BufferedImage BLACK = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
	
	public static void main(String[] args) {
//		XYUnitVector v = new XYUnitVector(0, 1); //Up
		XYUnitVector v = new XYUnitVector(1, 0); //Right
//		XYUnitVector v = new XYUnitVector(Math.sqrt(2)/2d, -Math.sqrt(2)/2d); //Turned
		double dt = 100;
        Airplane[] planes = new Airplane[] {
        		new Airplane(40, 250, 20, 0, 0, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*1, 0+v.y*250*dt*1, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*2, 0+v.y*250*dt*2, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*3, 0+v.y*250*dt*3, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*4, 0+v.y*250*dt*4, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*5, 0+v.y*250*dt*5, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*6, 0+v.y*250*dt*6, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*7, 0+v.y*250*dt*7, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*8, 0+v.y*250*dt*8, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*9, 0+v.y*250*dt*9, ftToM(20000), v),
        		new Airplane(40, 250, 20, 0+v.x*250*dt*10, 0+v.y*250*dt*10, ftToM(20000), v)
		};
        Heightmap heightmap = new Heightmap();
        heightmap.addHeight(0, 0, 0);
        heightmap.addCone(new double[] {0, -150000}, 100000, 3000, 1000, Math.PI/100d*2d);
        //
        BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB); //Used for full path
        Graphics2D g2d = bi.createGraphics();
        BufferedImage heightmapBI = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D heightmapg2d = heightmapBI.createGraphics();
        heightmap.draw(bi, g2d, 1d/1000d);
        heightmapg2d.dispose();
        //
        zoomableJPanel = openMaximizedImage(bi);
        Thread thread = new Thread(new Runnable() {public void run() {
        	AnimateFrames(planes, null, bi, g2d, heightmapBI);
		}});
        thread.start();
	}
	
	private static ZoomableJPanel zoomableJPanel = null;
	private static void AnimateFrames(Airplane[] planes, Heightmap heightmap, BufferedImage bi, Graphics2D g2d, BufferedImage heightBI) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long startTime = System.currentTimeMillis();
		@SuppressWarnings("unused")
		long curTime = startTime;
		int i = 0;
		while ((curTime = System.currentTimeMillis()) - startTime < 1000 && i++ < 2000) {
			Airplane plane = planes[i % planes.length];
//		for (Airplane plane : planes) {
			System.out.println("drawing");
			GlideSimulation sim = new GlideSimulation();
			List<double[]> organizedPointsForContour = new ArrayList<>();
	        int iterationCount_ccw = 10000;
	        double timeBankingDelta = 4;
	        double timeBanking_ccw = 0;
	        GlideSimulationResult result_ccw = null;
	        while ((result_ccw == null || (!result_ccw.crossedMidline && !result_ccw.crashedStillTurning)) && iterationCount_ccw-- > 0) {
	        	result_ccw = sim.simulate_V4(plane, 1, heightmap, timeBanking_ccw, plane.assumedMaxBankAngleDeg);
	        	if (!result_ccw.crossedMidline)
	        		organizedPointsForContour.add(0, result_ccw.crashedPos);
	        	timeBanking_ccw += timeBankingDelta;
	        }
	        //
	        int iterationCount_cw = 10000;
	        double timeBanking_cw = 0;
	        GlideSimulationResult result_cw = null;
	        while ((result_cw == null || (!result_cw.crossedMidline && !result_cw.crashedStillTurning)) && iterationCount_cw-- > 0) {
	        	result_cw = sim.simulate_V4(plane, 1, heightmap, timeBanking_cw, -plane.assumedMaxBankAngleDeg);
	        	if (timeBanking_cw != 0 && !result_cw.crossedMidline) //Skip adding this because the data point will be duplicated on the second run
	        		organizedPointsForContour.add(result_cw.crashedPos);
	        	timeBanking_cw += timeBankingDelta;
	        }
	        GlideSimulation.drawContour(bi, g2d, 1d/1000d, organizedPointsForContour);
	        /*
	         * Contour specifications:
	         * - Cut the turning time = 0 index from second run because it's a duplicate
	         * - Flip the order of the + turning angle loop so indices make a closed loop by end
	         * - Cut the run of + angle and - angle which crosses the midline (last run) because it often is contained within the radius we actually care about
	         */
	        //
	        plane.drawPlane(bi, g2d, 12, 1d/1000d);
	        
	        g2d.setColor(Color.white);
	        g2d.fillRect(0, 0, 50, 50);
	        g2d.setColor(Color.black);
	        g2d.drawString(i + "", 20, 20);
	        
        	zoomableJPanel.setImage(bi);
		}
//		g2d.dispose();
	}
	
	/*
	 * Misc Utilities
	 */
	
	public static double bilinearInterpolation(double[] p1, double[] p2, double x, double y, Heightmap heightmap) {
		double x1 = Math.min(p1[0], p2[0]);
		double x2 = Math.max(p1[0], p2[0]);
		double y1 = Math.min(p1[1], p2[1]);
		double y2 = Math.max(p1[1], p2[1]);
		double Q11 = heightmap.getHeight(x1, y1);
		double Q12 = heightmap.getHeight(x1, y2);
		double Q21 = heightmap.getHeight(x2, y1);
		double Q22 = heightmap.getHeight(x2, y2);
		double R1 = Q11 * (x2 - x)/(x2 - x1) + Q21 * (x - x1)/(x2 - x1);
		double R2 = Q12 * (x2 - x)/(x2 - x1) + Q22 * (x - x1)/(x2 - x1);
		return R1 * (y2 - y)/(y2 - y1) + R2 * (y - y1)/(y2 - y1);
	}
	
	public static double clamp(double value, double min, double max) {
		//Clamp values between a min and max (used because high-circle instability of points near edge often times go out of canvas bounds)
		return Math.min(Math.max(value, min), max);
	}
	
	public static void clearImage(BufferedImage bi, Graphics2D g2d, Color overrideColor) {
		//Wipe image to color
		g2d.setColor(overrideColor);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
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
		if (x1 == x2) {
			return y1;
		}
		//Linear interpolation
		return y1 + (x-x1)*(y2-y1)/(x2-x1);
	}
	
	public static double getNearestNeighborInterpolatedValue(Exemplar_Double p1, Exemplar_Double p2, double x, double y) {
		double deltaX1 = p1.getX() - x;
		double deltaY1 = p1.getY() - y;
		double d1 = Math.sqrt(deltaX1*deltaX1 + deltaY1*deltaY1);
		double deltaX2 = p2.getX() - x;
		double deltaY2 = p2.getY() - y;
		double d2 = Math.sqrt(deltaX2*deltaX2 + deltaY2*deltaY2);
		double w1 = 1 / d1;
		double w2 = 1 / d2;
		return (w1 * p1.getValue() + w2 * p2.getValue()) / (w1 + w2);
	}
	
	private static final double KNOT_TO_MPS = .51 + 4d/900d;
	public static double knotToMps(double knot) {
		return knot * KNOT_TO_MPS;
	}
	public static double mpsToKnot(double mps) {
		return mps / KNOT_TO_MPS;
	}
	
	public static ZoomableJPanel openMaximizedImage(BufferedImage targetImage) {
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
        //Add right click listener for copying to clipboard
        pane.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if (e.getButton() == MouseEvent.BUTTON3) {
        			System.out.println("Copied image to clipboard.");
        			ImageCopyToClipboard.setClipboard(panel.getImage());
        			JOptionPane.showMessageDialog(frame, "Copied to clipboard");
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
        
        return panel;
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
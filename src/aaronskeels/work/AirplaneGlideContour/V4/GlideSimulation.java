package aaronskeels.work.AirplaneGlideContour.V4;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import aaronskeels.work.AirplaneGlideContour.V1.Main;
import aaronskeels.work.AirplaneGlideContour.V2.XYUnitVector;
import aaronskeels.work.AirplaneGlideContour.V3.GlideSimulationResult;
import aaronskeels.work.AirplaneGlideContour.V3.Heightmap;

public class GlideSimulation {
	private List<double[]> locTrail; //[x, y, isTurning]
	private List<double[]> lastPoints; //[x, y]
//	private Color color, color2;
	
	public GlideSimulation() {
		locTrail = new ArrayList<>();
		lastPoints = new ArrayList<>();
		//Generate color
//		Random rand = new Random();
//		color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
//		color2 = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}
	
	public GlideSimulationResult simulate_V4(Airplane plane, double simulationTimestep, Heightmap heightmap, double timeBanking, double assumedBankAngleDeg) {
		List<double[]> flightPoints = new ArrayList<>();
		boolean isHeightmapNull = heightmap == null;
		//
		double[] startingPos = plane.getPos();
		double forwardVelo = plane.assumedGlideSpeedMs;
		double originalUprightDescentVelo = forwardVelo / plane.forwardVeloVsDropVeloRatio;
		XYUnitVector originalForwardVector = plane.getForwardUnitVector().clone();
		//
		double lastCurX = 0, lastCurY = 0, lastCurHeight = 0, lastDisplacementVectorThetaRad = 0;
		//
		double timeAccumulated = 0;
		double angularVelocityRad = (timeAccumulated >= timeBanking) ? 0 : calculateAngularVelocityRad_method1(forwardVelo, assumedBankAngleDeg);
		XYUnitVector forwardUnitVector = plane.getForwardUnitVector().clone();
		double xVelo = forwardUnitVector.x * forwardVelo * simulationTimestep;
		double yVelo = forwardUnitVector.y * forwardVelo * simulationTimestep;
		double curX = startingPos[0];
		double curY = startingPos[1];
		double descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedBankAngleDeg);
		double curHeight = startingPos[2];
		double displacementVectorThetaRad = 0;
		boolean displacementVectorThetaCrossedMidline = false;
		//
		double heightmapHeight = isHeightmapNull ? 0 : heightmap.getHeight(curX, curY);
//		System.out.println("CP1 (" + curX + "," + curY + "," + curHeight + ") " + heightmapHeight);
		while (curHeight > heightmapHeight) {
//			System.out.println("CP2a (" + curX + "," + curY + "," + curHeight + ") " + heightmapHeight);
			//Log location
			locTrail.add(new double[] {curX, curY, (double) ((timeAccumulated >= timeBanking) ? 1 : -1)});
			flightPoints.add(new double[] {curX, curY});
			//Transfer old vars
			lastCurX = curX;
			lastCurY = curY;
			lastCurHeight = curHeight;
			lastDisplacementVectorThetaRad = displacementVectorThetaRad;
			//Run simulation step
			curX += xVelo;
			curY += yVelo;
			curHeight -= descentVelo * simulationTimestep;
			//Midline check
			XYUnitVector displacementUnitVector = new XYUnitVector(curX-startingPos[0], curY-startingPos[1]).normalize();
			displacementVectorThetaRad = originalForwardVector.getAngleFromVectorRad(displacementUnitVector);
			forwardUnitVector.rotate(angularVelocityRad * simulationTimestep);
			if (!isHeightmapNull)
				heightmapHeight = heightmap.getHeight(curX, curY);
			
			//Update vars
			timeAccumulated += simulationTimestep;
			angularVelocityRad = (timeAccumulated >= timeBanking) ? 0 : angularVelocityRad;
			xVelo = forwardUnitVector.x * forwardVelo * simulationTimestep;
			yVelo = forwardUnitVector.y * forwardVelo * simulationTimestep;
			descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedBankAngleDeg);
			if (Math.abs(displacementVectorThetaRad) < Math.abs(lastDisplacementVectorThetaRad)) {
				displacementVectorThetaCrossedMidline = true;
				return new GlideSimulationResult(flightPoints, displacementVectorThetaCrossedMidline, new double[] {curX, curY}, !(timeAccumulated >= timeBanking), timeAccumulated);
			}
//			System.out.println("CP2b (" + curX + "," + curY + "," + curHeight + ") " + heightmapHeight);
		}
		//
		//	Loop will have ended with the state manager beyond the right point.
		//	Find out the height % from past point to ground of overall distance travelled
		//	Update x and y based on that percent
		double percentToUse = (lastCurHeight - heightmapHeight) / (lastCurHeight - curHeight);
		curX = lastCurX + (curX-lastCurX) * percentToUse;
		curY = lastCurY + (curY-lastCurY) * percentToUse;
		locTrail.add(new double[] {curX, curY, (double) ((timeAccumulated >= timeBanking) ? 1 : -1)});
		lastPoints.add(new double[] {curX, curY});
		flightPoints.add(lastPoints.get(lastPoints.size()-1));
//		System.out.println("Crashed @ (" + curX + "," + curY + "," + heightmapHeight + ") t = " + timeAccumulated);
		return new GlideSimulationResult(flightPoints, displacementVectorThetaCrossedMidline, new double[] {curX, curY}, !(timeAccumulated >= timeBanking), timeAccumulated);
	}
	
	public void draw(BufferedImage bi, Graphics2D g2d, double scale) {
		draw(bi, g2d, scale, locTrail);
	}
	public static void draw(BufferedImage bi, Graphics2D g2d, double scale, List<double[]> locTrail) {
		for (double[] pos : locTrail) {
			drawPoint(pos, bi, g2d, scale, (pos[2] == 1) ? Color.magenta : Color.blue);
		}
	}
	
	public void drawContour(BufferedImage bi, Graphics2D g2d, double scale) {
		drawContour(bi, g2d, scale, lastPoints);
	}
	public static void drawContour(BufferedImage bi, Graphics2D g2d, double scale, List<double[]> lastPoints) {
		double numOfSteps = 100d;
		Random rand = new Random();
		Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
		for (int index = 0;index < lastPoints.size();index++) {
			double[] p1 = lastPoints.get(index);
			double[] p2 = (index < lastPoints.size()-1) ? lastPoints.get(index+1) : lastPoints.get(0);
			double[] bL = new double[] {Math.min(p1[0], p2[0]), Math.min(p1[1], p1[1])};
			double[] tR = new double[] {Math.max(p1[0], p2[0]), Math.max(p1[1], p2[1])};
			double dX = (tR[0] - bL[0]) / numOfSteps;
			for (double x = bL[0];x < tR[0];x += dX) {
				double interpY = Main.getLinearInterpolatedValue(p1[0], p2[0], p1[1], p2[1], x);
				drawPoint(new double[] {x, interpY}, bi, g2d, scale, color);
			}
			double dY = (tR[1] - bL[1]) / numOfSteps;
			for (double y = bL[1];y < tR[1];y += dY) {
				double interpX = Main.getLinearInterpolatedValue(p1[1], p2[1], p1[0], p2[0], y);
				drawPoint(new double[] {interpX, y}, bi, g2d, scale, color);
			}
		}
	}
	
	public void drawLast(BufferedImage bi, Graphics2D g2d, double scale) {
		drawLast(bi, g2d, scale, locTrail);
	}
	public static void drawLast(BufferedImage bi, Graphics2D g2d, double scale, List<double[]> locTrail) {
		drawPoint(locTrail.get(locTrail.size()-1), bi, g2d, scale, Color.blue);
	}
	
	public static void drawPoint(double[] pos, BufferedImage bi, Graphics2D g2d, double scale, Color color) {
		g2d.setColor(color);
		//Scale distance into pixel-space
		double xScaled = pos[0]*scale;
		double yScaled = pos[1]*-scale;
		//Translate so (0,0) is centered
		int xTranslated = (int) (bi.getWidth()/2d + xScaled);
		int yTranslated = (int) (bi.getHeight()/2d + yScaled);
		if (xTranslated < 0 || xTranslated > bi.getWidth()-1 || yTranslated < 0 || yTranslated > bi.getHeight()-1)
			return;
		
		g2d.drawRect(xTranslated, yTranslated, 1, 1);
	}
	
	/*
	 * Static functions
	 */
	
	public static double calculateBankInducedGlideRatioChange(double uprightDescentVelo, double bankAngleDeg) {
		//See note #1 at the bottom about how banking alters glide ratio
		return uprightDescentVelo;
	}
	
	public static double calculateAngularVelocityRad_method1(double velocity, double bankAngleDeg) {
		double angleRad = Main.degToRad(bankAngleDeg);
//		double turningRadius = (velocity*velocity) / (9.80665*Math.tan(angleRad)); //r = v^2 / (g*tan(theta))
//		double angularVelocity = velocity / turningRadius; //v = r*omega
//		return angularVelocity;
		return 9.80665d * Math.tan(angleRad) / velocity;
	}
	
	public static void log(List<Double[]> locs) {
		String s = "";
		for (Double[] d : locs) {
			s += "[" + d[0] + "," + d[1] + "],";
		}
		System.out.println(s);
	}
	
	///// Important simulation notes learned from the internet:
	/*
	 * 1. While banking, the glide ratio is not altered by experienced pilots as they adjust the controls to keep it consistent (though it can change if they don't).
	 */
	
}

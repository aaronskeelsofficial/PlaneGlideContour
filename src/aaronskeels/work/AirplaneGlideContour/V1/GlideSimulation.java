package aaronskeels.work.AirplaneGlideContour.V1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GlideSimulation {
	private List<Double[]> locTrail;
	private Color color;
	
	public GlideSimulation() {
		locTrail = new ArrayList<>();
		//Generate color
		Random rand = new Random();
		color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}
	
	public void simulate_V1(Airplane plane, double assumedTurningAngleDeg, double simulationTimestep, Heightmap heightmap) {
		/*
		 * This version of the algorithm sweeps through turning angles while holding it constant throughout the entire bank.
		 * I don't believe this is right.
		 * I believe we want to only bank at the maximum banking angle when banking, and instead sweep through how long we bank for
		 */
		double[] startingPos = plane.getPos();
		double forwardVelo = plane.assumedGlideSpeed;
		double originalUprightDescentVelo = forwardVelo / plane.forwardVeloVsDropVeloRatio;
		double originalAngleFromYDeg = plane.getForwardAngleFromYDeg();
		double angularVelocity = calculateAngularVelocity_method1(forwardVelo, assumedTurningAngleDeg);
		//
		double lastCurX = 0, lastCurY = 0, lastCurHeight = 0;
		//
		double curAngleFromYDeg = originalAngleFromYDeg;
		double xVelo = -1*Math.sin(Main.degToRad(curAngleFromYDeg)) * forwardVelo * simulationTimestep;
		double yVelo = Math.cos(Main.degToRad(curAngleFromYDeg)) * forwardVelo * simulationTimestep;
		double curX = startingPos[0];
		double curY = startingPos[1];
		double descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedTurningAngleDeg);
		double curHeight = startingPos[2];
		//
		double heightmapHeight = heightmap.getHeight(curX, curY);
		while (curHeight > heightmapHeight) {
			//Log location
			locTrail.add(new Double[] {curX, curY});
			//Transfer old vars
			lastCurX = curX;
			lastCurY = curY;
			lastCurHeight = curHeight;
			//Run simulation step
			curAngleFromYDeg += angularVelocity * simulationTimestep;
			curX += xVelo;
			curY += yVelo;
			curHeight -= descentVelo * simulationTimestep;
			//Update vars
			xVelo = -1*Math.sin(Main.degToRad(curAngleFromYDeg)) * forwardVelo * simulationTimestep;
			yVelo = Math.cos(Main.degToRad(curAngleFromYDeg)) * forwardVelo * simulationTimestep;
			descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedTurningAngleDeg);
		}
		//
		//	Loop will have ended with the state manager beyond the right point.
		//	Find out the height % from past point to ground of overall distance travelled
		//	Update x and y based on that percent
		double percentToUse = (lastCurHeight - heightmapHeight) / (lastCurHeight - curHeight);
		curX = (lastCurX - curX) * percentToUse;
		curY = (lastCurY - curY) * percentToUse;
		//
//		log(locTrail);
	}
	
	public boolean simulate_V2(Airplane plane, double simulationTimestep, Heightmap heightmap, double timeBanking, double assumedBankAngle) {
		//Cleanse input
		// - If the assumedBankAngle is negative, actually simulate the positive version and shift variables across the horizontal axis
		boolean bankAngleWasNegative = assumedBankAngle < 0;
		if (bankAngleWasNegative) {
			assumedBankAngle *= -1;
		}
		
		
		double[] startingPos = plane.getPos();
		double forwardVelo = plane.assumedGlideSpeed;
		double originalUprightDescentVelo = forwardVelo / plane.forwardVeloVsDropVeloRatio;
		double originalAngleFromYDeg = plane.getForwardAngleFromYDeg();
		//
		double lastCurX = 0, lastCurY = 0, lastCurHeight = 0;
		//
		double timeAccumulated = 0;
		double angularVelocity = calculateAngularVelocity_method1(forwardVelo, assumedBankAngle);
		double curAngleFromYDeg = originalAngleFromYDeg;
		double xVelo = Math.cos(Main.degToRad(curAngleFromYDeg+90)) * forwardVelo * simulationTimestep;
		double yVelo = Math.sin(Main.degToRad(curAngleFromYDeg+90)) * forwardVelo * simulationTimestep;
		double curX = startingPos[0];
		double curY = startingPos[1];
		double descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedBankAngle);
		double curHeight = startingPos[2];
		//
		System.out.println("curX: " + curX + " curY: " + curY);
		double heightmapHeight = bankAngleWasNegative ? heightmap.getHeight(-curX, curY) : heightmap.getHeight(curX, curY);
		System.out.println(curHeight + " -> " + heightmapHeight);
		while (curHeight > heightmapHeight) {
//			System.out.println(curHeight);
			//Log location
			locTrail.add(new Double[] {curX * (bankAngleWasNegative ? -1 : 1), curY});
			//Transfer old vars
			lastCurX = curX;
			lastCurY = curY;
			lastCurHeight = curHeight;
			//Run simulation step
			curAngleFromYDeg += angularVelocity * simulationTimestep;
			curX += xVelo;
			curY += yVelo;
			curHeight -= descentVelo * simulationTimestep;
//			heightmapHeight = bankAngleWasNegative ? heightmap.getHeight(-curX, -curY) : heightmap.getHeight(curX, curY); //TODO
			
			//Update vars
			timeAccumulated += simulationTimestep;
			angularVelocity = (timeAccumulated >= timeBanking) ? 0 : angularVelocity;
			xVelo = Math.cos(Main.degToRad(curAngleFromYDeg+90)) * forwardVelo * simulationTimestep;
			yVelo = Math.sin(Main.degToRad(curAngleFromYDeg+90)) * forwardVelo * simulationTimestep;
			descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedBankAngle);
		}
		//
		//	Loop will have ended with the state manager beyond the right point.
		//	Find out the height % from past point to ground of overall distance travelled
		//	Update x and y based on that percent
		double percentToUse = (lastCurHeight - heightmapHeight) / (lastCurHeight - curHeight);
		curX = lastCurX + (curX-lastCurX) * percentToUse;
		curY = lastCurY + (curY-lastCurY) * percentToUse;
		locTrail.add(new Double[] {curX * (bankAngleWasNegative ? -1 : 1), curY});
		//Midline check
		//	Check if passed midline to cut recursion
		//	Version 1 - Doesn't work. Doesn't actually target midline
//		if (Math.abs(curAngleFromYDeg - originalAngleFromYDeg) > 180)
//			return true;
		//	Version 2
		System.out.println("Point: (" + (curX-startingPos[0]) + "," + (curY-startingPos[1]) + ")");
		if (assumedBankAngle > 0) {
			double displacementTheta = Math.atan2((curY-startingPos[1]), curX - startingPos[0]);
			if (displacementTheta < 0)
				displacementTheta += 2 * Math.PI;
			displacementTheta -= Math.PI/2d;
			System.out.println("   " + Main.radToDeg(displacementTheta) + "-" + Main.degToRad(originalAngleFromYDeg) + "=" + (displacementTheta-Main.degToRad(originalAngleFromYDeg)));
			if (displacementTheta - Main.degToRad(originalAngleFromYDeg) >= Math.PI)
				return true;
		}
		//
		return false;
	}
	
	public void draw(BufferedImage bi, Graphics2D g2d, double scale) {
		g2d.setColor(color);
		for (Double[] pos : locTrail) {
			drawPoint(pos, bi, g2d, scale);
		}
	}
	public void drawLast(BufferedImage bi, Graphics2D g2d, double scale) {
		g2d.setColor(color);
		drawPoint(locTrail.get(locTrail.size()-1), bi, g2d, scale);
	}
	private void drawPoint(Double[] pos, BufferedImage bi, Graphics2D g2d, double scale) {
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
	
	public static double calculateAngularVelocity_method1(double velocity, double bankAngleDeg) {
		double angleRad = bankAngleDeg * Math.PI / 180d;
		double turningRadius = (velocity*velocity) / (9.80665*Math.tan(angleRad)); //r = v^2 / (g*tan(theta))
		double angularVelocity = velocity / turningRadius; //v = r*omega
		return angularVelocity;
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

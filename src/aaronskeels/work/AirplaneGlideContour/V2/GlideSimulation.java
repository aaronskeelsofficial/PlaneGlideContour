package aaronskeels.work.AirplaneGlideContour.V2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import aaronskeels.work.AirplaneGlideContour.V1.Heightmap;
import aaronskeels.work.AirplaneGlideContour.V1.Main;

public class GlideSimulation {
	private List<Double[]> locTrail; //[x, y, isTurning]
	private Color color, color2;
	
	public GlideSimulation() {
		locTrail = new ArrayList<>();
		//Generate color
		Random rand = new Random();
		color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
		color2 = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}
	
	public GlideSimulationResult simulate_V3(Airplane plane, double simulationTimestep, Heightmap heightmap, double timeBanking, double assumedBankAngleDeg) {
		//Cleanse input
		// - If the assumedBankAngle is negative, actually simulate the positive version and shift variables across the horizontal axis
//		boolean bankAngleWasNegative = assumedBankAngleDeg < 0;
//		if (bankAngleWasNegative) {
//			assumedBankAngleDeg *= -1;
//		}
		
		
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
		double heightmapHeight = heightmap.getHeight(curX, curY);
		while (curHeight > heightmapHeight) {
//			System.out.println(curHeight);
			//Log location
			locTrail.add(new Double[] {curX, curY, (double) ((timeAccumulated >= timeBanking) ? 1 : -1)});
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
			heightmapHeight = heightmap.getHeight(curX, curY); //TODO
			
			//Update vars
			timeAccumulated += simulationTimestep;
			angularVelocityRad = (timeAccumulated >= timeBanking) ? 0 : angularVelocityRad;
			xVelo = forwardUnitVector.x * forwardVelo * simulationTimestep;
			yVelo = forwardUnitVector.y * forwardVelo * simulationTimestep;
			descentVelo = calculateBankInducedGlideRatioChange(originalUprightDescentVelo, assumedBankAngleDeg);
			if (Math.abs(displacementVectorThetaRad) < Math.abs(lastDisplacementVectorThetaRad)) {
				displacementVectorThetaCrossedMidline = true;
				return new GlideSimulationResult(displacementVectorThetaCrossedMidline, new double[] {curX, curY}, !(timeAccumulated >= timeBanking), timeAccumulated);
			}
		}
		//
		//	Loop will have ended with the state manager beyond the right point.
		//	Find out the height % from past point to ground of overall distance travelled
		//	Update x and y based on that percent
		double percentToUse = (lastCurHeight - heightmapHeight) / (lastCurHeight - curHeight);
		curX = lastCurX + (curX-lastCurX) * percentToUse;
		curY = lastCurY + (curY-lastCurY) * percentToUse;
		locTrail.add(new Double[] {curX, curY, (double) ((timeAccumulated >= timeBanking) ? 1 : -1)});
		System.out.println("Crashed @ (" + curX + "," + curY + " t = " + timeAccumulated);
		return new GlideSimulationResult(displacementVectorThetaCrossedMidline, new double[] {curX, curY}, !(timeAccumulated >= timeBanking), timeAccumulated);
	}
	
	public void draw(BufferedImage bi, Graphics2D g2d, double scale) {
		for (Double[] pos : locTrail) {
			drawPoint(pos, bi, g2d, scale);
		}
	}
	public void drawLast(BufferedImage bi, Graphics2D g2d, double scale) {
		g2d.setColor(color);
		drawPoint(locTrail.get(locTrail.size()-1), bi, g2d, scale);
	}
	private void drawPoint(Double[] pos, BufferedImage bi, Graphics2D g2d, double scale) {
		g2d.setColor(pos[2] == 1 ? color : color2);
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

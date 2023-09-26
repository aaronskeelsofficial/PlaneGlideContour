package aaronskeels.work.AirplaneGlideContour.V2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Airplane {
	public double forwardVeloVsDropVeloRatio; // Aka glide ratio, but I don't feel that definition is comprehensive enough towards what is happening
	public double assumedGlideSpeedMs; //[m/s] Assumed forward velocity throughout glide
	public double assumedMaxBankAngleDeg; //[deg] Assumed max bank angle. If so sharp circles are performed, simulations may use smaller angle to define largest radius contour
	private double x, y, height; //[m] GPS coordinates of actual body position
	private XYUnitVector forwardUnitVector; //[x, y]
	
	public Airplane(double forwardVeloVsDropVeloRatio, double assumedGlideSpeedMs, double assumedMaxBankAngleDeg, double curX, double curY, double curHeight, XYUnitVector forwardUnitVector) {
		this.forwardVeloVsDropVeloRatio = forwardVeloVsDropVeloRatio;
		this.assumedGlideSpeedMs = assumedGlideSpeedMs;
		this.assumedMaxBankAngleDeg = assumedMaxBankAngleDeg;
		this.x = curX;
		this.y = curY;
		this.height = curHeight;
		this.forwardUnitVector = forwardUnitVector;
	}
	
	public void drawPlane(BufferedImage bi, Graphics2D g2d, int radius) {
		g2d.setColor(Color.white);
		g2d.fillOval((int) (bi.getWidth()/2d - radius), (int) (bi.getHeight()/2d - radius), (int) (2d*radius), (int) (2d*radius));
	}
	
	public XYUnitVector getForwardUnitVector() {
		return forwardUnitVector;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getHeight() {
		return height;
	}
	public double[] getPos() {
		return new double[] {x, y, height};
	}
	
	public void updateForwardUnitVector(XYUnitVector v) {
		//Cleanse input by normalizing first just in case
		v.normalize();
		forwardUnitVector = v;
	}
	public void updatePos(double x, double y, double height) {
		updatePos(new double[] {x, y, height});
	}
	public void updatePos(double[] pos) {
		this.x = pos[0];
		this.y = pos[1];
		this.height = pos[2];
	}
}

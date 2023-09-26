package aaronskeels.work.AirplaneGlideContour.V2;

import aaronskeels.work.AirplaneGlideContour.V1.Main;

public class XYUnitVector {
	public double x, y;
	
	public XYUnitVector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public XYUnitVector clone() {
		return new XYUnitVector(x, y);
	}
	
	public double dot(XYUnitVector otherV) {
		return x*otherV.x + y*otherV.y;
	}
	
	public double getAngleFromVectorRad(XYUnitVector otherV) {
		double dotProduct = dot(otherV);
		double denom = getMagnitude()*otherV.getMagnitude();
		double thetaRad = Math.acos(dotProduct/denom);
		return Math.abs(thetaRad);
	}
	
	public double getMagnitude() {
		return Math.sqrt(x*x + y*y);
	}
	
	public XYUnitVector rotate(double ccwThetaRad) {
		double xCache = x;
		double yCache = y;
		x = xCache*Math.cos(ccwThetaRad) - yCache*Math.sin(ccwThetaRad);
		y = xCache*Math.sin(ccwThetaRad) + yCache*Math.cos(ccwThetaRad);
		return this;
	}
	
	public XYUnitVector normalize() {
		double magnitude = Math.sqrt(x*x + y*y);
		x /= magnitude;
		y /= magnitude;
		return this;
	}
	
	public String toString() {
		return "[" + x + "," + y + "]";
	}
	
	public static XYUnitVector generateFromThetaFromYDeg(double forwardAngleFromYDeg) {
		double angleInRad = Main.degToRad(forwardAngleFromYDeg+90d);
		return new XYUnitVector(Math.cos(angleInRad), Math.sin(angleInRad));
	}
	
}

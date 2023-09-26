package aaronskeels.work.AirplaneGlideContour.V1;

import aaronskeels.work.AirplaneGlideContour.duynkdtree.Exemplar;

public class Exemplar_Double extends Exemplar{
	private double value;
	
	public Exemplar_Double(double[] domain, double value) {
		super(domain);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public double getX() {
		return domain[0];
	}
	public double getY() {
		return domain[1];
	}
	
	public String getPosString() {
		return "[" + domain[0] + "," + domain[1] + "]";
	}
	
	public String toString() {
		return getPosString() + "(" + value + ")";
	}

}

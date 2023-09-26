package aaronskeels.work.AirplaneGlideContour.V3;

import java.util.List;

public class GlideSimulationResult {
	public List<double[]> flightPoints;
	public boolean crossedMidline;
	public double[] crashedPos;
	public boolean crashedStillTurning;
	public double crashedTime;
	
	public GlideSimulationResult(List<double[]> flightPoints, boolean crossedMidline, double[] crashedPos, boolean crashedStillTurning, double crashedTime) {
		this.flightPoints = flightPoints;
		this.crossedMidline = crossedMidline;
		this.crashedPos = crashedPos;
		this.crashedStillTurning = crashedStillTurning;
		this.crashedTime = crashedTime;
	}
}

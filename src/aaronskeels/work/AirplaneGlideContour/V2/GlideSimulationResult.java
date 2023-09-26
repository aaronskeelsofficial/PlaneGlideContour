package aaronskeels.work.AirplaneGlideContour.V2;

public class GlideSimulationResult {
	public boolean crossedMidline;
	public double[] crashedPos;
	public boolean crashedStillTurning;
	public double crashedTime;
	
	public GlideSimulationResult(boolean crossedMidline, double[] crashedPos, boolean crashedStillTurning, double crashedTime) {
		this.crossedMidline = crossedMidline;
		this.crashedPos = crashedPos;
		this.crashedStillTurning = crashedStillTurning;
		this.crashedTime = crashedTime;
	}
}

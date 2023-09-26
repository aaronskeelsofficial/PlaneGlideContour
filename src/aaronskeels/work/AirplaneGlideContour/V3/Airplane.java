package aaronskeels.work.AirplaneGlideContour.V3;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import aaronskeels.work.AirplaneGlideContour.V2.XYUnitVector;

public class Airplane {
	public double forwardVeloVsDropVeloRatio; // Aka glide ratio, but I don't feel that definition is comprehensive enough towards what is happening
	public double assumedGlideSpeedMs; //[m/s] Assumed forward velocity throughout glide
	public double assumedMaxBankAngleDeg; //[deg] Assumed max bank angle. If so sharp circles are performed, simulations may use smaller angle to define largest radius contour
	private double x, y, height; //[m] GPS coordinates of actual body position
	private XYUnitVector forwardUnitVector; //[x, y]
	private static BufferedImage originalPlanePNG;
	
	public Airplane(double forwardVeloVsDropVeloRatio, double assumedGlideSpeedMs, double assumedMaxBankAngleDeg, double curX, double curY, double curHeight, XYUnitVector forwardUnitVector) {
		this.forwardVeloVsDropVeloRatio = forwardVeloVsDropVeloRatio;
		this.assumedGlideSpeedMs = assumedGlideSpeedMs;
		this.assumedMaxBankAngleDeg = assumedMaxBankAngleDeg;
		this.x = curX;
		this.y = curY;
		this.height = curHeight;
		this.forwardUnitVector = forwardUnitVector;
		//load image
		if (originalPlanePNG == null) {
			try (BufferedInputStream bis = (BufferedInputStream) Airplane.class.getResourceAsStream("/plane.png")) {
				if (bis != null) {
					originalPlanePNG = ImageIO.read(bis);
//					System.out.println(originalPlanePNG.getType() + " : " + BufferedImage.);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void drawPlane(BufferedImage bi, Graphics2D g2d, int radius) {
		g2d.setColor(Color.red);
//		g2d.fillOval((int) (bi.getWidth()/2d - radius), (int) (bi.getHeight()/2d - radius), (int) (2d*radius), (int) (2d*radius));
		g2d.drawImage(getRotatedCopyOfPlane(), (int) (bi.getWidth()/2d - radius), (int) (bi.getHeight()/2d - radius), (int) (2d*radius), (int) (2d*radius), null);
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
	
	public BufferedImage getRotatedCopyOfPlane() {
		double theta = Math.PI*2d - (Math.atan2(forwardUnitVector.y, forwardUnitVector.x) - Math.PI/2d);
		
		// Calculate the new image dimensions to accommodate the rotated image
        int newWidth = (int) Math.ceil(Math.abs(originalPlanePNG.getWidth() * Math.cos(theta)) +
                Math.abs(originalPlanePNG.getHeight() * Math.sin(theta)));
        int newHeight = (int) Math.ceil(Math.abs(originalPlanePNG.getWidth() * Math.sin(theta)) +
                Math.abs(originalPlanePNG.getHeight() * Math.cos(theta)));

        // Create a new BufferedImage to hold the rotated image
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Create an AffineTransform and apply the rotation
        AffineTransform transform = new AffineTransform();
        transform.rotate(theta, newWidth / 2.0, newHeight / 2.0);

        // Apply the transformation to the original image
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        op.filter(originalPlanePNG, rotatedImage);
        
        return rotatedImage;
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

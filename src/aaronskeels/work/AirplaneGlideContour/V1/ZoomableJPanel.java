package aaronskeels.work.AirplaneGlideContour.V1;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ZoomableJPanel extends JPanel{
	private double scale = 1.0; // Initial scale factor
	private BufferedImage targetImage = null;
	
	public ZoomableJPanel(BufferedImage targetImage) {
		this.targetImage = targetImage;
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(scale, scale);
        g2d.drawImage(targetImage, 0, 0, null);
    }
	
	public BufferedImage getImage() {
		return targetImage;
	}
	
	@Override
    public Dimension getPreferredSize() {
		return new Dimension((int) (targetImage.getWidth() * scale), (int) (targetImage.getHeight() * scale));
    }
	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
        this.scale = scale;
        revalidate();
        repaint();
    }
}

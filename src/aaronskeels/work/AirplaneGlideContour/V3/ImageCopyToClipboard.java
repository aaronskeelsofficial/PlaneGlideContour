package aaronskeels.work.AirplaneGlideContour.V3;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/*
 * Code from https://alvinalexander.com/java/java-copy-image-to-clipboard-example/
 * They credit http://www.exampledepot.com/egs/java.awt.datatransfer/ToClipImg.html
 */

public class ImageCopyToClipboard {

	public static void setClipboard(BufferedImage image)
	{
	   ImageSelection imgSel = new ImageSelection(image);
	   Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
	}


	private static class ImageSelection implements Transferable
	{
		private Image image;

		public ImageSelection(Image image)
		{
			this.image = image;
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}
		
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return DataFlavor.imageFlavor.equals(flavor);
		}
		
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException
		{
			if (!DataFlavor.imageFlavor.equals(flavor))
			{
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}
	
}

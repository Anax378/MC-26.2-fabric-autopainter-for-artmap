package anax.autopainter.client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class PrintableImageLoadingException extends Exception {
	public String reason;
	public PrintableImageLoadingException(String reason){
		this.reason = reason;
	}
}

class PrintableImage {

	DyeColor[][] colorMatrix;

	public PrintableImage(File imageFile) throws IOException, PrintableImageLoadingException {
		BufferedImage image = ImageIO.read(imageFile);
		if(image == null){
			throw new PrintableImageLoadingException("invalid format");
		}
		if(image.getWidth() != 128 || image.getHeight() != 128){
			throw new PrintableImageLoadingException("invalid image dimensions (image must be 128x128)");
		}
		colorMatrix = new DyeColor[128][128];
		for(int x = 0; x < 128; x++){
			for(int y = 0; y < 128; y++){
				colorMatrix[x][y] = ColorManager.closestColor(new Color(image.getRGB(x, y), true));
			}
		}
	}

}



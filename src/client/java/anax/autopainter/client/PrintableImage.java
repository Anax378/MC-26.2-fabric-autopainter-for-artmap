package anax.autopainter.client;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import anax.autopainter.client.SquareCoverSolver.Square;

class PrintableImageLoadingException extends Exception {
	public String reason;
	public PrintableImageLoadingException(String reason){
		this.reason = reason;
	}
}

class PrintableImage {

	DyeColor[][] colorMatrix;

	public PrintableImage(DyeColor[][] colorMatrix){
		this.colorMatrix = colorMatrix;
	}

	public static PrintableImage fromColors(byte[] colors){
		DyeColor[] possible = DyeColor.getPossibleColors();
		DyeColor[][] mat = new DyeColor[128][128];
		for(int x = 0; x < 128; x++){
			for(int y = 0; y < 128; y++){

				int color = Byte.toUnsignedInt(colors[x + y*128]);

				mat[x][y] = possible[color];
			}
		}
		return new PrintableImage(mat);
	}

	public void applyAll(ArrayList<Square>[][] squares){
		for(BasicColor color : BasicColor.colors){
			for(ArrayList<Square> sizes : squares[color.index]){
				for(Square square : sizes){
					apply(square, color);
				}
			}
		}
	}

	public void apply(Square square, BasicColor color){
		if(square.done){
			return;
		}
		DyeColor newColor = DyeColor.unshaded(color);
		for(int dx = 0; dx < square.size; dx++){
			for(int dy = 0; dy < square.size; dy++){
				int x = square.x + dx;
				int y = square.y + dy;
				this.colorMatrix[x][y] = newColor;
			}
		}
	}

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



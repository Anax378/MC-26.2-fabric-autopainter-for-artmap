package anax.autopainter.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class MagicConstants{

	public static final float[] yawBounds = new float[]{-37.607f, -37.180714f, -36.747704f, -36.30805f, -35.861828f, -35.409115f, -34.94999f, -34.484528f, -34.01281f, -33.534912f, -33.05091f, -32.560883f, -32.064907f, -31.563063f, -31.055426f, -30.54207f, -30.023079f, -29.498528f, -28.96849f, -28.43305f, -27.89228f, -27.34626f, -26.795065f, -26.238775f, -25.677467f, -25.11122f, -24.540106f, -23.964207f, -23.3836f, -22.798363f, -22.20857f, -21.614302f, -21.015635f, -20.412647f, -19.805414f, -19.194016f, -18.57853f, -17.95903f, -17.335598f, -16.70831f, -16.07724f, -15.442471f, -14.804076f, -14.162135f, -13.516726f, -12.867924f, -12.215808f, -11.560455f, -10.901942f, -10.240349f, -9.57575f, -8.908224f, -8.237849f, -7.564702f, -6.88886f, -6.210401f, -5.5294027f, -4.8459415f, -4.1600957f, -3.471943f, -2.78156f, -2.0890248f, -1.3944144f, -0.6978068f, 0.0f, 0.6978068f, 1.3944144f, 2.0890248f, 2.78156f, 3.471943f, 4.1600957f, 4.8459415f, 5.5294027f, 6.210401f, 6.88886f, 7.564702f, 8.237849f, 8.908224f, 9.57575f, 10.240349f, 10.901942f, 11.560455f, 12.215808f, 12.867924f, 13.516726f, 14.162135f, 14.804076f, 15.442471f, 16.07724f, 16.70831f, 17.335598f, 17.95903f, 18.57853f, 19.194016f, 19.805414f, 20.412647f, 21.015635f, 21.614302f, 22.20857f, 22.798363f, 23.3836f, 23.964207f, 24.540106f, 25.11122f, 25.677467f, 26.238775f, 26.795065f, 27.34626f, 27.89228f, 28.43305f, 28.96849f, 29.498528f, 30.023079f, 30.54207f, 31.055426f, 31.563063f, 32.064907f, 32.560883f, 33.05091f, 33.534912f, 34.01281f, 34.484528f, 34.94999f, 35.409115f, 35.861828f, 36.30805f, 36.747704f, 37.180714f, 37.607f};

	private static float[][] loadTable() {
		float[][] table = new float[128][129];
		
		String resourcePath = "/assets/autopainter/pitchBounds.bin"; 

		try (InputStream is = MagicConstants.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IOException("Resource not found: " + resourcePath);
			}

			// Wrap in BufferedInputStream and DataInputStream just like before
			try (DataInputStream dis = new DataInputStream(new BufferedInputStream(is))) {
				for (int i = 0; i < 128; i++) {
					for (int j = 0; j < 129; j++) {
						table[i][j] = dis.readFloat();
					}
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException("Failed to load lookup table from mod resources!", e);
		}

		return table;
	}

	private static float[][] pitchBounds = null;

	public static float[][] getPitchBounds(){
		if(pitchBounds == null){
			pitchBounds = loadTable();
		}
		return pitchBounds;
	}

}


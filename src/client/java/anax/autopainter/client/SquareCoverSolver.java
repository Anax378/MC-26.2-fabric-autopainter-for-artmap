
package anax.autopainter.client;

import java.util.ArrayList;
import java.util.List;

public class SquareCoverSolver{

	public static class Square {
		public int x, y, size, originX, originY;
		public boolean isDoubleClick = false;
		public boolean done = false;

		public Square(int x, int y, int size) {
			this.x = x; this.y = y; this.size = size;
			originX = this.x + ((size - 1) / 2);
			originY = this.y + ((size - 1) / 2);
		}
	}

	/**
	 * Finds a highly optimized, near-minimum set of squares to cover all 1s.
	 * @param bitmap 128x128 boolean array where true represents a '1'
	 * @return List of squares used to cover the 1s
	 */

	public static List<Square> solve(boolean[][] bitmap) {
		int rows = bitmap.length;
		int cols = bitmap[0].length;
		int maxSize = 5;
		
		// valid[size][r][c] is true if a square of 'size' at (r, c) contains no zeros
		boolean[][][] valid = new boolean[maxSize + 1][rows][cols];
		
		// Base case: Size 1 squares are just the cells themselves
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				valid[1][r][c] = bitmap[r][c];
			}
		}
		
		// DP to find valid larger squares in O(1) time per cell
		for (int s = 2; s <= maxSize; s++) {
			for (int r = 0; r <= rows - s; r++) {
				for (int c = 0; c <= cols - s; c++) {
					// A size S square is valid if its four overlapping size S-1 sub-squares are valid
					valid[s][r][c] = valid[s-1][r][c] && 
									 valid[s-1][r+1][c] && 
									 valid[s-1][r][c+1] && 
									 valid[s-1][r+1][c+1];
				}
			}
		}

		boolean[][] covered = new boolean[rows][cols];
		List<Square> result = new ArrayList<>(512); // Pre-allocate to reduce resizing

		// Threshold Greedy: from largest squares down to 1x1
		for (int s = maxSize; s >= 1; s--) {
			// Max possible uncovered cells this square can cover is s*s
			for (int threshold = s * s; threshold > 0; threshold--) {
				boolean placedAny = true;
				
				// Keep sweeping for this threshold until no more squares meet it
				while (placedAny) {
					placedAny = false;
					for (int r = 0; r <= rows - s; r++) {
						for (int c = 0; c <= cols - s; c++) {
							
							// If square has no zeros, count how many uncovered 1s it hits
							if (valid[s][r][c]) {
								int uncoveredCount = 0;
								for (int i = 0; i < s; i++) {
									for (int j = 0; j < s; j++) {
										if (!covered[r + i][c + j]) {
											uncoveredCount++;
										}
									}
								}

								// If it meets our current threshold, place it
								if (uncoveredCount >= threshold) {
									for (int i = 0; i < s; i++) {
										for (int j = 0; j < s; j++) {
											covered[r + i][c + j] = true;
										}
									}
									result.add(new Square(r, c, s));
									placedAny = true;
								}
							}
							
						}
					}
				}
			}
		}

		return result;
	}

	public static List<Square> solveDisjoint(boolean[][] bitmap) {
			int rows = bitmap.length;
			int cols = bitmap[0].length;
			int maxSize = 5;
			
			// valid[size][r][c] is true if a square of 'size' at (r, c) contains no zeros
			boolean[][][] valid = new boolean[maxSize + 1][rows][cols];
			
			// 1. Base case: Size 1 squares
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					valid[1][r][c] = bitmap[r][c];
				}
			}
			
			// 2. DP to find valid larger squares in O(1) time per cell
			for (int s = 2; s <= maxSize; s++) {
				for (int r = 0; r <= rows - s; r++) {
					for (int c = 0; c <= cols - s; c++) {
						valid[s][r][c] = valid[s-1][r][c] && 
										 valid[s-1][r+1][c] && 
										 valid[s-1][r][c+1] && 
										 valid[s-1][r+1][c+1];
					}
				}
			}

			boolean[][] covered = new boolean[rows][cols];
			List<Square> result = new ArrayList<>(512);

			// 3. Greedy Disjoint Placement: Largest to smallest
			for (int s = maxSize; s >= 1; s--) {
				for (int r = 0; r <= rows - s; r++) {
					for (int c = 0; c <= cols - s; c++) {
						
						// If the square contains no zeros...
						if (valid[s][r][c]) {
							
							// ...check if it intersects with any ALREADY PLACED squares
							boolean overlaps = false;
							for (int i = 0; i < s; i++) {
								for (int j = 0; j < s; j++) {
									if (covered[r + i][c + j]) {
										overlaps = true;
										break;
									}
								}
								if (overlaps) break;
							}

							// If it's completely clear, place it!
							if (!overlaps) {
								for (int i = 0; i < s; i++) {
									for (int j = 0; j < s; j++) {
										covered[r + i][c + j] = true;
									}
								}
								result.add(new Square(r, c, s));
								
								// Optimization: Skip columns we just covered
								c += (s - 1); 
							}
						}
					}
				}
			}

			return result;
		}


		public static boolean isValidCover(boolean[][] bitmap, List<Square> squares) {
				int rows = bitmap.length;
				int cols = bitmap[0].length;
				
				// Track which cells have been covered by the squares
				boolean[][] covered = new boolean[rows][cols];

				// 1. Process squares: check bounds and ensure no '0's are covered
				for (Square sq : squares) {
					// Check if square is out of bounds
					if (sq.x < 0 || sq.y < 0 || sq.x + sq.size > rows || sq.y + sq.size > cols) {
						System.err.printf("Invalid: Square at (%d, %d) size %d is out of bounds.%n", sq.x, sq.y, sq.size);
						return false;
					}

					// Mark cells as covered and check for 0-coverage
					for (int i = 0; i < sq.size; i++) {
						for (int j = 0; j < sq.size; j++) {
							int r = sq.x + i;
							int c = sq.y + j;
							
							// If the underlying bitmap is false (0), the solver made a mistake
							if (!bitmap[r][c]) {
								System.err.printf("Invalid: A '0' was covered at (%d, %d).%n", r, c);
								return false; 
							}
							
							covered[r][c] = true;
						}
					}
				}

				// 2. Verify that every '1' in the bitmap was actually covered
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						// If the bitmap has a 1, but our squares didn't cover it
						if (bitmap[r][c] && !covered[r][c]) {
							System.err.printf("Invalid: A '1' was left uncovered at (%d, %d).%n", r, c);
							return false;
						}
					}
				}

				// If we made it here, the cover is perfectly valid
				return true;
			}

}

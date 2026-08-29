package anax.autopainter.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;

import anax.autopainter.client.SquareCoverSolver.Square;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;

import static anax.autopainter.client.MagicConstants.yawBounds;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

class AutopaintingSession{

	public static final int MAX_BRUSH_SIZE = 5;
	private static final int DARKENING_SQUARES_INDEX = 0;
	private static final int LIGHTENING_SQUARES_INDEX = 1;

	private ArrayList<Square>[][] squares = new ArrayList[BasicColor.colors.length][MAX_BRUSH_SIZE];
	private ArrayList<Square>[][] shadingSquares = new ArrayList[2][MAX_BRUSH_SIZE];

	private DyeColor[][] colorMatrix;
	private PrintableImage image;

	public volatile boolean paused = false;
	public volatile boolean ended = false;
	volatile HashSet<BasicColor> toApply = new HashSet<>();

	private boolean needLightening = false;
	private boolean needDarkening = false;

	public static int delayMillis = 160;

	AutopaintingSession(PrintableImage img){
		DyeColor[][] mat = img.colorMatrix;
		this.image = img;

		this.colorMatrix = mat;

		for(DyeColor[] colors : mat){
			for(DyeColor color : colors){
				if(color.shift < 0){
					needDarkening = true;
				}
				if(color.shift > 0){
					needLightening = true;
				}
				toApply.add(color.base);
			}
		}

		for(ArrayList<Square>[] colorSquares : squares){
			for(int i = 0; i < colorSquares.length; i++){
				colorSquares[i] = new ArrayList<Square>();
			}
		}
		for(ArrayList<Square>[] shadeSquares : shadingSquares){
			for(int i = 0; i < shadeSquares.length; i++){
				shadeSquares[i] = new ArrayList<Square>();
			}
		}

		boolean[][] bitmap = new boolean[mat.length][mat[0].length];
		for(BasicColor color : toApply){
			for(int x = 0; x < mat.length; x++){
				for(int y = 0; y < mat[0].length; y++){
					bitmap[x][y] = colorMatrix[x][y].base == color;
				}
			}
			List<Square> result = SquareCoverSolver.solve(bitmap);
			for(Square square : result){
				squares[color.index][square.size - 1].add(square);
			}
		}
		for(int s = -2; s < 2; s++){
			if(s == 0){
				continue;
			}
			for(int x = 0; x < mat.length; x++){
				for(int y = 0; y < mat[0].length; y++){
					bitmap[x][y] = colorMatrix[x][y].shift == s;
				}
			}
			List<Square> result = s == -1 ? 
				SquareCoverSolver.solveDisjoint(bitmap)
			:	SquareCoverSolver.solve(bitmap);

		for(Square square : result){
				square.isDoubleClick = s == -2;
				int idx = s < 0 ? DARKENING_SQUARES_INDEX : LIGHTENING_SQUARES_INDEX;
				shadingSquares[idx][square.size - 1].add(square);
			}
		}
		checkAgainstMap();
	}

	static boolean isDone(Square square, PrintableImage source, PrintableImage dest, boolean checkShading){

		for(int dx = 0; dx < square.size; dx++){
			for(int dy = 0; dy < square.size; dy++){
				int x = square.x + dx;
				int y = square.y + dy;
				DyeColor scolor = source.colorMatrix[x][y];
				DyeColor dcolor = dest.colorMatrix[x][y];

				if (scolor.base != dcolor.base){
					// sendMessage("debug: " + scolor.base.dye.toString() + " != " + dcolor.base.dye.toString());
					return false;
				}

				if(dcolor.shift != 0 && (dcolor.shift != scolor.shift)){
					// sendMessage("debug: " + dcolor.base.dye.toString() + " with shift " + dcolor.shift + " does not match " + scolor.shift);
					return false;
				}

				if(checkShading && (!scolor.equals(dcolor))){
					return false;
				}
			}
		}
		return true;
	}

	boolean willInterfere(Square square, PrintableImage source, PrintableImage dest, boolean isLightening){
		for(int dx = 0; dx < square.size; dx++){
			for(int dy = 0; dy < square.size; dy++){
				int x = square.x + dx;
				int y = square.y + dy;
				DyeColor scolor = source.colorMatrix[x][y];
				DyeColor dcolor = dest.colorMatrix[x][y];
				if(scolor.base != dcolor.base){
					continue;
				}
				int offset = isLightening ? 1 : -1;
				if(square.isDoubleClick){
					offset *= 2;
				}
				int finalShift = Math.clamp(dcolor.shift + offset, -2, 1);
				if(finalShift != scolor.shift){
					return true;
				}
			}
		}
		return false;
	}

	boolean checkAgainstMap(){
		boolean changed = false;
		ItemFrame frame = findMap();
		if(frame == null){
			sendMessage("cannot find map to check against");
			return false;
		}
		byte[] colors = mapColors(frame);
		if(colors == null){
			sendMessage("could not extract map data to check against");
			return false;
		}
		HashSet<BasicColor> unfinished = new HashSet<>();

		boolean recalculateShading = false;

		PrintableImage mapImage = PrintableImage.fromColors(colors);
		for(ArrayList<Square>[] sqss : squares){
			for(ArrayList<Square> sqs : sqss){
				for(Square sq : sqs){
					boolean done = isDone(sq, image, mapImage, false);
					if(!done && sq.done){
						recalculateShading = true;
					}
					changed |= sq.done != done;
					sq.done = done;
					if(!sq.done){
						unfinished.add(image.colorMatrix[sq.x][sq.y].base);
					}
				}
			}
		}

		mapImage.applyAll(squares);

		boolean unfinishedDarkening = false;
		boolean unfinishedLightening = false;

		for(int idx = 0; idx < 2; idx++){
			ArrayList<Square>[] sqss = shadingSquares[idx];
			for(ArrayList<Square> sqs : sqss){
				for(Square sq : sqs){
					boolean done = isDone(sq, image, mapImage, true);
					changed |= sq.done != done;
					sq.done = done;
					if(!sq.done){
						DyeColor color = image.colorMatrix[sq.x][sq.y];
						unfinishedDarkening |= color.shift < 0;
						unfinishedLightening |= color.shift > 0;
						recalculateShading |= willInterfere(sq, image, mapImage, idx == LIGHTENING_SQUARES_INDEX);
					}
					if(recalculateShading){
						break;
					}
				}
				if(recalculateShading){
					break;
				}
			}
			if(recalculateShading){
				break;
			}
		}
		if(recalculateShading){
			unfinishedLightening = false;
			unfinishedDarkening = false;

			for(ArrayList<Square>[] sqss : shadingSquares){
				for(ArrayList<Square> sqs : sqss){
					sqs.clear();
				}
			}
			boolean[][] bitmap = new boolean[image.colorMatrix.length][image.colorMatrix[0].length];
			for(int s = -2; s < 2; s++){
				if(s == 0){
					continue;
				}
				for(int x = 0; x < bitmap.length; x++){
					for(int y = 0; y < bitmap[0].length; y++){
						if(image.colorMatrix[x][y].shift != s){
							bitmap[x][y] = false;
							continue;
						}


						bitmap[x][y] = !image.colorMatrix[x][y].equals(mapImage.colorMatrix[x][y]);
						if(bitmap[x][y]){
							unfinishedDarkening |= s < 0;
							unfinishedLightening |= s > 0;
						}
					}
				}

				List<Square> result = s == -1 ?
					SquareCoverSolver.solveDisjoint(bitmap)
				:	SquareCoverSolver.solve(bitmap);

				int idx = s < 0 ? DARKENING_SQUARES_INDEX : LIGHTENING_SQUARES_INDEX;
				for(Square sq : result){
					sq.isDoubleClick = s == -2;
					shadingSquares[idx][sq.size - 1].add(sq);
				}
			}
		}
		this.needDarkening = unfinishedDarkening;
		this.needLightening = unfinishedLightening;
		this.toApply = unfinished;
		return changed;
	}

	static @Nullable ItemFrame findMap(){
		Entity vehicle = Minecraft.getInstance().player.getVehicle();
		if(vehicle instanceof ArmorStand){
			ItemFrame frame = null;
			Double closest = Double.POSITIVE_INFINITY;
			for(Entity entity : Minecraft.getInstance().level.entitiesForRendering()){
				if(entity instanceof ItemFrame fe){
					Double distance = fe.position().distanceTo(vehicle.position());
					if(distance < closest){
						closest = distance;
						frame = fe;
					}
				}
			}
			return frame;

		}else{
			sendMessage("could not find easel (are you sitting on one ?)");
			return null;
		}
	}
	
	static @Nullable byte[] mapColors(ItemFrame frame){
		try{
			MapId mapid = frame.getFramedMapId(frame.getItem());
			return MapItem.getSavedData(mapid, frame.level()).colors;

		}catch(NullPointerException e){
			return null;
		}
	}

	private static float yawFor(int x){
		return (yawBounds[x] + yawBounds[x + 1]) / 2f;
	}

	private static float pitchFor(int x, int y){
		if(x == 0 && y == 127){
			return 32.304527f;
		}
		float[] bounds = MagicConstants.getPitchBounds()[x];
		return (bounds[y] + bounds[y + 1]) / 2f;
	}

	static void look(int x, int y){
		float yaw = yawFor(x);
		float pitch = pitchFor(x, y);
		Minecraft client = Minecraft.getInstance();
		if(client.player == null || client.gameMode == null){
			return;
		}
		client.player.setYRot(yaw);
		client.player.setXRot(pitch);
		client.player.connection.send(new ServerboundMovePlayerPacket.Rot(
					client.player.getYRot(),
					client.player.getXRot(),
					client.player.onGround(),
					client.player.horizontalCollision)
				);
	}

	static void lookAndClick(int x, int y, int delayMillis){
		Minecraft client = Minecraft.getInstance();
		if(client.player == null || client.gameMode == null){
			return;
		}
		look(x, y);
		delay(delayMillis);
		client.player.swing(InteractionHand.MAIN_HAND);
		delay(delayMillis);
	}

	static boolean findAndHold(Item item){
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.gameMode == null){
			return false;
		}
		Inventory inv = client.player.getInventory();
		if(inv.getItem(inv.getSelectedSlot()).getItem() == item){
			return true;
		}

		List<Slot> slots = client.player.containerMenu.slots;
		for(Slot slot : slots){
			if(slot.getItem().getItem() == item){
				client.gameMode.handleContainerInput(
						client.player.containerMenu.containerId,
						slot.index,
						inv.getSelectedSlot(),
						ContainerInput.SWAP,
						client.player);

				return true;
			}
		}
		return false;
	}


	public static String formatDuration(long millis) {
		Duration d = Duration.ofMillis(millis);

		long hours = d.toHours();
		long minutes = d.toMinutesPart();
		long seconds = d.toSecondsPart();

		if (hours > 0) {
			return hours + "h " + minutes + "m";
		}
		if (minutes > 0) {
			return minutes + "m " + seconds + "s";
		}
		return seconds + "s";
	}

	static void sendMessage(String text){
		Autopainter.sendMessage(text);
	}

	static void sendCommand(String command){
		Minecraft.getInstance().player.connection.sendCommand(command);
	}

	static void setBrushSize(int size){
		delay(delayMillis);
		sendCommand("art brushsize " + size);
	}

	void reportRemainingDyes(){
		sendMessage("the following dyes are required: ");
		for(BasicColor color : toApply){
			sendMessage(color.dye.toString());
		}
	}

	String estimatedDuration(){
		long millis = 0;
		for(ArrayList<Square>[] sqss : squares){
			for(ArrayList<Square> sqs : sqss){
				for(Square sq : sqs){
					if(!sq.done){
						millis += delayMillis * 2;
					}
				}
			}
		}
		for(ArrayList<Square>[] sqss : shadingSquares){
			for(ArrayList<Square> sqs : sqss){
				for(Square sq : sqs){
					if(!sq.done){
						millis += delayMillis * 2;
						if(sq.isDoubleClick){
							millis += delayMillis * 2;
						}
					}
				}
			}
		}
		return formatDuration(millis);
	}

	BasicColor mostCommonColor(){
		HashMap<BasicColor, Integer> counts = new HashMap<>();
		BasicColor mostCommon = null;
		Integer topCount = 0;
		for(DyeColor[] row : colorMatrix){
			for(DyeColor color : row){
				Integer count = counts.getOrDefault(color.base, 0) + 1;
				counts.put(color.base, count);
				if(count > topCount){
					mostCommon = color.base;
					topCount = count;
				}
			}
		}
		return mostCommon;
	}

	void paint(){
		while(!ended){
			if(checkAgainstMap()){
				sendMessage("new estimated time: " + estimatedDuration());
			}
			HashSet<Item> availableItems = new HashSet<>(
					Minecraft.getInstance()
					.player
					.containerMenu
					.getItems()
					.stream()
					.map(s -> s.getItem())
					.toList()
					);

			HashSet<BasicColor> applyNow = new HashSet<>();

			for(BasicColor color : toApply){
				if(availableItems.contains(color.dye)){
					applyNow.add(color);
				}
			}

			boolean canDarken = availableItems.contains(ColorManager.DARKENING_ITEM);
			boolean canLighten = availableItems.contains(ColorManager.LIGHTENING_ITEM);

			// sendMessage("applying: " + Arrays.toString(applyNow.toArray()));

			if(applyNow.isEmpty()){
				if(!toApply.isEmpty()){
					reportRemainingDyes();
					paused = true;
				}else if(needDarkening || needLightening){
					if(!( (needDarkening && canDarken) || (needLightening && canLighten) )){
						sendMessage("the following items are required: ");
						if(needDarkening){
							sendMessage(ColorManager.DARKENING_ITEM.toString());
						}
						if(needLightening){
							sendMessage(ColorManager.LIGHTENING_ITEM.toString());
						}
						sendMessage("use /autopaint resume after you have obtained the items");
						paused = true;
					}

				}else{
					sendMessage("finished");
					ended = true;
					return;
				}
			}

unpaused:   while(!paused && !ended){
				if(!applyNow.isEmpty()){
					//paint color
					BasicColor color = applyNow.iterator().next();
					for(int size = 0; size < MAX_BRUSH_SIZE && !paused && !ended; size++){
						int sqlen = squares[color.index][size].size();
						if(sqlen > 0) setBrushSize(size + 1);
						for(int idx = 0; idx < sqlen && !paused && !ended; idx++){
							Square square = squares[color.index][size].get(idx);
							if(square.done){
								continue;
							}
							if(!findAndHold(color.dye)){
								break unpaused;
							}
							lookAndClick(square.originX, square.originY, delayMillis);
							square.done = true;
						}
					}
					if(!ended && !paused){
						applyNow.remove(color);
						toApply.remove(color);
					}

					// for(int x = 0; x < colorMatrix.length && !paused && !ended; x++){
					// 	for(int y = 0; y < colorMatrix[x].length && !paused && !ended; y++){
					// 		if(colorMatrix[x][y].base != color){
					// 			continue;
					// 		}
					// 		if(!findAndHold(color.dye)){
					// 			break unpaused;
					// 		}
					// 		lookAndClick(x, y, delayMillis);
					// 	}
					// }

				} else if( toApply.isEmpty() && ((needLightening && canLighten) || (needDarkening && canDarken)) ) {
					boolean lightening = (needLightening && canLighten);
					int idx = lightening ? LIGHTENING_SQUARES_INDEX : DARKENING_SQUARES_INDEX;
					for(int size = 0; size < MAX_BRUSH_SIZE && !paused && !ended; size++){
						int sqlen = shadingSquares[idx][size].size();
						if(sqlen > 0) setBrushSize(size + 1);
						for(int sqidx = 0; sqidx < sqlen && !paused && !ended; sqidx++){
							Square square = shadingSquares[idx][size].get(sqidx);
							if(square.done){
								continue;
							}
							if(!findAndHold(lightening ? ColorManager.LIGHTENING_ITEM : ColorManager.DARKENING_ITEM)){
								break unpaused;
							}
							lookAndClick(square.originX, square.originY, delayMillis);
							if(square.isDoubleClick){
								lookAndClick(square.originX, square.originY, delayMillis);
							}
							square.done = true;
						}
					}

					// for(int x = 0; x < colorMatrix.length && !paused && !ended; x++){
					// 	for(int y = 0; y < colorMatrix[x].length && !paused && !ended; y++){
					// 		if(!doneShading[x][y] && ((lightening && colorMatrix[x][y].shift > 0) || (!lightening && colorMatrix[x][y].shift < 0))){
					// 			if(!findAndHold(lightening ? ColorManager.LIGHTENING_ITEM : ColorManager.DARKENING_ITEM)){
					// 				break unpaused;
					// 			}
					// 			for(int s = 0; s < Math.abs(colorMatrix[x][y].shift); s++){
					// 				lookAndClick(x, y, delayMillis);
					// 			}
					// 			doneShading[x][y] = true;
					// 		}
					// 	}
					// }
					if(lightening){
						needLightening = false;
					}else{
						needDarkening = false;
					}
				} else {
					break unpaused;
				}
			}
			if(paused){
				sendMessage("session paused");
				return;
			}
			// while(paused && !aborted){
			// 	try{Thread.sleep(1000);}catch(InterruptedException e){};
			// }
		}
	}

	private static void delay(int millis){
		try{Thread.sleep(millis);}catch(InterruptedException e){busySleep(millis);}
	}

	private static void busySleep(int millis){
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() < start + millis){}
	}




}



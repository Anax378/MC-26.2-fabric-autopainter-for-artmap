package anax.autopainter.client;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import static anax.autopainter.client.MagicConstants.yawBounds;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

class AutopaintingSession{

	private DyeColor[][] colorMatrix;
	private boolean[][] doneShading;

	public volatile boolean paused = false;
	public volatile boolean ended = false;
	volatile HashSet<BasicColor> toApply = new HashSet<>();

	private boolean needLightening = false;
	private boolean needDarkening = false;

	public static int delayMillis = 160;

	AutopaintingSession(PrintableImage img){
		DyeColor[][] mat = img.colorMatrix;

		this.colorMatrix = mat;
		this.doneShading = new boolean[mat.length][];
		for(int i = 0; i < mat.length; i++){
			doneShading[i] = new boolean[mat[i].length];
		}

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
	}

	private static float yawFor(int x){
		return (yawBounds[x] + yawBounds[x + 1]) / 2f;
	}

	private static float pitchFor(int x, int y){
		float[] bounds = MagicConstants.getPitchBounds()[x];
		return (bounds[y] + bounds[y + 1]) / 2f;
	}

	static void lookAndClick(int x, int y, int delayMillis){
		float yaw = yawFor(x);
		float pitch = pitchFor(x, y);
		Minecraft client = Minecraft.getInstance();
		if(client.player == null || client.gameMode == null){
			return;
		}
		client.player.setYRot(yaw);
		client.player.setXRot(pitch);
		client.player.connection.send(new ServerboundMovePlayerPacket.Rot(client.player.getYRot(), client.player.getXRot(), client.player.onGround(), client.player.horizontalCollision));

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

	void reportRemainingDyes(){
		sendMessage("the following dyes are required: ");
		for(BasicColor color : toApply){
			sendMessage(color.dye.toString());
		}
	}

	void reportEstimatedDuration(){
		long millis = 0;
		for(int x = 0; x < colorMatrix.length; x++){
			for(int y = 0; y < colorMatrix[x].length; y++){
				if(toApply.contains(colorMatrix[x][y].base)){
					millis += delayMillis * 2;
				}
				if(!doneShading[x][y]){
					millis += delayMillis * 2 * Math.abs(colorMatrix[x][y].shift);
				}
			}
		}
		sendMessage("estimated time: " + formatDuration(millis));
	}

	void paint(){
		while(!ended){
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
					for(int x = 0; x < colorMatrix.length && !paused && !ended; x++){
						for(int y = 0; y < colorMatrix[x].length && !paused && !ended; y++){
							if(colorMatrix[x][y].base != color){
								continue;
							}
							if(!findAndHold(color.dye)){
								break unpaused;
							}
							lookAndClick(x, y, delayMillis);
						}
					}
					if(!ended && !paused){
						applyNow.remove(color);
						toApply.remove(color);
					}

				} else if( toApply.isEmpty() && ((needLightening && canLighten) || (needDarkening && canDarken)) ) {
					boolean ligtening = (needLightening && canLighten);
					for(int x = 0; x < colorMatrix.length && !paused && !ended; x++){
						for(int y = 0; y < colorMatrix[x].length && !paused && !ended; y++){
							if(!doneShading[x][y] && ((ligtening && colorMatrix[x][y].shift > 0) || (!ligtening && colorMatrix[x][y].shift < 0))){
								if(!findAndHold(ligtening ? ColorManager.LIGHTENING_ITEM : ColorManager.DARKENING_ITEM)){
									break unpaused;
								}
								for(int s = 0; s < Math.abs(colorMatrix[x][y].shift); s++){
									lookAndClick(x, y, delayMillis);
								}
								doneShading[x][y] = true;
							}
						}
					}
					if(ligtening){
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



package anax.autopainter.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import org.slf4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutopainterClient implements ClientModInitializer {

	private static final Path IMAGE_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("art-images");
	public static final String MOD_ID = "autopainter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		try {
			Files.createDirectories(IMAGE_DIRECTORY);
		}catch(IOException e){
			LOGGER.warn("could not create images directory");
		}

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("autopaint-debug")
					.then(literal("lookat")
						.then(argument("x", IntegerArgumentType.integer(0, 127))
							.then(argument("y",IntegerArgumentType.integer(0, 127)).executes(context -> {
								int x = IntegerArgumentType.getInteger(context, "x");
								int y = IntegerArgumentType.getInteger(context, "y");
								AutopaintingSession.look(x, y);
								return 1;
							}))
							))
					.then(literal("get-rot").executes(context -> {
						Minecraft.getInstance().player.getXRot();
						LocalPlayer player = Minecraft.getInstance().player;
							
						Autopainter.sendMessage("XRot: " + player.getXRot());
						Autopainter.sendMessage("YRot: " + player.getYRot());
						return 1;
					}))
					.then(literal("get-hit-result").executes(context -> {
						HitResult h = Minecraft.getInstance().hitResult;
						if(h instanceof EntityHitResult eh){
							Autopainter.sendMessage("entity: " + eh.getEntity().toString());
							Entity entity = eh.getEntity();
							if(entity instanceof ItemFrame frame){
								MapId mapid = frame.getFramedMapId(frame.getItem());
								byte[] colors = MapItem.getSavedData(mapid, entity.level()).colors;
								Autopainter.sendMessage("colors: " + Arrays.toString(colors));
							}
						}
						return 1;
					}))
					.then(literal("painting-thread-stack-trace").executes(context -> {
						if (Autopainter.getInstance().paintingThread != null){
							StackTraceElement[] trace = Autopainter.getInstance().paintingThread.getStackTrace();
							for(StackTraceElement el : trace){
								Autopainter.sendMessage(el.toString());
							}
						}
						return 1;
					}))
					);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispathcher, registryAccess) -> {
			dispathcher.register(
					literal("autopaint")
					.then(literal("load").then(argument("path", StringArgumentType.greedyString())
							.suggests((context, builder) -> {
								for(String file : getFilesInImageDirectory()){
									builder.suggest(file);
								}
								return builder.buildFuture();
							}).executes(context -> {
								//start command
							
								String fname = StringArgumentType.getString(context, "path");
								Path path = IMAGE_DIRECTORY.resolve(fname);
								if(Files.isRegularFile(path)){
									File file = path.toFile();
									try{
										PrintableImage image = new PrintableImage(file);
										Autopainter.getInstance().load(image);

									}catch(IOException e){
										Autopainter.sendMessage("could not read file: " + e.getMessage());
										return 1;
									}catch(PrintableImageLoadingException e){
										Autopainter.sendMessage("could not load image: " + e.reason);
									}

								}else{
									Autopainter.sendMessage("file does not exist or is not a regular file");
									return 1;
								}

								return 1;
							})

							)

						)
					.then(literal("stop").executes(context -> {
						Autopainter.getInstance().stop();
						return 1;
					}))
					.then(literal("pause").executes(context -> {
						Autopainter.getInstance().pause();
						return 1;
					}))
					.then(literal("resume").executes(context -> {
						Autopainter.getInstance().resume();
						return 1;
					}))
					.then(literal("status").executes(context -> {
						Autopainter.getInstance().reportStatus();
						return 1;
					}))
					.then(literal("start").executes(context -> {
						Autopainter.getInstance().start();
						return 1;
					})));

		});
	}

	public List<String> getFilesInImageDirectory(){
		try{
		return Files.list(IMAGE_DIRECTORY).map(Path::getFileName).map(Path::toString).toList();
		}
		catch(IOException e){
			return Collections.emptyList();
		}
	}
}

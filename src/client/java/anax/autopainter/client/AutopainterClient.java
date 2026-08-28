package anax.autopainter.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;

import org.slf4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;
import net.fabricmc.loader.api.FabricLoader;

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
					}))
					.then(literal("skip").then(argument("item", StringArgumentType.greedyString()).executes(context -> {
						Autopainter.getInstance().skip(StringArgumentType.getString(context, "item"));
						return 1;
					})))
					);

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

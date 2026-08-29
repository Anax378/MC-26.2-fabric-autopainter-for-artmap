package anax.autopainter.client;


import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

class Autopainter {
	private static Autopainter INSTANCE = null;

	private Autopainter(){}

	public static Autopainter getInstance(){
		if(INSTANCE == null){
			INSTANCE = new Autopainter();
		}
		return INSTANCE;
	}

	Thread paintingThread = null;
	AutopaintingSession session = null;

	static void sendMessage(String text){
		Minecraft.getInstance().player.sendSystemMessage(Component.literal("[autopainter] " + text));
	}

	public void stop(){
		if(session != null){
			session.ended = true;
		}
		if(paintingThread != null){
			paintingThread.interrupt();
		}
		session = null;
		paintingThread = null;
		sendMessage("stopped");
	}

	public void pause(){
		if(session == null){
			sendMessage("no session is active");
			return;
		}
		if(session.paused){
			sendMessage("sessoin already paused");
			return;
		}
		session.paused = true;
		paintingThread = null;
	}

	public void resume(){
		if(session == null){
			sendMessage("no session active");
			return;
		}
		if(!session.paused){
			sendMessage("session not paused");
			return;
		}
		session.paused = false;
		paintingThread = new Thread(session::paint);
		sendMessage("resumed");
		paintingThread.start();
	}

	public void reportStatus(){
		if(session == null){
			sendMessage("no session is active");
			return;
		}
		if(paintingThread == null && !session.paused){
			sendMessage("session loaded (waiting for /autopaint start)");
			sendMessage("estimated time: " + session.estimatedDuration());
			return;
		}
		if(session.paused){
			sendMessage("session is paused");
			sendMessage("estimated time: " + session.estimatedDuration());
			return;
		}
		if(session.ended){
			sendMessage("session ended");
			return;
		}
		sendMessage("session is running");
		sendMessage("estimated time: " + session.estimatedDuration());
	}

	public void load(PrintableImage img){
		stop();
		session = new AutopaintingSession(img);
		sendMessage("loaded sucessfully");
		session.reportRemainingDyes();
		sendMessage("estimated time: " + session.estimatedDuration());
		BasicColor mostCommon = session.mostCommonColor();
		if(mostCommon != null){
			sendMessage(mostCommon.dye.toString() + " is the most common color in this image.");
			sendMessage("you start with this color as the background to save time.");
		}
	}

	public void start(){
		if(session == null){
			sendMessage("no session is loaded");
			return;
		}
		if(paintingThread != null){
			sendMessage("session already running");
			return;
		}
		if(session.paused){
			sendMessage("there is a paused session");
			return;
		}
		paintingThread = new Thread(session::paint);
		paintingThread.start();
	}

}

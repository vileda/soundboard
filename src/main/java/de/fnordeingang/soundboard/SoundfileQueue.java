package de.fnordeingang.soundboard;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;

@Singleton
public class SoundfileQueue
{
	@Resource
	private ManagedExecutorService mes;

	@Inject
	private WebsocketSessionManager websocketSessionManager;

	final BlockingQueue<ProcessBuilder> processQueue = new LinkedBlockingQueue<>();
	final BlockingQueue<Process> runningProcesses = new LinkedBlockingQueue<>();
	private volatile boolean isPlaying = false;

	public void add(ProcessBuilder item)
	{
		try {
			processQueue.put(item);
			playQueue();
			final String[] fragments = item.command().get(item.command().size() - 1).split("/");
			final String title = fragments[fragments.length - 1];
			final JsonObject jsonObject = Json.createObjectBuilder()
					.add("event", "enqueue")
					.add("title", title).build();
			websocketSessionManager.broadcast(jsonObject.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void playQueue() {
		if(isPlaying) return;
		isPlaying = true;
		Runnable playsounds = () -> {
			while (true) {
				try {
					ProcessBuilder processBuilder = processQueue.take();
					System.out.println("playing " + StringUtils.join(processBuilder.command(), ' '));
					Process start = processBuilder.start();
					runningProcesses.put(start);
					start.waitFor();
					final String[] fragments = processBuilder.command().get(processBuilder.command().size() - 1).split("/");
					final String title = fragments[fragments.length - 1];
					final JsonObject jsonObject = Json.createObjectBuilder()
							.add("event", "played")
							.add("title", title).build();
					websocketSessionManager.broadcast(jsonObject.toString());
					if(!isPlaying) break;
				} catch (UnsupportedOperationException | InterruptedException | IOException ignored) {

				}
			}
		};

		mes.execute(playsounds);
	}

	public void killall() {
		isPlaying = false;
		processQueue.clear();
		runningProcesses.stream().forEach(Process::destroyForcibly);
		runningProcesses.clear();
		final JsonObject jsonObject = Json.createObjectBuilder()
				.add("event", "kill").build();
		websocketSessionManager.broadcast(jsonObject.toString());
		try
		{
			Runtime.getRuntime().exec("/usr/bin/env mpv " + getSoundfileLocation() + "/scratch.mp3");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}

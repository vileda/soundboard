package de.fnordeingang.soundboard;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;

@ApplicationScoped
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
			websocketSessionManager.broadcast(makeEventJSON("enqueue", title).toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private JsonObject makeEventJSON(String eventName, String title) {
		return Json.createObjectBuilder()
				.add("event", eventName)
				.add("title", title).build();
	}

	private void playQueue() {
		if(isPlaying) return;
		isPlaying = true;
		Runnable playsounds = () -> {
			while (isPlaying) {
				try {
					ProcessBuilder processBuilder = processQueue.take();
					System.out.println("playing " + StringUtils.join(processBuilder.command(), ' '));
					Process start = processBuilder.start();
					runningProcesses.put(start);
					start.waitFor();
					final String[] fragments = processBuilder.command().get(processBuilder.command().size() - 1).split("/");
					final String title = fragments[fragments.length - 1];
					websocketSessionManager.broadcast(makeEventJSON("played", title).toString());
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
		websocketSessionManager.broadcast(makeEventJSON("kill", "all").toString());
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

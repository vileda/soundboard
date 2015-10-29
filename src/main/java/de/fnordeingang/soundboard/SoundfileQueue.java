package de.fnordeingang.soundboard;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;

@Singleton
public class SoundfileQueue
{
	@Resource
	private ManagedExecutorService mes;

	final BlockingQueue<ProcessBuilder> processQueue = new LinkedBlockingQueue<>();
	final BlockingQueue<Process> runningProcesses = new LinkedBlockingQueue<>();
	private volatile boolean isPlaying = false;

	public void add(ProcessBuilder item)
	{
		try {
			processQueue.put(item);
			playQueue();
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

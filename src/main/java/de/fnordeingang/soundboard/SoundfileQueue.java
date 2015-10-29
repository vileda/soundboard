package de.fnordeingang.soundboard;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class SoundfileQueue
{
	List<Process> processes = new CopyOnWriteArrayList<>();
	Stack<ProcessBuilder> processQueue = new Stack<>();
	private boolean isPlaying = false;

	public ProcessBuilder add(ProcessBuilder item)
	{
		return processQueue.push(item);
	}

	@Asynchronous
	public void playQueue() {
		while (!processQueue.isEmpty() && !isPlaying) {
			isPlaying = true;
			final ProcessBuilder processBuilder = processQueue.pop();
			try
			{
				clearDeadProcesses();
				final Process process = processBuilder.start();
				processes.add(process);
				process.waitFor();
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		isPlaying = false;
	}

	public void killall() {
		processes.stream().forEach(Process::destroy);
		clearDeadProcesses();
		try
		{
			Runtime.getRuntime().exec("/usr/bin/env mpv " + getSoundfileLocation() + "/scratch.mp3");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void clearDeadProcesses()
	{
		processes.stream().filter(process -> !process.isAlive()).forEach(processes::remove);
	}

	private String getSoundfileLocation() {
		String soundfileDir = System.getenv("soundfiles");
		soundfileDir = soundfileDir == null ? System.getProperty("soundfiles") : soundfileDir;
		return soundfileDir;
	}
}

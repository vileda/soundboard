package de.fnordeingang.soundboard;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@ApplicationScoped
public class SoundfileController {
	List<Process> processes = new CopyOnWriteArrayList<>();

	public List<Category> getSoundfiles() {
		List<Category> soundfiles = new ArrayList<>();
		String soundfileDir = getSoundfileLocation();
		Category uncategorized = new Category("Uncategorized");
		File dir = new File(soundfileDir);

		walkDirectories(dir, uncategorized, soundfiles);
		return soundfiles;
	}

	private String getSoundfileLocation() {
		String soundfileDir = System.getenv("soundfiles");
		soundfileDir = soundfileDir == null ? System.getProperty("soundfiles") : soundfileDir;
		return soundfileDir;
	}

	private void walkDirectories(File root, Category category, List<Category> soundfiles) {
		File[] files = root.listFiles();
		if(files != null) {
			if(root.isDirectory()) {
				soundfiles.add(category);
				makeTree(category, files, soundfiles);
			}
			else {
				for (File leaf : files) {
					if(leaf.isDirectory()) {
						Category category1 = new Category(leaf.getName());
						soundfiles.add(category1);
						makeTree(category1, leaf.listFiles(), soundfiles);
					}
					else makeSoundfile(category, leaf);
				}
			}
		}
	}

	private void makeTree(Category category, File[] files, List<Category> soundfiles) {
		for (File leaf : files) {
			if(leaf.isDirectory()) {
				Category category1 = new Category(leaf.getName());
				soundfiles.add(category1);
				makeTree(category1, leaf.listFiles(), soundfiles);
			}
			else makeSoundfile(category, leaf);
		}
	}

	private boolean makeSoundfile(Category category, File leaf) {
		return category.getSoundfiles().add(new Soundfile(leaf.getName(), leaf.getAbsolutePath()));
	}

	public void play(String path) {
		String command = "mpv " + path;
		System.out.println(command);
		exec(command);
	}

	public void killall() {
		try {
			processes.stream().forEach(Process::destroy);
			processes.stream().forEach(processes::remove);
			exec("/usr/bin/env mpv " + getSoundfileLocation() + "/scratch.mp3").waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Process exec(String command) {
		try {
			Process _process = Runtime.getRuntime().exec("/usr/bin/env " + command);
			processes.add(_process);
			processes.stream().filter(process -> !process.isAlive()).forEach(processes::remove);
			return _process;
		} catch (IOException e) {
			throw new RuntimeException("exec " + command + "failed");
		}
	}

	public List<Soundfile> search(String term) {
		List<Category> soundfiles = getSoundfiles();
		List<Soundfile> foundSounds = new ArrayList<>();

		for (Category category : soundfiles) {
			foundSounds.addAll(
					category.getSoundfiles().stream()
							.map(soundfile -> new Soundfile(category.getName() + "/" + soundfile.getTitle(), soundfile.getPath()))
							.filter(soundfile -> soundfile.getTitle().contains(term))
							.collect(Collectors.toList()));
		}

		return foundSounds;
	}
}

package de.fnordeingang.soundboard;

import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
			clearDeadProcesses();
			exec("/usr/bin/env mpv " + getSoundfileLocation() + "/scratch.mp3").waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Process exec(String command) {
		try {
			Process _process = Runtime.getRuntime().exec("/usr/bin/env " + command);
			processes.add(_process);
			clearDeadProcesses();
			return _process;
		} catch (IOException e) {
			throw new RuntimeException("exec " + command + "failed");
		}
	}

	private void clearDeadProcesses()
	{
		processes.stream().filter(process -> !process.isAlive()).forEach(processes::remove);
	}

	public List<SortedSoundfile> search(String term) {
		List<Category> soundfiles = getSoundfiles();
		List<SortedSoundfile> sortedSoundfiles = new ArrayList<>();

		for (Category category : soundfiles) {
			sortedSoundfiles.addAll(
					category.getSoundfiles().stream()
							.map(soundfile -> {
								String title = category.getName() + "/" + soundfile.getTitle();
								double sortKey = StringUtils.getFuzzyDistance(title, term, Locale.getDefault());
								return new SortedSoundfile(title, soundfile.getPath(), sortKey);
							})
							.collect(Collectors.toList()));
		}

		sortedSoundfiles.sort((o1, o2) -> {
			if(o1.getSortKey() > o2.getSortKey()) return -1;
			if(o1.getSortKey() == o2.getSortKey()) return 0;
			else return 1;
		});

		return sortedSoundfiles;
	}
}

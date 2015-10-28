package de.fnordeingang.soundboard;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SoundfileController {
	private List<String> soundfileLocations = new ArrayList<>();
	List<Category> soundfiles = new ArrayList<>();

	public List<Category> getSoundfiles() {
		Category uncategorized = new Category("Uncategorized");
		soundfiles.add(uncategorized);
		List<File> dirList = soundfileLocations.stream()
				.map(s -> new File(soundfileLocations.get(0))).collect(Collectors.toList());

		for (File dir : dirList) {
			walkDirectories(dir, uncategorized);
		}
		return soundfiles;
	}

	private void walkDirectories(File root, Category uncategorized) {
		if(root.isDirectory()) {
			File[] files = root.listFiles();
			if(files != null) {
				makeTree(uncategorized, files);
			}
		}
	}

	private void makeTree(Category uncategorized, File[] files) {
		for (File leaf : files) {
			if(leaf.isDirectory()) walkCategory(leaf);
			else makeSoundfile(uncategorized, leaf);
		}
	}

	private boolean makeSoundfile(Category uncategorized, File leaf) {
		return uncategorized.getSoundfiles().add(new Soundfile(leaf.getName(), leaf.getAbsolutePath()));
	}

	private void walkCategory(File leaf) {
		Category category = new Category(leaf.getName());
		soundfiles.add(category);
		walkDirectories(leaf, category);
	}

	@PostConstruct
	public void postConstruct() {
		String soundfiles = System.getenv("soundfiles");
		soundfiles = soundfiles == null ? System.getProperty("soundfiles") : soundfiles;
		soundfileLocations.add(soundfiles);
	}

	public void play(String path) {
		try {
			String command = "/usr/bin/env mpv " + path;
			System.out.println(command);
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

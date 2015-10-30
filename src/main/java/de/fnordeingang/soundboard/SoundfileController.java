package de.fnordeingang.soundboard;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.Stateful;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;

@Stateful
public class SoundfileController {
	@Inject
	private SoundfileQueue soundfileQueue;

	final List<Category> soundfiles = new ArrayList<>();

	public List<Category> getSoundfiles() {
		if(soundfiles.isEmpty()) {
			String soundfileDir = getSoundfileLocation();
			Category uncategorized = new Category("Uncategorized");
			File dir = new File(soundfileDir);

			walkDirectories(dir, uncategorized, soundfiles);

			soundfiles.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			soundfiles.stream()
					.map(Category::getSoundfiles)
					.map(soundfiles2 -> soundfiles2.stream()
							.map(soundfile -> soundfile = new Soundfile(soundfile.getTitle()
									.replace("_", " ").replace("/\\..*$/", ""), soundfile.getPath()))
							.collect(Collectors.toList()))
					.forEach(soundfiles1 -> soundfiles1
							.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle())));
		}

		return soundfiles;
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

	private void makeSoundfile(Category category, File leaf) {
		category.getSoundfiles().add(new Soundfile(leaf.getName(), leaf.getAbsolutePath()));
	}

	public void play(String path) {
		enqueue(path);
	}

	private void enqueue(String command) {
		ProcessBuilder p = new ProcessBuilder("/usr/bin/env", "mpv", command.replace(" ", "\\ "));
		System.out.println("enqueuing" + StringUtils.join(p.command(), ' '));
		soundfileQueue.add(p);
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

	public void killall()
	{
		soundfileQueue.killall();
	}
}

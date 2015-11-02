package de.fnordeingang.soundboard;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;

@ApplicationScoped
public class SoundfileController {
	@Inject
	private SoundfileQueue soundfileQueue;

	private List<Category> soundfiles = new ArrayList<>();
	private List<Soundfile> flatSoundfiles = new ArrayList<>();

	public List<Category> getSoundfiles() {
		if(soundfiles.isEmpty()) {
			System.out.println("caching soundfiles");
			String soundfileDir = getSoundfileLocation();
			Category uncategorized = new Category("Uncategorized");
			File dir = new File(soundfileDir);

			walkDirectories(dir, uncategorized, soundfiles);

			soundfiles.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			soundfiles.stream()
					.forEach(category -> category.getSoundfiles().forEach(soundfile1 -> {
						soundfile1.setTitle(soundfile1.getTitle()
								.replace("_", " ")
								.substring(0, soundfile1.getTitle().lastIndexOf('.')));
					}));
			soundfiles.stream()
					.forEach(category -> {
						category.setName(category.getName().replace("_", " "));
						category.getSoundfiles().sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
					});
			flatSoundfiles.clear();
			makeFlatList(soundfiles);
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
		System.out.println("enqueuing" + StringUtils.join(command, ' '));
		soundfileQueue.add(command);
	}

	public List<SortedSoundfile> search(String term) {
		List<SortedSoundfile> fuzzySearchResults = flatSoundfiles.stream().map(soundfile -> {
			double sortKey = StringUtils.getFuzzyDistance(soundfile.getTitle(), term, Locale.getDefault());
			return new SortedSoundfile(soundfile.getTitle(), soundfile.getPath(), sortKey);
		}).collect(Collectors.toList());

		fuzzySearchResults.sort((o1, o2) -> {
			if (o1.getSortKey() > o2.getSortKey()) return -1;
			if (o1.getSortKey() == o2.getSortKey()) return 0;
			else return 1;
		});

		fuzzySearchResults = fuzzySearchResults.size() > 10 ?
				fuzzySearchResults.subList(0, 10) :
				fuzzySearchResults;

		return fuzzySearchResults;
	}

	private void makeFlatList(List<Category> soundfiles) {
		for (Category category : soundfiles) {
			flatSoundfiles.addAll(
					category.getSoundfiles().stream()
							.map(soundfile -> {
								String title = category.getName() + "/" + soundfile.getTitle();
								return new Soundfile(title, soundfile.getPath());
							})
							.collect(Collectors.toList()));
		}
	}

	public void killall()
	{
		soundfileQueue.killall();
	}

	public boolean isSoundfilePresent(String url) {
		for (Soundfile soundfile : flatSoundfiles) {
			if (soundfile.getPath().equals(url)) return true;
		}
		return false;
	}
}

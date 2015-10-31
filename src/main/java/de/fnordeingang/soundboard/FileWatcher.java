package de.fnordeingang.soundboard;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import static de.fnordeingang.soundboard.Config.getSoundfileLocation;
import static java.nio.file.StandardWatchEventKinds.*;

@Singleton
public class FileWatcher {
	@Inject
	SoundfileController soundfileController;

	private WatchService watcher;

	public FileWatcher() throws IOException {
		FileSystem fileSystem = FileSystems.getDefault();
		Path path = fileSystem.getPath(getSoundfileLocation());
		watcher = fileSystem.newWatchService();
		path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
	}

	@Schedule(persistent = false, hour = "*", minute = "*", second = "*/4")
	public void execute() {
		try {
			WatchKey key = watcher.poll(3, TimeUnit.SECONDS);
			if(key == null) return;
			if (!key.pollEvents().isEmpty()) {
				soundfileController.getSoundfiles().clear();
				soundfileController.getSoundfiles();
				System.out.println("clearing cache");
			}
		} catch (InterruptedException ignored) {
		}
	}
}

package de.fnordeingang.soundboard;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Stateless
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

	@Schedule(hour = "*", minute = "*/1", persistent = false)
	public void execute() {
		try {
			WatchKey key = watcher.take();
			for (WatchEvent<?> event : key.pollEvents()) {
				soundfileController.getSoundfiles().clear();
				System.out.println("clearing cache " + event.kind().name());
			}
		} catch (InterruptedException ignored) {
		}
	}

	public String getSoundfileLocation() {
		String soundfileDir = System.getenv("soundfiles");
		soundfileDir = soundfileDir == null ? System.getProperty("soundfiles") : soundfileDir;
		return soundfileDir;
	}
}

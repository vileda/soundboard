package de.fnordeingang.soundboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Soundfile {
	private String title;
	private String path;
}

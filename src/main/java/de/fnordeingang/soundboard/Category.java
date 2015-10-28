package de.fnordeingang.soundboard;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Category {
	private final String name;
	private final List<Soundfile> soundfiles = new ArrayList<>();
}

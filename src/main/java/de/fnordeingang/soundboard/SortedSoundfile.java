package de.fnordeingang.soundboard;

import lombok.Value;

@Value
public class SortedSoundfile
{
	private final String title;
	private final String path;
	private final double sortKey;
}

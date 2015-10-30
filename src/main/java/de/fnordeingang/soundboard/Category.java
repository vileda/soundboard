package de.fnordeingang.soundboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Category {
	private String name;
	private List<Soundfile> soundfiles = new ArrayList<>();

	public Category(String name)
	{
		this.name = name;
	}
}

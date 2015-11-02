package de.fnordeingang.soundboard;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateTimerRequest implements Serializable {
	private String filePath;
	private String year;
	private String month;
	private String day;
	private String hour;
	private String minute;
	private String second;
}

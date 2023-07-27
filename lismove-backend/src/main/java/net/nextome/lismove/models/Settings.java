package net.nextome.lismove.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Settings {

	@Id
	private String name;
	private String value;

	public Settings(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public Settings() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

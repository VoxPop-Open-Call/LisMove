package net.nextome.lismove.services;

import net.nextome.lismove.models.Settings;
import net.nextome.lismove.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

	@Autowired
	private SettingsRepository settingsRepository;

	public String get(String name, String defaultValue) {
		Settings s = settingsRepository.findById(name).orElse(null);
		return s != null && s.getValue() != null ? s.getValue() : defaultValue;
	}

	public Integer get(String name, Integer defaultValue) {
		Settings s = settingsRepository.findById(name).orElse(null);
		return s != null && s.getValue() != null ? Integer.parseInt(s.getValue()) : defaultValue;
	}

	public Long get(String name, Long defaultValue) {
		Settings s = settingsRepository.findById(name).orElse(null);
		return s != null && s.getValue() != null ? Long.parseLong(s.getValue()) : defaultValue;
	}

	public Double get(String name, Double defaultValue) {
		Settings s = settingsRepository.findById(name).orElse(null);
		return s != null && s.getValue() != null ? Double.parseDouble(s.getValue()) : defaultValue;
	}

	public void set(String name, String value) {
		settingsRepository.save(new Settings(name, value));
	}
}

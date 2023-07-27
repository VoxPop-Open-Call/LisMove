package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.LogWallTemplate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "log_wall")
public class LogWall {
	@Id
	private Long ts;
	private LogWallTemplate template;
	private String params;

	public LogWall() {
	}

	public LogWall(Long ts, LogWallTemplate template, String params) {
		this.ts = ts;
		this.template = template;
		this.params = params;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public LogWallTemplate getTemplate() {
		return template;
	}

	public void setTemplate(LogWallTemplate template) {
		this.template = template;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
}

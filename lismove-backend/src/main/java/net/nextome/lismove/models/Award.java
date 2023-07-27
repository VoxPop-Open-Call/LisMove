package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.AwardType;

import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;

@MappedSuperclass
public class Award extends AuditableEntity {
	private String name;
	private String description;
	private BigDecimal value;
	private AwardType type;
	private String imageUrl;
	private Integer winningsAllowed;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public AwardType getType() {
		return type;
	}

	public void setType(AwardType type) {
		this.type = type;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getWinningsAllowed() {
		return winningsAllowed;
	}

	public void setWinningsAllowed(Integer winningsAllowed) {
		this.winningsAllowed = winningsAllowed;
	}
}

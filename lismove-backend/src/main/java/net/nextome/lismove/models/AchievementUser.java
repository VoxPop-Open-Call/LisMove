package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "achievements_users")
public class AchievementUser extends AuditableEntity {
	@Id
	@SequenceGenerator(name = "achievementusersseq", sequenceName = "achievements_users_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "achievementusersseq")
	private Long id;
	@ManyToOne
	@JoinColumn(name = "achievement")
	private Achievement achievement;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	@Column(columnDefinition = "numeric(7,3)")
	private BigDecimal score;
	private Boolean fullfilled;

	public AchievementUser() {
	}

	public AchievementUser(Achievement achievement, User user) {
		this.achievement = achievement;
		this.user = user;
		score = BigDecimal.ZERO;
		fullfilled = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	public void setAchievement(Achievement achievement) {
		this.achievement = achievement;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getScore() {
		return score;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public Boolean getFullfilled() {
		return fullfilled;
	}

	public void setFullfilled(Boolean fullfilled) {
		this.fullfilled = fullfilled;
	}
}

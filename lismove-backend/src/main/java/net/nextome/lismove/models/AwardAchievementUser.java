package net.nextome.lismove.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "award_achievement_users")
public class AwardAchievementUser {
    @Id
    @SequenceGenerator(name = "awardachievementusersseq", sequenceName = "awardachievementusers_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardachievementusersseq")
    private Long id;
    @ManyToOne
    private AwardAchievement awardAchievement;
    @ManyToOne
    private AchievementUser achievementUser;
    private LocalDateTime timestamp;
    @OneToOne
    private Coupon coupon;

    public AwardAchievementUser(AwardAchievement awardAchievement, AchievementUser achievementUser, LocalDateTime timestamp) {
        this.awardAchievement = awardAchievement;
        this.achievementUser = achievementUser;
        this.timestamp = timestamp;
    }

    public AwardAchievementUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AwardAchievement getAwardAchievement() {
        return awardAchievement;
    }

    public void setAwardAchievement(AwardAchievement achievement) {
        this.awardAchievement = achievement;
    }

    public AchievementUser getAchievementUser() {
        return achievementUser;
    }

    public void setAchievementUser(AchievementUser achievementUser) {
        this.achievementUser = achievementUser;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}

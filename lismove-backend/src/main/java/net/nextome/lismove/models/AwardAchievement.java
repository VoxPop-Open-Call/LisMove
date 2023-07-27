package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "award_achievements")
public class AwardAchievement extends Award {
    @Id
    @SequenceGenerator(name = "awardachievementsseq", sequenceName = "awardachievements_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardachievementsseq")
    private Long id;
    @ManyToOne
    private Achievement achievement;

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
}
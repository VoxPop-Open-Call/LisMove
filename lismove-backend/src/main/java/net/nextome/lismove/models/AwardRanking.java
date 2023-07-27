package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "award_rankings")
public class AwardRanking extends Award {
    @Id
    @SequenceGenerator(name = "awardrankingsseq", sequenceName = "awardrankings_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardrankingsseq")
    private Long id;
    @ManyToOne
    private Ranking ranking;
    @ManyToOne
    private User user;
    private Integer position;
    private String range;
    @OneToOne
    private Coupon coupon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}

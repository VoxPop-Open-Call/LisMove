package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "award_custom_users")
public class AwardCustomUser extends AuditableEntity {
    @Id
    @SequenceGenerator(name = "awardcustomusersseq", sequenceName = "awardcustomusers_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardcustomusersseq")
    private Long id;

    @ManyToOne
    private AwardCustom awardCustom;

    @ManyToOne
    private User user;

    @OneToOne
    private Coupon coupon;

    public AwardCustomUser(AwardCustom awardCustom, User user) {
        this.awardCustom = awardCustom;
        this.user = user;
    }

    public AwardCustomUser() {
    }

    public AwardCustom getAwardCustom() {
        return awardCustom;
    }

    public void setAwardCustom(AwardCustom awardCustom) {
        this.awardCustom = awardCustom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}

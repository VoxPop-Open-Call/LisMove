package net.nextome.lismove.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "award_position_users")
public class AwardPositionUser {
    @Id
    @SequenceGenerator(name = "awardpositionusersseq", sequenceName = "awardpositionusers_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardpositionusersseq")
    private Long id;
    private LocalDateTime timestamp;

    @ManyToOne
    private AwardPosition awardPosition;

    @ManyToOne
    private User user;

    @OneToOne
    private Coupon coupon;

    public AwardPositionUser() {
    }

    public AwardPositionUser(AwardPosition awardPosition, User user, LocalDateTime timestamp) {
        this.awardPosition = awardPosition;
        this.user = user;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AwardPosition getAwardPosition() {
        return awardPosition;
    }

    public void setAwardPosition(AwardPosition awardPosition) {
        this.awardPosition = awardPosition;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

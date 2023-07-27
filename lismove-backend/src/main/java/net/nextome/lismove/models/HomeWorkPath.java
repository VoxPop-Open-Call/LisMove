package net.nextome.lismove.models;

import org.hibernate.jdbc.Work;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "home_work_paths")
public class HomeWorkPath {
    @Id
    @SequenceGenerator(name = "pathsseq", sequenceName = "paths_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "pathsseq")
    private Long id;

    @OneToOne
    private HomeAddress homeAddress;
    @OneToOne
    private Seat seat;
    @Column(columnDefinition = "numeric(10,5)")
    private BigDecimal distance;
    @Column(columnDefinition = "varchar")
    private String polyline;

    @ManyToOne
    @JoinColumn(name = "user_uid", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HomeAddress getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(HomeAddress home) {
        this.homeAddress = home;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

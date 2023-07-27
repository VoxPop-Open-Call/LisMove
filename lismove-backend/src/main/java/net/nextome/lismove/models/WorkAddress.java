package net.nextome.lismove.models;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "work_addresses")
public class WorkAddress {

    @Id
    @SequenceGenerator(name = "workaddressesseq", sequenceName = "workaddresses_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "workaddressesseq")
    private Long id;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime startAssociation;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime endAssociation;

    @ManyToOne
    @JoinColumn(name = "user_uid")
    private User user;

    @OneToOne
    private Seat seat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartAssociation() {
        return startAssociation;
    }

    public void setStartAssociation(LocalDateTime startAssociation) {
        this.startAssociation = startAssociation;
    }

    public LocalDateTime getEndAssociation() {
        return endAssociation;
    }

    public void setEndAssociation(LocalDateTime endAssociation) {
        this.endAssociation = endAssociation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
}

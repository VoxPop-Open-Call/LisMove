package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.AwardCustomIssuer;

import javax.persistence.*;

@Entity
@Table(name = "award_customs")
public class AwardCustom extends Award {
	@Id
	@SequenceGenerator(name = "awardcustomsseq", sequenceName = "awardcustoms_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "awardcustomsseq")
	private Long id;
	private AwardCustomIssuer issuer;

	@ManyToOne
	private Organization organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AwardCustomIssuer getIssuer() {
		return issuer;
	}

	public void setIssuer(AwardCustomIssuer type) {
		this.issuer = type;
	}

	public Organization getOrganization() {
		return organization;
	}

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}

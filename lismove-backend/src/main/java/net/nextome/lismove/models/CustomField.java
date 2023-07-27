package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.RankingFilter;

import javax.persistence.*;

@Entity
@Table(name = "custom_fields",
		uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "type"}))
public class CustomField {

	@Id
	@SequenceGenerator(name = "custfieldseq", sequenceName = "cust_field_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custfieldseq")
	private Long id;
	private String name;
	private String description;
	private RankingFilter type;
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public RankingFilter getType() {
		return type;
	}

	public void setType(RankingFilter type) {
		this.type = type;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}

package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "custom_field_values")
public class CustomFieldValue {

	@Id
	@SequenceGenerator(name = "custfieldvalseq", sequenceName = "custfield_val_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custfieldvalseq")
	private Long id;
	private Boolean value;
	@ManyToOne
	@JoinColumn(name = "enrollment")
	private Enrollment enrollment;
	@ManyToOne
	@JoinColumn(name = "custom_field")
	private CustomField customField;

	public CustomFieldValue() {
	}

	public CustomFieldValue(Enrollment enrollment, CustomField customField) {
		this.enrollment = enrollment;
		this.customField = customField;
		value = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public Enrollment getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Enrollment enrollment) {
		this.enrollment = enrollment;
	}

	public CustomField getCustomField() {
		return customField;
	}

	public void setCustomField(CustomField customField) {
		this.customField = customField;
	}
}

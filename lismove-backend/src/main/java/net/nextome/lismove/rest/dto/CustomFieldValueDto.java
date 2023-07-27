package net.nextome.lismove.rest.dto;

public class CustomFieldValueDto {
    private Long id;
    private Long customField;
    private Long enrollment;
    private Boolean value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomField() {
        return customField;
    }

    public void setCustomField(Long customField) {
        this.customField = customField;
    }

    public Long getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Long enrollment) {
        this.enrollment = enrollment;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}

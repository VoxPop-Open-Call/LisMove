package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationSettingValueDto {
    private Long id;
    private String value;
    private String defaultValue;
    private String organizationSetting;
    private Long organization;

    public OrganizationSettingValueDto(Long id, String value, String defaultValue, String organizationSetting, Long organization) {
        this.id = id;
        this.value = value;
        this.defaultValue = defaultValue;
        this.organizationSetting = organizationSetting;
        this.organization = organization;
    }

    public OrganizationSettingValueDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOrganizationSetting() {
        return organizationSetting;
    }

    public void setOrganizationSetting(String organizationSetting) {
        this.organizationSetting = organizationSetting;
    }

    public Long getOrganization() {
        return organization;
    }

    public void setOrganization(Long organization) {
        this.organization = organization;
    }
}

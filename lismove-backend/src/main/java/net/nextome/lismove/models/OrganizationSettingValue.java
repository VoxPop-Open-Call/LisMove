package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "organization_setting_values")
public class OrganizationSettingValue {

    @Id
    @SequenceGenerator(name = "orgsettvalsseq", sequenceName = "orgsettval_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "orgsettvalsseq")
    private Long id;
    private String value;
    @ManyToOne
    private OrganizationSetting organizationSetting;
    @ManyToOne
    private Organization organization;

    public OrganizationSettingValue() {
    }

    public OrganizationSettingValue(String value, OrganizationSetting organizationSetting, Organization organization) {
        this.value = value;
        this.organizationSetting = organizationSetting;
        this.organization = organization;
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

    public OrganizationSetting getOrganizationSetting() {
        return organizationSetting;
    }

    public void setOrganizationSetting(OrganizationSetting organizationSetting) {
        this.organizationSetting = organizationSetting;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}

package net.nextome.lismove.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "organization_settings")
public class OrganizationSetting {

    @Id
    private String name;
    private String defaultValue;

    public OrganizationSetting() {
    }

    public OrganizationSetting(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String id) {
        this.name = id;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}

package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.OrganizationSetting;
import net.nextome.lismove.models.OrganizationSettingValue;
import net.nextome.lismove.repositories.OrganizationSettingRepository;
import net.nextome.lismove.repositories.OrganizationSettingValueRepository;
import net.nextome.lismove.rest.dto.OrganizationSettingValueDto;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationSettingsService extends UtilitiesService {

    @Autowired
    private OrganizationSettingValueRepository settingsRepository;
    @Autowired
    private OrganizationSettingRepository defaultSettingsRepository;
    @Autowired
    private OrganizationSettingRepository organizationSettingRepository;
    @Autowired
    private OrganizationSettingValueRepository organizationSettingValueRepository;

    public <T> T getDefault(String name, Class<T> varType) {
        return get(null, name, varType);
    }

    public <T> T get(Organization organization, String name, Class<T> varType) {
        String defaultValue = defaultSettingsRepository.findById(name).orElseThrow(() -> new LismoveException("Default value of organization setting \""+ name +"\" not found", HttpStatus.NOT_FOUND)).getDefaultValue();
            switch (varType.getCanonicalName()) {
                case "java.lang.String":
                    return varType.cast(get(organization, name, defaultValue));
                case "java.lang.Integer":
                    return varType.cast(get(organization, name, defaultValue != null ? Integer.parseInt(defaultValue) : null));
                case "java.lang.Long":
                    return varType.cast(get(organization, name, defaultValue != null ? Long.parseLong(defaultValue) : null));
                case "java.lang.Double":
                    return varType.cast(get(organization, name, defaultValue != null ? Double.parseDouble(defaultValue) : null));
                case "java.lang.Boolean":
                    return varType.cast(get(organization, name, defaultValue != null ? Boolean.parseBoolean(defaultValue) : null));
                case "java.time.LocalDate":
                    return varType.cast(get(organization, name, defaultValue != null ? LocalDate.parse(defaultValue) : null));
                case "java.time.LocalTime":
                    return varType.cast(get(organization, name, defaultValue != null ? LocalTime.parse(defaultValue) : null));
                default:
                    return null;
            }
    }

    private String get(Organization organization, String name, String defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return s.getValue();
        } else {
            return defaultValue;
        }
    }

    private Integer get(Organization organization, String name, Integer defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return Integer.parseInt(s.getValue());
        } else {
            return defaultValue;
        }
    }

    private Long get(Organization organization, String name, Long defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return Long.parseLong(s.getValue());
        } else {
            return defaultValue;
        }
    }

    private Double get(Organization organization, String name, Double defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return Double.parseDouble(s.getValue());
        } else {
            return defaultValue;
        }
    }

    private Boolean get(Organization organization, String name, Boolean defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return Boolean.parseBoolean(s.getValue());
        } else {
            return defaultValue;
        }
    }

    private LocalDate get(Organization organization, String name, LocalDate defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return LocalDate.parse(s.getValue());
        } else {
            return defaultValue;
        }
    }

    private LocalTime get(Organization organization, String name, LocalTime defaultValue) {
        if (organization == null) {
            return defaultValue;
        }
        OrganizationSettingValue s = settingsRepository.findByOrganizationAndOrganizationSettingName(organization, name).orElse(null);
        if (s != null && s.getValue() != null) {
            return LocalTime.parse(s.getValue());
        } else {
            return defaultValue;
        }
    }

    public OrganizationSetting setDefault(String settingName, String value) {
        OrganizationSetting newSetting = new OrganizationSetting(value, settingName);
        OrganizationSetting oldSetting = organizationSettingRepository.findById(settingName).orElse(new OrganizationSetting());
        notNullBeanCopy(newSetting, oldSetting);
        return save(oldSetting);
    }

    public OrganizationSettingValue set(String settingName, String value, Organization org) {
        OrganizationSetting setting = findSettingById(settingName).orElse(null);
        Assert.notNull(setting, "Setting not found");
        return set(setting, value, org);
    }

    public OrganizationSettingValue set(OrganizationSetting setting, String value, Organization org) {
        OrganizationSettingValue newValue = new OrganizationSettingValue(value, setting, org);
        OrganizationSettingValue oldValue = organizationSettingValueRepository.findByOrganizationIdAndOrganizationSettingName(org.getId(), setting.getName()).orElse(new OrganizationSettingValue());
        notNullBeanCopy(newValue, oldValue);
        return save(oldValue);
    }

    public OrganizationSetting save(OrganizationSetting setting) {
        return organizationSettingRepository.save(setting);
    }

    public OrganizationSettingValue save(OrganizationSettingValue value) {
        return organizationSettingValueRepository.save(value);
    }

    public List<OrganizationSettingValueDto> findSettingsByOrganization(Organization organization) {
        return organizationSettingValueRepository.findByOrganization(organization);
    }

    public Optional<OrganizationSetting> findSettingById(String name) {
        return organizationSettingRepository.findById(name);
    }

    public Optional<OrganizationSettingValue> findByOrganizationAndOrganizationSetting(Long oid, String organizationSetting) {
        return organizationSettingValueRepository.findByOrganizationIdAndOrganizationSettingName(oid, organizationSetting);
    }
}

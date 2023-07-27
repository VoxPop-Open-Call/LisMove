package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationOverviewDto {

    private Long id;
    private Integer type;
    private String title;
    private String notificationLogo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotificationLogo() {
        return notificationLogo;
    }

    public void setNotificationLogo(String notificationLogo) {
        this.notificationLogo = notificationLogo;
    }
}
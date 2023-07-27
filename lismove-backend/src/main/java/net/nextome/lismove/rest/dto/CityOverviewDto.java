package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CityOverviewDto {

    private Long istatId;
    private String city;

    public Long getIstatId() {
        return istatId;
    }

    public void setIstatId(Long istatId) {
        this.istatId = istatId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

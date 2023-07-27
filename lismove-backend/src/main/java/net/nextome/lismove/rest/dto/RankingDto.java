package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RankingDto {
    private Long id;
    private String title;
    private Integer value;
    private Integer filter;
    private String filterValue;
    private Long startDate;
    private Long endDate;
    private Long organization;
    private Integer repeatType = 0;
    private Integer repeatNum = 0;

    private List<RankingPositionDto> rankingPositions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getFilter() {
        return filter;
    }

    public void setFilter(Integer filter) {
        this.filter = filter;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public List<RankingPositionDto> getRankingPositions() {
        return rankingPositions;
    }

    public void setRankingPositions(List<RankingPositionDto> rankingPositions) {
        this.rankingPositions = rankingPositions;
    }

    public Long getOrganization() {
        return organization;
    }

    public void setOrganization(Long organization) {
        this.organization = organization;
    }

    public Integer getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(Integer repeatType) {
        this.repeatType = repeatType;
    }

    public Integer getRepeatNum() {
        return repeatNum;
    }

    public void setRepeatNum(Integer repeatNum) {
        this.repeatNum = repeatNum;
    }
}

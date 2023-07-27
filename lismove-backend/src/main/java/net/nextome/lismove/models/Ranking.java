package net.nextome.lismove.models;

import io.swagger.annotations.ApiParam;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.RankingRepeat;
import net.nextome.lismove.models.enums.RankingValue;
import net.nextome.lismove.rest.dto.RankingPositionDto;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rankings")
public class Ranking extends AuditableEntity {

	@Id
	@SequenceGenerator(name = "rankingseq", sequenceName = "ranking_seq_id", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "rankingseq")
	private Long id;
	private String title;
	private RankingValue value;  //valore di riferimento
	private RankingFilter filter;
	private String filterValue;
	private LocalDate startDate;
	private LocalDate endDate;
	private RankingRepeat repeatType;
	private Integer repeatNum;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime awardsAssigned;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime closed;

	@ManyToOne
	private Organization organization;

	@Transient
	@ApiParam(readOnly = true)
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

	public RankingValue getValue() {
		return value;
	}

	public void setValue(RankingValue value) {
		this.value = value;
	}

	public RankingFilter getFilter() {
		return filter;
	}

	public void setFilter(RankingFilter filter) {
		this.filter = filter;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<RankingPositionDto> getRankingPositions() {
		return rankingPositions;
	}

	public void setRankingPositions(List<RankingPositionDto> rankingPositions) {
		this.rankingPositions = rankingPositions;
	}

	public RankingRepeat getRepeatType() {
		return repeatType;
	}

	public void setRepeatType(RankingRepeat repeatType) {
		this.repeatType = repeatType;
	}

	public Integer getRepeatNum() {
		return repeatNum;
	}

	public void setRepeatNum(Integer repeatNum) {
		this.repeatNum = repeatNum;
	}

	public LocalDateTime getAwardsAssigned() {
		return awardsAssigned;
	}

	public void setAwardsAssigned(LocalDateTime prizeAssigned) {
		this.awardsAssigned = prizeAssigned;
	}

	public LocalDateTime getClosed() {
		return closed;
	}

	public void setClosed(LocalDateTime closed) {
		this.closed = closed;
	}
}

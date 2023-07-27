package net.nextome.lismove.models;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cities")
//@Subselect("SELECT * FROM cities")
public class City {

	@Id
	private Long istatId;
	private String city;
	private String province;
	private String region;
	private String cap;
	private Long geonameId;

	@JsonRawValue
	private String geojson;

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

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCap() {
		return cap;
	}

	public void setCap(String cap) {
		this.cap = cap;
	}

	public Long getGeonameId() {
		return geonameId;
	}

	public void setGeonameId(Long geonameId) {
		this.geonameId = geonameId;
	}

	public String getGeojson() {
		return geojson;
	}

	public void setGeojson(String geojson) {
		this.geojson = geojson;
	}
}

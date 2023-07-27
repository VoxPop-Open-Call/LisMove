package net.nextome.lismove.integrations.revolut.models;

public class RevolutAddress {
	private String street_line1;
	private String street_line2;
	private String region;
	private String postcode;
	private String city;
	private String country;

	public String getStreet_line1() {
		return street_line1;
	}

	public void setStreet_line1(String street_line1) {
		this.street_line1 = street_line1;
	}

	public String getStreet_line2() {
		return street_line2;
	}

	public void setStreet_line2(String street_line2) {
		this.street_line2 = street_line2;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}

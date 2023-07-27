package net.nextome.lismove.rest.dto;

import net.nextome.lismove.models.CarBrand;

import javax.persistence.ManyToOne;

public class CarModelDto {
	private Long id;
	private String name;
	private Long brand;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getBrand() {
		return brand;
	}

	public void setBrand(Long brand) {
		this.brand = brand;
	}
}

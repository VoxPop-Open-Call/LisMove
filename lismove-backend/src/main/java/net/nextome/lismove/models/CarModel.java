package net.nextome.lismove.models;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_models")
public class CarModel {

	@Id
	private Long id;
	private String name;
	@ManyToOne
	private CarBrand brand;

	public CarModel() {
	}

	public CarModel(Long id, String name, CarBrand brand) {
		this.id = id;
		this.name = name;
		this.brand = brand;
	}

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

	public CarBrand getBrand() {
		return brand;
	}

	public void setBrand(CarBrand brand) {
		this.brand = brand;
	}
}

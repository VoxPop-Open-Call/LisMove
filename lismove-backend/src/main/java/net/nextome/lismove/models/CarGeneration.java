package net.nextome.lismove.models;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_generations")
public class CarGeneration {

	@Id
	private Long id;
	private String name;
	private Integer modelYear;
	@ManyToOne
	private CarModel model;

	public CarGeneration() {
	}

	public CarGeneration(Long id, String name, Integer modelYear, CarModel model) {
		this.id = id;
		this.name = name;
		this.modelYear = modelYear;
		this.model = model;
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

	public Integer getModelYear() {
		return modelYear;
	}

	public void setModelYear(Integer modelYear) {
		this.modelYear = modelYear;
	}

	public CarModel getModel() {
		return model;
	}

	public void setModel(CarModel model) {
		this.model = model;
	}
}

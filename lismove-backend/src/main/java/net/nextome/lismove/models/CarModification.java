package net.nextome.lismove.models;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "car_modifications")
public class CarModification {

	@Id
	private Long id;
	private Integer engineDisplacement;
	private String fuel;
	private String fuelConsumptionUrban;
	private String fuelConsumptionExtraurban;
	private String co2;
	@ManyToOne
	private CarGeneration generation;

	public CarModification() {
	}

	public CarModification(Long id, Integer engineDisplacement, String fuel, String fuelConsumptionUrban, String fuelConsumptionExtraurban, String co2, CarGeneration generation) {
		this.id = id;
		this.engineDisplacement = engineDisplacement;
		this.fuel = fuel;
		this.fuelConsumptionUrban = fuelConsumptionUrban;
		this.fuelConsumptionExtraurban = fuelConsumptionExtraurban;
		this.co2 = co2;
		this.generation = generation;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEngineDisplacement() {
		return engineDisplacement;
	}

	public void setEngineDisplacement(Integer engineDisplacement) {
		this.engineDisplacement = engineDisplacement;
	}

	public String getFuel() {
		return fuel;
	}

	public void setFuel(String fuel) {
		this.fuel = fuel;
	}

	public String getFuelConsumptionUrban() {
		return fuelConsumptionUrban;
	}

	public void setFuelConsumptionUrban(String fuelConsumptionUrban) {
		this.fuelConsumptionUrban = fuelConsumptionUrban;
	}

	public String getFuelConsumptionExtraurban() {
		return fuelConsumptionExtraurban;
	}

	public void setFuelConsumptionExtraurban(String fuelConsumptionExtraurban) {
		this.fuelConsumptionExtraurban = fuelConsumptionExtraurban;
	}

	public String getCo2() {
		return co2;
	}

	public void setCo2(String co2) {
		this.co2 = co2;
	}

	public CarGeneration getGeneration() {
		return generation;
	}

	public void setGeneration(CarGeneration generation) {
		this.generation = generation;
	}
}

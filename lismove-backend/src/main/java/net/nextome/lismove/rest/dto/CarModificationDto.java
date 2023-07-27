package net.nextome.lismove.rest.dto;

public class CarModificationDto {
	private Long id;
	private Integer engineDisplacement;
	private String fuel;
	private String fuelConsumptionUrban;
	private String fuelConsumptionExtraurban;
	private String co2;
	private Long generation;

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

	public Long getGeneration() {
		return generation;
	}

	public void setGeneration(Long generation) {
		this.generation = generation;
	}
}

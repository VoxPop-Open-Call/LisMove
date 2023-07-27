package net.nextome.lismove.models.enums;

public enum SessionType {
	BIKE("bici muscolare", "Bike"),
	ELECTRIC_BIKE("bici elettrica", "ElectricBike"),
	SCOOTER("monopattino"),
	FOOT("camminata"),
	CARPOOLING("carpooling");

	private final String name;
	private final String value;

	SessionType(String name) {
		this.name = name;
		value = "";
	}

	SessionType(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}

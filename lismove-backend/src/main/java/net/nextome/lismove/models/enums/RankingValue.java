package net.nextome.lismove.models.enums;

public enum RankingValue {
	URBAN_KM("distance"),
	WORK_KM("nationalKm"),
	WORK_NUM("session"),
	INITIATIVE_POINTS("points"),
	NATIONAL_POINTS("nationalPoints"),  //national
	NATIONAL_KM("nationalKm");  //national

	private final String fieldName;

	RankingValue() {
		this.fieldName = null;
	}

	RankingValue(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getColumnName() {
		return fieldName;
	}
}

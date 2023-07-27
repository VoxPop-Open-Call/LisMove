package net.nextome.lismove.models.enums;

public enum SessionStatus {
	VALID("Valid"),
	ERROR("General error"),
	DISTANCE_ERROR("Distance not accurate"),
	SPEED_ERROR("Average speed not valid"),
	VALID_OFFLINE("Validated with offline session"),
	NOT_CERTIFICATED("Not enough distance certificated by sensor"),
	DEBUG("Debug"),
	ACCELERATION_PEAK("Acceleration Peak"),
	CERTIFICATED("Certificated");

	private final String msg;

	SessionStatus(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}

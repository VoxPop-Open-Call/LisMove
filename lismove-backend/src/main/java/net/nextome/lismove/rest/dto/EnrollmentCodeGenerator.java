package net.nextome.lismove.rest.dto;

public class EnrollmentCodeGenerator {
	private Integer n = 1;
	private String start;
	private String end;

	public EnrollmentCodeGenerator() {
	}

	public EnrollmentCodeGenerator(String start, String end) {
		this.start = start;
		this.end = end;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
}

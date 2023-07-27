package net.nextome.lismove.rest.dto;

import java.util.List;

public class EnrollmentCodeEditor {

    private List<Long> selectedEnrollments;
    private String start;
    private String end;

    public List<Long> getSelectedEnrollments() {
        return selectedEnrollments;
    }

    public void setSelectedEnrollments(List<Long> selectedEnrollments) {
        this.selectedEnrollments = selectedEnrollments;
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

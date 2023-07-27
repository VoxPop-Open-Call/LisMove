package net.nextome.lismove.rest.dto;

public class SessionForwardDto {
    private String session_id;
    private String email;
    private Integer user_id;
    private Float km_to_assign;
    private Short can_gain_urb_nat_points;
    private Integer session_type;
    private String duration;
    private Long session_timestamp_start;
    private Long session_timestamp_end;
    private Short send_notification;

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Float getKm_to_assign() {
        return km_to_assign;
    }

    public void setKm_to_assign(Float km_to_assign) {
        this.km_to_assign = km_to_assign;
    }

    public Short getCan_gain_urb_nat_points() {
        return can_gain_urb_nat_points;
    }

    public void setCan_gain_urb_nat_points(Short can_gain_urb_nat_points) {
        this.can_gain_urb_nat_points = can_gain_urb_nat_points;
    }

    public Integer getSession_type() {
        return session_type;
    }

    public void setSession_type(Integer session_type) {
        this.session_type = session_type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Long getSession_timestamp_start() {
        return session_timestamp_start;
    }

    public void setSession_timestamp_start(Long session_timestamp_start) {
        this.session_timestamp_start = session_timestamp_start;
    }

    public Long getSession_timestamp_end() {
        return session_timestamp_end;
    }

    public void setSession_timestamp_end(Long session_timestamp_end) {
        this.session_timestamp_end = session_timestamp_end;
    }

    public Short getSend_notification() {
        return send_notification;
    }

    public void setSend_notification(Short send_notification) {
        this.send_notification = send_notification;
    }
}

package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VendorDto {
    private String businessName;
    private String address;
    private String vatNumber;
    private String iban;
    private String bic;
    private Boolean isVisible;
    private String email;
    private String firstName;
    private String lastName;
    private String uid;
    private String phone;
    private Boolean enableCoupon;
    private BigDecimal totalValueReleasedCoupon;
    private Integer numberAssignedCoupon;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getEnableCoupon() {
        return enableCoupon;
    }

    public void setEnableCoupon(Boolean enableCoupon) {
        this.enableCoupon = enableCoupon;
    }

    public BigDecimal getTotalValueReleasedCoupon() {
        return totalValueReleasedCoupon;
    }

    public void setTotalValueReleasedCoupon(BigDecimal totalValueReleasedCoupon) {
        this.totalValueReleasedCoupon = totalValueReleasedCoupon;
    }

    public Integer getNumberAssignedCoupon() {
        return numberAssignedCoupon;
    }

    public void setNumberAssignedCoupon(Integer numberAssignedCoupon) {
        this.numberAssignedCoupon = numberAssignedCoupon;
    }
}

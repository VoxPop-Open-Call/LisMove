package net.nextome.lismove.integrations.revolut.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Counterparty {
	private UUID id;
	private String name;
	private String state;
	private String company_name;
	private String bank_country;
	private String currency;
	private String iban;
	private String bic;
	private String email;
	private String phone;
	private RevolutAddress address;
	private RevolutIndividual individual_name;
	private String created_at;
	private String updated_at;
	private String type;
	private List<Counterparty> accounts;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getBank_country() {
		return bank_country;
	}

	public void setBank_country(String bank_country) {
		this.bank_country = bank_country;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public RevolutAddress getAddress() {
		return address;
	}

	public void setAddress(RevolutAddress address) {
		this.address = address;
	}

	public RevolutIndividual getIndividual_name() {
		return individual_name;
	}

	public void setIndividual_name(RevolutIndividual individual_name) {
		this.individual_name = individual_name;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Counterparty> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Counterparty> accounts) {
		this.accounts = accounts;
	}
}

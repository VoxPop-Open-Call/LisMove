package net.nextome.lismove.integrations.revolut.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevolutPayment {
	private String id;
	private String state;
	private String request_id;
	private UUID account_id;
	private Double amount;
	private String currency;
	private String reference;
	private RevolutPaymentReceiver receiver;
	private String created_at;
	private String completed_at;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getRequest_id() {
		return request_id;
	}

	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}

	public UUID getAccount_id() {
		return account_id;
	}

	public void setAccount_id(UUID account_id) {
		this.account_id = account_id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public RevolutPaymentReceiver getReceiver() {
		return receiver;
	}

	public void setReceiver(RevolutPaymentReceiver receiver) {
		this.receiver = receiver;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCompleted_at() {
		return completed_at;
	}

	public void setCompleted_at(String completed_at) {
		this.completed_at = completed_at;
	}
}

package net.nextome.lismove.integrations.revolut.models;

import java.util.UUID;

public class RevolutPaymentReceiver {
	private UUID counterparty_id;

	public RevolutPaymentReceiver(UUID counterparty_id) {
		this.counterparty_id = counterparty_id;
	}

	public UUID getCounterparty_id() {
		return counterparty_id;
	}

	public void setCounterparty_id(UUID counterparty_id) {
		this.counterparty_id = counterparty_id;
	}
}

package net.nextome.lismove.models.enums;

public enum RefundStatus {
    NO_REFUND("Sessione non valida, sessione nazionale o organizzazione con rimborsi disabilitati"),
    REFUND_DONE("L'intero importo è stato riconosciuto"),
    PARTIAL_DAILY("Parzialmente riconosciuto per raggiungimento soglia giornaliera"),
    PARTIAL_MONTHLY("Parzialmente riconosciuto per raggiungimento soglia mensile"),
    PARTIAL_INITIATIVE("Parzialmente riconosciuto per raggiungimento soglia iniziativa"),
    LIMIT_DAILY("Non riconosciuto per raggiungimento soglia giornaliera"),
    LIMIT_MONTHLY("Non riconosciuto per raggiungimento soglia mensile"),
    LIMIT_INITIATIVE("Non riconosciuto per raggiungimento soglia iniziativa");

    private final String msg;

    RefundStatus(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}

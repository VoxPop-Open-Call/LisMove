export const partialType = {
    UNKNOWN: 0,
    START: 1,
    END: 2,
    IN_PROGRESS: 3,
    PAUSE: 4,
    RESUME: 5,
    SESSION: 6,
    SYSTEM: 7,
    SERVICE: 8,
    BLE: 9,
    GPS: 10,
    OTHER: 11,
    SKIPPED: 12,
}

export const partialTypeName = [
    "Sconosciuto",
    "Inizio",
    "Fine",
    "In corso",
    "Pausa",
    "Ripresa",
    "Debug - Sessione",
    "Debug - Sistema",
    "Debug - Servizio",
    "Debug - BLE",
    "Debug - GPS",
    "Debug - Altro",
    "Scartato"
]

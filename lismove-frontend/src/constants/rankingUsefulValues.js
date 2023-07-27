export default function getUsefulValues (national) {
    if(!national) {
        return [
            {
                id: 0,
                name: "Km percorsi urbani"
            },
            {
                id: 1,
                name: "Km in bici per lavoro/scuola"
            },
            {
                id: 2,
                name: "Numero sessioni in bici per lavoro/scuola"
            },
            {
                id: 3,
                name: "Punti iniziativa"
            },
        ]
    } else {
        return [
            {
                id: 4,
                name: "Punti nazionali"
            },
            {
                id: 5,
                name: "Km percorsi nazionali"
            },
        ]
    }
}
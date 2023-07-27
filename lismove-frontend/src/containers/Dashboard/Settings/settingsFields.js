export function urbanPointsSettingsFields(values, setValues, error, setError, onError){
    return [
        {
            type: "switch",
            name: "isActiveUrbanPoints",
            label: "Punti Iniziativa Attivi",
            control: String(values.isActiveUrbanPoints) === "true",
            onChange: (event) => {
                let newValues = {...values};
                newValues[event.target.name] = event.target.checked;
                delete newValues.startDateBonus
                delete newValues.endDateBonus
                delete newValues.multiplier
                delete newValues.homeWorkPointsTolerance
                delete newValues.isActiveTimeSlotBonus
                delete newValues.endTimeBonus
                delete newValues.startTimeBonus
                setValues(newValues);
                onError(false);
                setError({});
            },
            xs: 12
        },
        {
            type: "date",
            name: "startDateBonus",
            label: "Data inizio bonus",
            condition1: values.isActiveUrbanPoints+"" === "true",
            xs: 6,
        },
        {
            type: "date",
            name: "endDateBonus",
            label: "Data fine bonus",
            condition1: values.isActiveUrbanPoints+"" === "true",
            xs: 6,
        },
        {
            type: "number",
            name: "multiplier",
            label: "Moltiplicatore",
            condition1: values.isActiveUrbanPoints+"" === "true",
            startAdornment: "x",
            min: 0,
            required: true,
            step: 0.01,
            xs: 6,
        },
        {
            type: "number",
            name: "homeWorkPointsTolerance",
            label: "Tolleranza in metri per calcolo coordinate",
            condition1: values.isActiveUrbanPoints+"" === "true",
            startAdornment: "Metri",
            min: 0,
            required: true,
            tooltip: "Indica i metri di tolleranza in linea d’aria tra i punti toccati dalla sessione e le coordinate di indirizzo casa e lavoro impostate dall'utente. La tolleranza degli indirizzi di lavoro delle sedi create da gestionale è impostata manualmente.",
            xs: 6,
        },
        {
            type: "switch",
            name: "isActiveTimeSlotBonus",
            label: "Bonus solo su fascia oraria",
            control: String(values.isActiveTimeSlotBonus) === "true",
            condition1: values.isActiveUrbanPoints+"" === "true",
            onChange: (event) => {
                setValues({...values,[ event.target.name ] : event.target.checked})
                delete values.startTimeBonus
                delete values.endTimeBonus
                delete error["startTimeBonus"]
                delete error["endTimeBonus"]
                if(Object.keys(error).length === 0) onError(false)
            },
            xs: 12
        },
        {
            type: "time",
            name: "startTimeBonus",
            label: "Ora inizio bonus",
            condition1: values.isActiveUrbanPoints+"" === "true",
            condition2: values.isActiveTimeSlotBonus+"" === "true",
            required: true,
            xs: 6,
        },
        {
            type: "time",
            name: "endTimeBonus",
            label: "Ora fine bonus",
            condition1: values.isActiveUrbanPoints+"" === "true",
            condition2: values.isActiveTimeSlotBonus+"" === "true",
            required: true,
            xs: 6,
        },
    ]
}

export function refundsFields(values){
    return [
        {
            type: "radio",
            label: "Rimborso al raggiungimento di:",
            name: "typeThresholdForRefund",
            options: [
                {
                    value: "km",
                    label:"Km"
                },
                {
                    value: "euro",
                    label: "Euro"
                }
            ],
            xs: 6,
        },
        {
            type: "number",
            name: "minThresholdForRefund",
            label: "Soglia a cui scatta il rimborso",
            startAdornment: values.typeThresholdForRefund === "euro" ? "€" : " Km",
            min: 0,
            step: values.typeThresholdForRefund === "euro" ? 0.01 : 1,
            required: true,
            tooltip: "Definisce raggiunti quanti " + (values.typeThresholdForRefund === "euro" ? "euro" : "km") + " scatta il rimborso. Il rimborso genera un coupon.",
            xs: 6,
        },
        {
            type: "radio",
            label: "Il coupon di rimborso della PA o spendibile con Esercenti?",
            name: "couponRefundType",
            options: [
                {
                    value: "esercenti",
                    label:"Esercenti"
                },
                {
                    value: "pa",
                    label: "PA"
                }
            ],
            xs: 12,
        },
        {
            type: "number",
            name: "minUrbanPointsForRefund",
            label: "Minimi punti iniziativa per rimborso",
            startAdornment: "Punti",
            min: 0,
            tooltip: "Se nullo, non c'è limite. Es 10 indica che l'utente deve aver fatto in una sessione almeno 1km per avere il rimborso (se 1pt = 100mt).",
            xs: 6,
        },
        {
            type: "number",
            name: "euroMaxRefundInADay",
            label: "Rimborso massimo di euro per una giornata",
            startAdornment: "€",
            min: 0,
            step: 0.01,
            tooltip: "Se non valorizzato, non esiste limite.",
            xs: 6,
        },
        {
            type: "number",
            name: "euroMaxRefundInAMonth",
            label: "Rimborso massimo di euro per mese solare",
            startAdornment: "€",
            min: 0,
            step: 0.01,
            tooltip: "Se non valorizzato, non esiste limite.",
            xs: 6,
        },
        {
            type: "number",
            name: "euroMaxRefundInATime",
            label: "Rimborso massimo di euro per periodo",
            startAdornment: "€",
            min: 0,
            step: 0.01,
            tooltip: "Se non valorizzato, non esiste limite.",
            xs: 6,
        },
    ]
}

export function homeWorkRefundsFields(values, setValues, setError, onError) {
    return [
        {
            type: "switch",
            name: "isActiveHomeWorkRefunds",
            label: "Rimborso Casa -> Scuola/Lavoro Attivo",
            control: String(values.isActiveHomeWorkRefunds) === "true",
            onChange: (event) => {
                let newValues = {...values};
                newValues[event.target.name] = event.target.checked;
                delete newValues.emailAddressApprovalManager
                event.target.checked ?  newValues.homeWorkRefundType = "urbanPoints" : delete newValues.homeWorkRefundType
                delete newValues.euroMaxRefundInADay
                delete newValues.euroMaxRefundInAMonth
                delete newValues.euroMaxRefundInATime
                delete newValues.valueKmHomeWorkBike
                delete newValues.valueKmHomeWorkElectricBike
                delete newValues.homeWorkPathTolerancePerc
                setValues(newValues);
                onError(false);
                setError({});
            }
        },
        {
            type: "switch",
            name: "addressValidation",
            label: "Validazione degli indirizzi da parte di un responsabile",
            control: String(values.addressValidation) === "true",
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            onChange: (event) => {
                let newValues = {...values};
                newValues[event.target.name] = event.target.checked;
                delete newValues.emailAddressApprovalManager
                setValues(newValues);
            }
        },
        {
            type: "text",
            name: "emailAddressApprovalManager",
            label: "Email Responsabile approvazioni indirizzi",
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            condition2: values.addressValidation+"" === "true",
            required: true,
            tooltip: "Email che riceverà le conferme per l'approvazione degli indirizzi di casa e lavoro degli utenti.",
            xs: 12,
        },
        {
            type: "radio",
            label: "Tipo Rimborso",
            name: "homeWorkRefundType",
            onChange: (newValue) => {
                let newValues = {...values};
                newValues["homeWorkRefundType"] = newValue;
                setValues(newValues);
            },
            options: [{value: "urbanPoints", label:"Punti Iniziativa"},{value: "euro", label: "Euro"}],
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            xs: 6,
        },
        {
            type: "number",
            name: "valueKmHomeWorkBike",
            label: "Valore del rimborso Percorso Casa->Scuola/Lavoro bici muscolare",
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            startAdornment: values.homeWorkRefundType === "euro" ? "€" : " Punti",
            min: 0,
            required: true,
            step: values.homeWorkRefundType === "euro" ? 0.01 : 1,
            tooltip: "Quanti " + (values.homeWorkRefundType === "euro" ? "euro" : "punti") + " vale un km di un percorso casa lavoro.",
            xs: 12,
        },
        {
            type: "number",
            name: "valueKmHomeWorkElectricBike",
            label: "Valore del rimborso Percorso Casa->Scuola/Lavoro bici elettrica",
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            startAdornment: values.homeWorkRefundType === "euro" ? "€" : " Punti",
            min: 0,
            step: values.homeWorkRefundType === "euro" ? 0.01 : 1,
            tooltip: "Quanti " + (values.homeWorkRefundType === "euro" ? "euro" : "punti") + " vale un km di un percorso casa lavoro. Se nullo, sarà considerato uguale al valore per bici muscolare.",
            xs: 12,
        },
        {
            type: "number",
            name: "homeWorkPathTolerancePerc",
            label: "Tolleranza tra percorso effettivo e nominale",
            condition1: values.isActiveHomeWorkRefunds+"" === "true",
            startAdornment: "%",
            min: 0,
            max: 100,
            required: true,
            tooltip: "Indica quanto il percorso effettivo può essere più lungo rispetto a quello nominale (0% indica che il percorso effettivo deve essere uguale a quello nominale)",
            xs: 12,
            onChange: (value) => {
                let newValues = {...values};
                if(value > 100) newValues["homeWorkPathTolerancePerc"] = 100;
                else if(value < 0) newValues["homeWorkPathTolerancePerc"] = 0;
                else newValues["homeWorkPathTolerancePerc"] = value;
                setValues(newValues);
            }
        }
    ]
}

export function urbanPathRefundsFields(values, setValues, setError, onError){
    return [
        {
            type: "switch",
            name: "isActiveUrbanPathRefunds",
            label: "Rimborso Tragitto Urbano Attivo",
            control: String(values.isActiveUrbanPathRefunds) === "true",
            onChange: (event) => {
                let newValues = {...values};
                newValues[event.target.name] = event.target.checked;
                delete newValues.euroValueKmUrbanPathBike
                delete newValues.euroValueKmUrbanPathElectricBike
                setValues(newValues);
                onError(false);
                setError({});
            }
        },
        {
            type: "number",
            name: "euroValueKmUrbanPathBike",
            label: "Rimborso in Euro per km su Percorso Urbano",
            condition1: values.isActiveUrbanPathRefunds+"" === "true",
            startAdornment: "€",
            min: 0,
            step: 0.01,
            required: true,
            xs: 12,
        },
        {
            type: "number",
            name: "euroValueKmUrbanPathElectricBike",
            label: "Rimborso in Euro per km su Percorso Urbano Bici Elettrica",
            condition1: values.isActiveUrbanPathRefunds+"" === "true",
            startAdornment: "€",
            tooltip: "Se nullo, sarà considerato uguale al valore per bici muscolare.",
            min: 0,
            step: 0.01,
            xs: 12,
        }
    ]
}

export function customFieldManagerFields(values, setValues) {
    return [
        {
            type : "switch",
            name : "exclusiveCustomField",
            label : "Valori jolly in esclusiva?",
            control : String(values.exclusiveCustomField) === "true",
            onChange : (event) => {
                let newValues = {...values};
                newValues[ event.target.name ] = event.target.checked;
                setValues(newValues);
            },
            xs: 12
        }
    ]
}

export function globalRanksAchievementsManagerFields(values, setValues) {
    return [
        {
            type : "switch",
            name : "canViewGlobalRanksAchievements",
            label : "Classifiche e coppe globali visibili all'utente?",
            control : String(values.canViewGlobalRanksAchievements) === "true",
            onChange : (event) => {
                let newValues = {...values};
                newValues[ event.target.name ] = event.target.checked;
                setValues(newValues);
            },
            xs: 12
        }
    ]
}

import Grid from "@mui/material/Grid";
import React,{useEffect,useState} from "react";
import {urbanPointsSettingsFields} from "./settingsFields";
import RenderFields from "./RenderFields";

export default function UrbanPointsSettings({values, setValues, onError}){

    let [error, setError] = useState({});
    let fields = urbanPointsSettingsFields(values, setValues, error, setError, onError)

    useEffect(() => {
        if(values.isActiveTimeSlotBonus+"" === "true"){
            checkError("endTimeBonus", values.endTimeBonus);
            checkError("startTimeBonus", values.startTimeBonus);
        }
        checkError("homeWorkPointsTolerance",values.homeWorkPointsTolerance);
        checkError("multiplier", values.multiplier);
        checkError("endDateBonus", values.endDateBonus);
        checkError("startDateBonus", values.startDateBonus);
    }, [values])

    const checkError = (prop, value) => {
        if(error && error[prop]) {
            delete error[prop]
            onError(false)
        }
        if(prop === "startDateBonus" && values.endDateBonus && value > values.endDateBonus) {
            setError({...error, startDateBonus : "valore non valido"})
            onError(true)
        }
        if(prop === "endDateBonus" && values.startDateBonus && value < values.startDateBonus) {
            setError({...error, endDateBonus : "valore non valido"})
            onError(true)
        }
        if(prop === "homeWorkPointsTolerance" && !value) {
            onError(true);
            setError({...error, homeWorkPointsTolerance : "campo obbligatorio"})
        }
        if(prop === "multiplier" && value < 1) {
            setError({...error, multiplier : "valore non valido"})
            onError(true)
        }
        if(values.isActiveUrbanPoints+"" === "true"){
            if(prop !== "startDateBonus" && prop !== "endDateBonus" && value === undefined) {
                setError({[prop] : "campo obbligatorio"})
                onError(true)
            }
        }
    }

    const onTextChange = (prop) => (value) => {
        let newValues = {...values};
        newValues[prop] = value;
        setValues(newValues);
        checkError(prop, value)
    }

    return <Grid container spacing={4} style={{margin : 0,width : "100%",marginBottom : "0.5rem"}}>

        <RenderFields fields={fields} values={values} error={error} onTextChange={onTextChange}/>

    </Grid>
}
import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import {urbanPathRefundsFields} from "./settingsFields";
import RenderFields from "./RenderFields";

export default function UrbanPathRefunds({values, setValues, onError}){

    let [error, setError] = useState({});
    let fields = urbanPathRefundsFields(values, setValues, setError, onError)

    useEffect(() => {
        if(values.isActiveUrbanPathRefunds+"" === "true") {
            checkError("euroValueKmUrbanPathBike",values.euroValueKmUrbanPathBike);
        }
    }, [values])

    const checkError = (prop, value) => {
        if(error && error[ prop ]) {
            delete error[ prop ]
            if(Object.keys(error).length === 0) onError(false)
        }
        if(prop === "euroValueKmUrbanPathBike" && !value) {
            onError(true);
            setError({...error, euroValueKmUrbanPathBike : "campo obbligatorio"})
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
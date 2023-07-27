import Grid from "@mui/material/Grid";
import React,{useEffect,useState} from "react";
import {homeWorkRefundsFields} from "./settingsFields";
import RenderFields from "./RenderFields";

export default function HomeWorkRefunds({values, setValues, onError}){

    let [error, setError] = useState({});
    let fields = homeWorkRefundsFields(values, setValues, setError, onError)

    useEffect(() => {
        if(values.isActiveHomeWorkRefunds+"" === "true") {
            checkError("homeWorkPathTolerancePerc",values.homeWorkPathTolerancePerc);
            checkError("valueKmHomeWorkBike",values.valueKmHomeWorkBike);
            if(values.addressValidation+"" === "true") checkError("emailAddressApprovalManager",values.emailAddressApprovalManager);
        }
    }, [values])

    const checkError = (prop, value) => {
        if(error && error[ prop ]) {
            delete error[ prop ]
            if(Object.keys(error).length === 0) onError(false)
        }
        if(prop === "emailAddressApprovalManager" && !value) {
            onError(true);
            setError({...error, emailAddressApprovalManager : "campo obbligatorio"})
        }
        if(prop === "valueKmHomeWorkBike" && !value) {
            onError(true);
            setError({...error, valueKmHomeWorkBike : "campo obbligatorio"})
        }
        if(prop === "homeWorkPathTolerancePerc" && !value) {
            onError(true);
            setError({...error, homeWorkPathTolerancePerc : "campo obbligatorio"})
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
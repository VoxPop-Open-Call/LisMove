import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import FormControl from "@mui/material/FormControl";
import { FormControlLabel, FormLabel, Radio, RadioGroup } from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import TextInput from "../../../components/forms/TextInput";
import InputAdornment from "@mui/material/InputAdornment";
import {CustomTooltip} from "../../../components/forms/CustomTooltip";
import RadioButtons from "../../../components/forms/RadioButtons";
import {refundsFields} from "./settingsFields";
import RenderFields from "./RenderFields";

export default function Refunds({values, setValues, onError}){

    let [error, setError] = useState({});
    let fields = refundsFields(values)

    useEffect(() => {
        checkError("minThresholdForRefund",values.minThresholdForRefund);
    }, [values])

    const checkError = (prop, value) => {
        if(error && error[ prop ]) {
            delete error[ prop ]
            if(Object.keys(error).length === 0) onError(false)
        }
        if(prop === "minThresholdForRefund" && !value) {
            onError(true);
            setError({...error, minThresholdForRefund : "campo obbligatorio"})
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
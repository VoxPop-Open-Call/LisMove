import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import TextInput from "../forms/TextInput";
import Grid from "@mui/material/Grid";
import React,{useEffect,useState} from "react";
import {Tooltip} from "@mui/material";
import {useGetPartialParameters} from "../../services/ContentManager";

const useStylesTooltip = makeStyles((theme) => ({
    arrow: {
        color: theme.palette.primary.light,
    },
    tooltip: {
        backgroundColor: theme.palette.primary.light,
        color: theme.palette.text.primary,
    },
}));

function CustomTooltip(props) {
    const classes = useStylesTooltip();

    return <Tooltip placement="" classes={classes} {...props} disableInteractive/>;
}

export default function PartialModal({open, onClose, onSubmit}){

    let {defaultParameters} = useGetPartialParameters();
    let [parameters, setParameters] = useState({});

    useEffect(() => {
        if(defaultParameters.length > 0) {
            setParameters({
                partialQty : defaultParameters[ 0 ] * 100,
                partialDeviation : defaultParameters[ 1 ] * 100,
                speedThreshold : defaultParameters[ 2 ]
            })
        }
    }, [defaultParameters])

    const onTextChange = (value, name) => {
        let newParameters = {...parameters};
        newParameters[name] = value.trim();
        setParameters(newParameters);
    }

    const save = () => {
        onSubmit(parameters)
        onClose()
    }

    return <BaseModal open={open} onClose={onClose} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>

                <CustomTooltip title={"Percentuale parziali validi per considerare la sessione valida"}>
                    <Grid item xs={12}>
                        <TextInput required label={"Percentuale parziali validi"} value={parameters.partialQty}
                                   type="number"
                                   min={0} max={100} step={0.1}
                                   onTextChange={(value) => {onTextChange(value, "partialQty")}}/>
                    </Grid>
                </CustomTooltip>

                <CustomTooltip title="Differenza in percentuale ammessa tra la distanza GPS calcolata e la distanza rilevata dal giroscopio">
                    <Grid item xs={12}>
                        <TextInput required label={"Differenza percentuale distanze"} value={parameters.partialDeviation}
                                   type="number"
                                   min={0} max={100} step={0.1}
                                   onTextChange={(value) => {onTextChange(value, "partialDeviation")}}/>
                    </Grid>
                </CustomTooltip>

                <CustomTooltip title="Soglia di velocità (in km/h) per considerare un parziale come fatto in macchina">
                    <Grid item xs={12}>
                        <TextInput required label={"Soglia di velocità (km/h)"} value={parameters.speedThreshold}
                                   type="number"
                                   min={0} max={150} step={0.1}
                                   onTextChange={(value) => {onTextChange(value, "speedThreshold")}}/>
                    </Grid>
                </CustomTooltip>


        </Grid>
    </BaseModal>
}
import BaseModal from "./BaseModal";
import React,{useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import dayjs from "dayjs";

export default function GenerateCodesModal({open, onClose, onSubmit}){

    const today = dayjs(new Date()).format("YYYY-MM-DD");
    let [values, setValues] = useState({n: 1, start: today, end: today});
    let [endError, setEndError] = useState(false);

    const save = () => {
        if(values.start > values.end){
            setEndError(true)
            return;
        }
        onSubmit(values)
        onClose()
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        newValues[name] = value.trim();
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={onClose} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
            <Grid item xs={12}>
                <TextInput required label={"Numero codici da generare"} value={values.n}
                           type="number"
                           min={0} step={1}
                           onTextChange={(value) => {onTextChange(value, "n")}}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Data inizio"} value={values.start}
                           type="date" min={today}
                           onTextChange={(value) => {onTextChange(value, "start")}}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Data fine"} value={values.end}
                           type="date"  min={today}
                           error={endError && "Data fine validità antecedente a data inizio"}
                           onTextChange={(value) => {onTextChange(value, "end")}}/>
            </Grid>
        </Grid>
    </BaseModal>
}
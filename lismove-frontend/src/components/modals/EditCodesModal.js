import BaseModal from "./BaseModal";
import React,{useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import dayjs from "dayjs";

export default function EditCodesModal({open, onClose, onSubmit}){

    const today = dayjs(new Date()).format("YYYY-MM-DD");
    let [values, setValues] = useState({ start: today, end: today});
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
        setEndError(false);
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={onClose} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
            <Grid item xs={12}>
                <TextInput required label={"Data inizio"} value={values.start}
                           type="date"
                           onTextChange={(value) => {onTextChange(value, "start")}}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Data fine"} value={values.end}
                           type="date"
                           error={endError && "Data fine validità antecedente a data inizio"}
                           onTextChange={(value) => {onTextChange(value, "end")}}/>
            </Grid>
        </Grid>
    </BaseModal>
}
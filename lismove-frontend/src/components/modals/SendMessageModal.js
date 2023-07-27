import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import BaseModal from "./BaseModal";
import React,{useState} from "react";

export default function SendMessageModal({open, onClose, onSave}){

    let [values, setValues] = useState({});
    let [error, setError] = useState({});

    const close = () => {
        setValues({});
        setError({});
        onClose();
    }

    const save = () => {
        if(!values.title) {
            setError({title : "Campo obbligatorio"});
            return;
        }
        onSave(values);
        close();
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        setError({});
        newValues[name] = value;
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={close} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1.5rem"}}>

            <Grid item xs={12}>
                <TextInput label={"Titolo"} value={values.title}
                           type="string"
                           onTextChange={(value) => {onTextChange(value, "title")}}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput label={"Corpo"} value={values.body}
                           type="string"
                           multiline={true}
                           onTextChange={(value) => {onTextChange(value, "body")}}/>
            </Grid>
        </Grid>
    </BaseModal>
}

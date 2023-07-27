import BaseModal from "./BaseModal";
import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import InputAdornment from "@mui/material/InputAdornment";

export default function EditExpandedCellModal({open, onSubmit, onClose, defaultValues}){

    let [values, setValues] = useState([])

    useEffect(() => {
        setValues(JSON.parse(JSON.stringify(defaultValues)))
    }, [defaultValues]);

    const submit = () => {
        let returnValues = []
        values.filter((v,i) => v.value !== defaultValues[i].value).map(v => returnValues.push({id: v.id, value: {[v.field]: v.value}}))
        onSubmit(returnValues)
        onClose()
    }

    const onTextChange = (value, key) => {
        let newValues = [...values];
        newValues.find(v => v.key === key).value = value
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={onClose} onSave={submit}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
            {
                values && values.map(value => <Grid item xs={12}>
                                        <TextInput label={value.key} value={value.value}
                                                   type={value.type || "string"}
                                                   startAdornment={value.startAdornment && <InputAdornment position="start">{value.startAdornment}</InputAdornment>}
                                                   onTextChange={(newValue) => {onTextChange(newValue, value.key)}}/>
                                    </Grid>)
            }
        </Grid>
    </BaseModal>

}
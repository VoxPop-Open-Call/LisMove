import BaseModal from "./BaseModal";
import React,{useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import InputAdornment from "@mui/material/InputAdornment";
import {useGetCities} from "../../services/ContentManager";
import NTMSelect from "../NTMSelect";

export default function CreateSeatModal({open, onClose, onSubmit}){

    let {cities = []} = useGetCities();
    let [values, setValues] = useState({});
    let [error, setError] = useState({});

    const save = () => {
        if(!values.name) {
            setError({name : "Campo obbligatorio"});
            return;
        }
        if(!values.address) {
            setError({address : "Campo obbligatorio"});
            return;
        }
        if(!values.number) {
            setError({number : "Campo obbligatorio"});
            return;
        }
        if(!values.cityName) {
            setError({cityName : "Campo obbligatorio"});
            return;
        }
        if(!cities.find(c => c.city.toLowerCase() === values.cityName.toLowerCase())){
            setError({cityName : "Città inesistente"});
            return;
        }
        if(!values.destinationTolerance) {
            setError({destinationTolerance : "Campo obbligatorio"});
            return;
        }
        if(values.destinationTolerance < 0) {
            setError({destinationTolerance : "Valore non valido"});
            return;
        }
        onSubmit(values)
        close()
    }

    const close = () => {
        setValues({})
        setError({});
        onClose()
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        newValues[name] = value;
        setError({});
        setValues(newValues);
    }

    const getFormattedCities = () => {
        let formattedCities = []
        cities.map(c => formattedCities.push({value : c.city, text: c.city}))
        return formattedCities
    }

    return <BaseModal open={open} onClose={close} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
            <Grid item xs={6}>
                <TextInput label={"Nome"} value={values.name}
                           type="string" required
                           onTextChange={(value) => {onTextChange(value, "name")}}
                           error={error.name}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Indirizzo"} value={values.address}
                           type="string" required
                           onTextChange={(value) => {onTextChange(value, "address")}}
                           error={error.address}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"N. Civico"} value={values.number}
                           type="number" required
                           onTextChange={(value) => {onTextChange(value, "number")}}
                           error={error.number}/>
            </Grid>
            <Grid item xs={6}>
                <NTMSelect label={"Città"} value={values.cityName || ""} maxItemsSize={3}
                           required items={getFormattedCities()} color={"light"}
                           onChange={(value) => {onTextChange(value, "cityName")}}
                           error={error.cityName}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Tolleranza"} value={values.destinationTolerance}
                           type="number" required min={0} step={0.01}
                           startAdornment={<InputAdornment position="start">km</InputAdornment>}
                           onTextChange={(value) => {onTextChange(value, "destinationTolerance")}}
                           error={error.destinationTolerance}/>
            </Grid>
        </Grid>
    </BaseModal>
}

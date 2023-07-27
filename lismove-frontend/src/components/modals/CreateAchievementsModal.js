import React,{useState} from "react";
import BaseModal from "./BaseModal";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import MenuItem from "@mui/material/MenuItem";
import {sessionType} from "../../constants/sessionType";
import FileInput from "../forms/FileInput";
import { Alert } from '@mui/material';

const genderOptions = [
    {
        id: "M",
        name: "Maschio"
    },
    {
        id: "F",
        name: "Femmina"
    }
]

export default function CreateAchievementsModal({open, onClose, onSubmit, usefulValues, achievementsFilter, organizationId, infoLogoDimension}){

    let [values, setValues] = useState({});
    let [error, setError] = useState({});

    const save = () => {

        let convertedValues = {...values};
        convertedValues["startDate"] = Date.parse(values["startDate"])
        convertedValues["endDate"] = Date.parse(values["endDate"])

        if(!convertedValues.name) return setError({name : "Campo obbligatorio"})
        if(!convertedValues.startDate) return setError({startDate: "Campo Obbligatorio"})
        if(!convertedValues.endDate) return setError({endDate: "Campo Obbligatorio"})
        if(!convertedValues.duration) return setError({duration : "Campo obbligatorio"})
        if(convertedValues.duration < 0) return setError({duration : "Valore non valido"})
        if(convertedValues.startDate > convertedValues.endDate) return setError({endDate: "Data precedente a data inizio"})
        if(convertedValues.value === null || convertedValues.value === undefined) return setError({value : "Campo obbligatorio"})
        if(!convertedValues.target) return setError({target : "Campo obbligatorio"})
        if(convertedValues.target < 0) return setError({target : "Valore non valido"})

        if(convertedValues.filter !== null && (convertedValues.filter === 0 || convertedValues.filter === 2) && (convertedValues.filterValue === null || convertedValues.filterValue === undefined)) {
            return setError({filterValue : "Campo obbligatorio"})
        }
        if(convertedValues.filter !== null && convertedValues.filter === 1) {
            if(!convertedValues.minAge) return setError({minAge : "Campo obbligatorio"})
            if(convertedValues.minAge < 1) return setError({minAge : "Valore non valido"})
            if(!convertedValues.maxAge) return setError({maxAge : "Campo obbligatorio"})
            if(convertedValues.maxAge < convertedValues.minAge) return setError({maxAge : "Età inferiore al minimo"})
            convertedValues.filterValue = convertedValues.minAge + " - " + convertedValues.maxAge
            delete convertedValues.minAge
            delete convertedValues.maxAge
        }
        if(convertedValues.filter === '') delete convertedValues.filter
        onSubmit(convertedValues)
        close()
    }

    const close = () => {
        setValues({})
        setError({});
        onClose()
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        if(name === "filter") delete newValues.filterValue
        newValues[name] = value;
        setError({});
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={close} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
            <Grid item xs={12}>
                <TextInput label={"Nome"} value={values.name}
                           type="string" required
                           onTextChange={(value) => {onTextChange(value, "name")}}
                           error={error.name}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Data inizio"} value={values.startDate}
                           type="date"
                           InputLabelProps={{shrink : true}}
                           onTextChange={(value) => {
                               onTextChange(value,"startDate")
                           }}
                           error={error.startDate}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Data fine"} value={values.endDate}
                           type="date" min={values.startDate}
                           InputLabelProps={{shrink : true}}
                           onTextChange={(value) => {
                               onTextChange(value,"endDate")
                           }}
                           error={error.endDate}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Durata in giorni"} value={values.duration}
                           type="number"
                           min={1} step={1}
                           onTextChange={(value) => {onTextChange(value, "duration")}}
                           error={error.duration}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Valore"} value={values.value}
                           options={usefulValues} select
                           onTextChange={(value) => {onTextChange(value, "value")}}
                           error={error.value}>
                    {usefulValues.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                </TextInput>
            </Grid>
            <Grid item xs={12}>
                <TextInput required label={"Obiettivo"} value={values.target}
                           type="number"
                           min={1} step={1}
                           onTextChange={(value) => {onTextChange(value, "target")}}
                           error={error.target}/>
            </Grid>
            <Grid item xs={12}>
                <TextInput label={"Categoria filtro"} value={values.filter}
                           options={achievementsFilter} select
                           onTextChange={(value) => {onTextChange(value, "filter")}}
                           error={error.filter}>
                    <MenuItem key='' value=''>
                        <br/>
                    </MenuItem>
                    {achievementsFilter.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                </TextInput>
            </Grid>
            {
                values.filter !== null && values.filter === 0 &&
                <Grid item xs={12}>
                    <TextInput required label={"Valore filtro"} value={values.filterValue}
                               options={sessionType} select
                               onTextChange={(value) => {
                                   onTextChange(value,"filterValue")
                               }}
                               error={error.filterValue}>
                        {sessionType.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                    </TextInput>
                </Grid>
            }
            {
                values.filter !== null && values.filter === 1 &&
                <>
                    <Grid item xs={6}>
                        <TextInput required label={"Età minima"} value={values.minAge}
                                   type="number"
                                   min={1} step={1}
                                   onTextChange={(value) => {onTextChange(value, "minAge")}}
                                   error={error.minAge}/>
                    </Grid>
                    <Grid item xs={6}>
                        <TextInput required label={"Età massima"} value={values.maxAge}
                                   type="number"
                                   min={1} step={1}
                                   onTextChange={(value) => {onTextChange(value, "maxAge")}}
                                   error={error.maxAge}/>
                    </Grid>
                </>
            }
            {
                values.filter !== null && values.filter === 2 &&
                <Grid item xs={12}>
                    <TextInput required label={"Genere"} value={values.filterValue}
                               options={genderOptions} select
                               onTextChange={(value) => {
                                   onTextChange(value,"filterValue")
                               }}
                               error={error.filterValue}>
                        {genderOptions.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                    </TextInput>
                </Grid>
            }
            <Grid item xs={12}>
                <Alert severity="info">{infoLogoDimension}</Alert>
            </Grid>
            <Grid item xs={12}>
                <FileInput folder={"organizations/achievements"} prefix={organizationId} onRequestSave={(e) => setValues({ ...values, "logo": e })} label={"Trascina l'immagine"}/>
            </Grid>
        </Grid>
    </BaseModal>
}
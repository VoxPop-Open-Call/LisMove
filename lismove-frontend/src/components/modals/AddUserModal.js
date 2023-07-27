import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import BaseModal from "./BaseModal";
import React,{useState} from "react";
import dayjs from "dayjs";
import MenuItem from "@mui/material/MenuItem";

const genderOptions = [
    {
        id: "Maschio",
        name: "Maschio"
    },
    {
        id: "Femmina",
        name: "Femmina"
    }
]


export default function AddUserModal({open, onClose, onSave}){

    const today = dayjs(new Date()).format("YYYY-MM-DD");
    let [values, setValues] = useState({});
    let [error, setError] = useState({});

    const close = () => {
        setValues({});
        setError({});
        onClose();
    }

    const save = () => {
        if(!values.email) {
            setError({email : "Campo obbligatorio"});
            return;
        }
        if(!values.password) {
            setError({...error, password : "Campo obbligatorio"});
            return;
        }
        onSave(values);
        close();
    }

    const onTextChangeNoSpace = (value, name) => {
        let newValues = {...values};
        setError({});
        newValues[name] = value.trim();
        setValues(newValues);
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        setError({});
        newValues[name] = value;
        setValues(newValues);
    }

    return <BaseModal open={open} onClose={close} onSave={save}>
        <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1.5rem"}}>

            <Grid item xs={6}>
                <TextInput label={"Nome"} value={values.firstName}
                           type="string"
                           onTextChange={(value) => {onTextChange(value, "firstName")}}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Cognome"} value={values.lastName}
                           type="string"
                           onTextChange={(value) => {onTextChange(value, "lastName")}}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Username"} value={values.username}
                           type="string"
                           onTextChange={(value) => {onTextChange(value, "username")}}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput value={values.gender} options={genderOptions} label={"Genere"} onTextChange={(value) => {onTextChange(value, "gender")}} select>
                    {genderOptions.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                </TextInput>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Numero di Telefono"} value={values.phoneNumber}
                           type="number" min={0} step={1}
                           onTextChange={(value) => {onTextChangeNoSpace(value, "phoneNumber")}}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput label={"Data di Nascita"} value={values.birthDate}
                           type="date" max={today}
                           onTextChange={(value) => {onTextChangeNoSpace(value, "birthDate")}}
                           InputLabelProps={{
                                shrink: true,
                           }}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput required label={"Email"} value={values.email}
                           onTextChange={(value) => {onTextChangeNoSpace(value, "email")}}
                           error={error.email}/>
            </Grid>
            <Grid item xs={6}>
                <TextInput required label={"Password"} value={values.password} type="password"
                           onTextChange={(value) => {onTextChangeNoSpace(value, "password")}} showPasswordIcon
                           error={error.password}/>
            </Grid>

        </Grid>
    </BaseModal>
}
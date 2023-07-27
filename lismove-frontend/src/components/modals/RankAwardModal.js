import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import FormControl from "@mui/material/FormControl";
import {FormControlLabel,FormLabel,Radio,RadioGroup} from "@mui/material";
import FileInput from "../forms/FileInput";
import {useParams} from "react-router-dom";


const useStyles = makeStyles((theme) => ({
        radioLabel : {
            color : theme.palette.primary.light,
            display : 'flex',
            alignContent : "space-between",
            '&.Mui-focused' : {
                color : theme.palette.primary.light
            }
        },
        radioButton: {
            color: theme.palette.primary.light
        }
    }
));

export default function RankAwardModal({open, onClose, onSubmit, defaultValues = {}, isEditing}) {

    let {id} = useParams();
    let [values, setValues] = useState({positionType: 0,type: 0});
    let [error, setError] = useState({});
    let classes = useStyles();

    useEffect(() => {
        let newValues = {...defaultValues};
        newValues["positionType"] = 0;
        if(newValues.type === null || newValues.type === undefined) newValues["type"] = 0;
        if(newValues.type === "MONEY") newValues.type = 0;
        if(newValues.type === "POINTS") newValues.type = 1;

        setValues(newValues);
    }, [open])

    const close = () => {
        setValues({positionType: 0,type: 0})
        setError({})
        onClose();
    }

    const save = () => {
        if(!values.name) {
            setError({name: "Campo obbligatorio"});
            return;
        }
        if(!isEditing && values.positionType === 0 && !values.position){
            setError({position: "Campo obbligatorio"});
            return;
        }
        if(!isEditing && values.positionType === 1){
            if(!values.minPosition){
                setError({minPosition: "Campo obbligatorio"});
                return;
            }
            if(!values.maxPosition){
                setError({maxPosition: "Campo obbligatorio"});
                return;
            }
            if(parseInt(values.maxPosition) <= parseInt(values.minPosition)){
                setError({maxPosition: "Valore invalido"});
                return;
            }
            if(!values.amount || values.amount < 0){
                setError({amount: "Valore invalido"});
                return;
            }
        }
        if(!values.value || values.value < 0){
            setError({value: "Valore invalido"});
            return;
        }
        onSubmit(values);
        close()
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        newValues[name] = value;
        setError({});
        setValues(newValues);
    }

    return (
        <BaseModal open={open} onClose={close} onSave={save}>
            <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
                <Grid item xs={12}>
                    <TextInput label={"Nome"} value={values.name}
                               type="string" required
                               onTextChange={(value) => {onTextChange(value, "name")}}
                               error={error.name}/>
                </Grid>
                {!isEditing &&
                    <>
                        <Grid item xs={12}>
                            <FormControl component="fieldset">
                                <FormLabel component="legend" className={classes.radioLabel}>Tipo posizione</FormLabel>
                                <RadioGroup row value={values.positionType}
                                            onChange={(event) => onTextChange(parseInt(event.target.value),"positionType")}
                                            className={classes.radioLabel}>
                                    <FormControlLabel value={0} control={<Radio className={classes.radioButton}/>}
                                                      label="Posizione"/>
                                    <FormControlLabel value={1} control={<Radio className={classes.radioButton}/>}
                                                      label="Intervallo di posizioni"/>
                                </RadioGroup>
                            </FormControl>
                        </Grid>
                        {
                            values.positionType === 0 && <Grid item xs={12}>
                                <TextInput required label={"Posizione in classifica"} value={values.position}
                                    type="number"
                                    min={1} step={1}
                                    onTextChange={(value) => onTextChange(value,"position")}
                                    error={error.position}/>
                            </Grid>
                        }
                        {
                            values.positionType === 1 && <>
                                <Grid item xs={6}>
                                    <TextInput label={"Posizione minima"} value={values.minPosition}
                                        type="number" required
                                        onTextChange={(value) => {onTextChange(value, "minPosition")}}
                                        error={error.minPosition}/>
                                </Grid>
                                <Grid item xs={6}>
                                    <TextInput label={"Posizione massima"} value={values.maxPosition}
                                        type="number" required
                                        onTextChange={(value) => {onTextChange(value, "maxPosition")}}
                                        error={error.maxPosition}/>
                                </Grid>
                                <Grid item xs={12}>
                                    <TextInput required label={"Numero di premi"} value={values.amount}
                                        type="number"
                                        min={1} step={1}
                                        onTextChange={(value) => onTextChange(value,"amount")}
                                        error={error.amount}/>
                                </Grid>
                            </>
                        }
                    </>
                }
                <Grid item xs={12}>
                    <FormControl component="fieldset">
                        <FormLabel component="legend" className={classes.radioLabel}>Tipo Valore</FormLabel>
                        <RadioGroup row value={values.type} onChange={(event) => onTextChange(parseInt(event.target.value), "type")} className={classes.radioLabel}>
                            <FormControlLabel value={0} control={<Radio className={classes.radioButton}/>} label="Euro"/>
                            <FormControlLabel value={1} control={<Radio className={classes.radioButton}/>} label="Punti"/>
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item xs={12}>
                    <TextInput required label={"Valore in "+ (values.type === 0 ? "euro" : "punti")} value={values.value}
                               type="number"
                               min={1} step={1}
                               onTextChange={(value) => {onTextChange(value, "value")}}
                               error={error.value}/>
                </Grid>
                <Grid item xs={12}>
                    <TextInput label={"Descrizione"} value={values.description}
                               type="string" multiline
                               onTextChange={(value) => {onTextChange(value, "description")}}
                               error={error.description}/>
                </Grid>
                <Grid item xs={12}>
                    <FileInput folder={"organizations/awards"} prefix={id} onRequestSave={(e) => setValues({ ...values, "imageUrl": e })} label={"Trascina l'immagine"}/>
                </Grid>
            </Grid>
        </BaseModal>
    );

}
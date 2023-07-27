import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import TextInput from "../forms/TextInput";
import Grid from "@mui/material/Grid";
import {FormControlLabel,Radio,RadioGroup} from "@mui/material";

const useStyles = makeStyles((theme) => ({
    radioLabel: {
        color: theme.palette.primary.contrastText
    },
    radioButton: {
        color: theme.palette.primary.contrastText
    },
}));

const organizationType = {
    PA: "0",
    COMPANY: "1"
}

export default function OrganizationModal({open, onClose, onSubmit}){

    let [title, setTitle] = useState("");
    let [titleError, setTitleError] = useState(false);
    let [type, setType] = useState(organizationType.PA);
    let classes = useStyles();

    const save = () => {
        if(title === "") {
            setTitleError(true)
            return
        }
        onSubmit({title, type})
        close()
    }

    const close = () => {
        setTitle("")
        setType(organizationType.PA)
        setTitleError(false)
        onClose()
    }

    return (
        <BaseModal open={open} onClose={close} onSave={save}>
            <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>

                <Grid item xs={12}>
                    <TextInput required label={"Titolo"} value={title}
                               onTextChange={(value) => {
                                    setTitle(value);
                                    setTitleError(false);
                                }}
                               error={titleError}/>
                </Grid>

                <Grid item xs={12}>
                    <RadioGroup value={type} onChange={(event) => setType(event.target.value)} className={classes.radioLabel}>
                        <FormControlLabel value={organizationType.PA} control={<Radio className={classes.radioButton}/>} label="Pubblica Amministrazione" />
                        <FormControlLabel value={organizationType.COMPANY} control={<Radio className={classes.radioButton}/>} label="Azienda" />
                    </RadioGroup>
                </Grid>
            </Grid>
        </BaseModal>
    );
}
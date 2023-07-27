import FileInput from "../forms/FileInput";
import Grid from "@mui/material/Grid";
import React,{useState} from "react";
import BaseModal from "./BaseModal";
import { Alert } from '@mui/material';

export default function EditAvatarModal({open, onClose, onSubmit, folder, prefix, label, infoMessage, warningMessage}){

    let [value, setValue] = useState("");

    const submit = () => {
        onSubmit(value)
        onClose()
    }

    return <BaseModal open={open} onClose={onClose} onSave={submit}>
        {warningMessage &&
        <Grid item xs={12} style={{marginBottom : "0.5rem"}}>
            <Alert severity="warning">{warningMessage}</Alert>
        </Grid>
        }
        {infoMessage &&
        <Grid item xs={12} style={{marginBottom: "0.5rem"}}>
            <Alert severity="info">{infoMessage}</Alert>
        </Grid>
        }
        <Grid container justifyContent={"center"}>
            <Grid container spacing={4} style={{margin: 0, width: "15rem", marginBottom: "0.1rem"}}>
                <Grid item xs={12}>
                    <FileInput folder={folder} prefix={prefix} onRequestSave={(e) => setValue(e)} label={label}/>
                </Grid>
            </Grid>
        </Grid>

    </BaseModal>
}
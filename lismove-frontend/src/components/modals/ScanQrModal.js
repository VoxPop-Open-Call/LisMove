import BaseModal from "./BaseModal";
import React from "react";
import QrReader from 'react-qr-reader'
import {Grid} from "@mui/material";


export default function ScanQrModal({open, onClose, onScan, onError}){
    return <BaseModal open={open} onClose={onClose} >
        <Grid item xs={12} style={{ width: '50vw', maxHeight:'90vh' }}>
            <QrReader
                delay={300}
                onError={(e) => {
                    onError(e);
                    onClose();
                }}
                onScan={(text)=>{
                    if(text)
                    {
                        onScan(text);
                        onClose();
                    }
                }}
                style={{ width: '100%'}}
            />
        </Grid>
    </BaseModal>
}
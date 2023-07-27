import React,{useEffect,useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import BaseModal from "./BaseModal";
import Grid from "@mui/material/Grid";
import MapContainer from "../MapContainer";
import {Marker} from "@react-google-maps/api";
import Typography from "@mui/material/Typography";

const useStyles = makeStyles((theme) => ({
    title: {
        fontWeight: "bold",
        color: theme.palette.primary.light
    },
}));

export default function EditCoordinatesModal({open, defaultLat, defaultLng, onSubmit, onClose}) {

    let [lat, setLat] = useState(defaultLat)
    let [lng, setLng] = useState(defaultLng);

    useEffect(()=>{
        setLat(defaultLat);
        setLng(defaultLng)
    },[defaultLat,defaultLng])

    let classes = useStyles();

    useEffect(() => {
        if(open){
            defaultLat ? setLat(defaultLat) : setLat(41.9028);
            defaultLng ? setLng(defaultLng) : setLng(12.4964);
        }
    }, [open]);

    const submit = () => {
        onSubmit(lat, lng);
        onClose();
    }

    const onDragEnd = (newPosition) => {
        setLat(newPosition.latLng.lat());
        setLng(newPosition.latLng.lng());
    }

    return (
        <BaseModal open={open} onClose={onClose} onSave={submit} fullWidth>
            <Typography variant="h5" align="center" gutterBottom className={classes.title}>
                Trascina il cursore nella nuova posizione
            </Typography>
            <Typography variant="body2" align="center" gutterBottom className={classes.title}>
                Nuova posizione: {lat} - {lng}
            </Typography>
            <Grid container spacing={4} style={{margin: 0, width: "99.9%", height: "70vh", marginBottom: "1rem"}}>
                <MapContainer center={{lat: lat, lng: lng}}>
                    <Marker position={{lat: lat, lng: lng}} draggable onDragEnd={onDragEnd}/>
                </MapContainer>
            </Grid>
        </BaseModal>
    );

}
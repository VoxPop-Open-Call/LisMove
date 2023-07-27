import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import HoverableButton from "../layout/HoverableButton";
import InfoIcon from "@mui/icons-material/Info";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import MapContainer from "../MapContainer";
import {Marker} from "@react-google-maps/api";
import BaseModal from "../modals/BaseModal";

const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    },
    title: {
        fontWeight: "bold",
        color: theme.palette.primary.light
    },
}));

export default function RenderCoordinates({lat, lng}){

    let classes = useStyles();
    let [modal, setModal] = useState(false);

    return (
        <div>

            <HoverableButton
                item={{
                    icon: <InfoIcon/>,
                    name: lat || lng ? `${lat || ''} - ${lng || ''}` : ""
                }}
                onClick={() => setModal(true)}
                classes={{root: classes.root}}
            />

            <BaseModal open={modal} onClose={() => setModal(false)} fullWidth>
                <Typography variant="body2" align="center" gutterBottom className={classes.title}>
                    Posizione: {lat} - {lng}
                </Typography>
                <Grid container spacing={4} style={{margin: 0, width: "99.9%", height: "70vh", marginBottom: "1rem"}}>
                    <MapContainer center={{lat: lat, lng: lng}}>
                        <Marker position={{lat: lat, lng: lng}}/>
                    </MapContainer>
                </Grid>
            </BaseModal>

        </div>
    );
}
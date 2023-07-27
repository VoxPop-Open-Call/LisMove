import {CircularProgress, Grid} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import React from "react";

const useStyles = makeStyles(theme => ({
    center: {
        position: 'fixed',
        top: 'calc(50vh - 20px)',
        left: 'calc(50vw - 20px)'
    }
}));

/**
 * icona di loading circolare al centro dello schermo
 * @returns {JSX.Element}
 * @constructor
 */
export default function CircularLoading(){
    const classes = useStyles();
    return <Grid className={classes.center} container><CircularProgress/></Grid>
}
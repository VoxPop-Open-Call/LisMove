import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import ButtonBase from "@mui/material/ButtonBase";
import {useSelector} from "react-redux";
import Can from "../../services/Can";

const useStyles = makeStyles(() => ({
    layerController: {
        display: "flex",
        flexDirection: "column",
        margin: "1rem",
    },
    addElementEnabled:{
        fontSize: "2rem",
        textAlign: "center",
        backgroundColor: "white",
        color: props => props.color,
        border: "solid 2px",
        borderColor: props => props.color,
        borderLeftWidth: "0",
        width:"42px",
        lineHeight: "40px",
        "&:hover": {
            backgroundColor: props => props.color,
            color: "white"
        }
    },
    addElementDisabled:{
        fontSize: "2rem",
        textAlign: "center",
        backgroundColor: "white",
        color: "#e1e1e1",
        border: "solid #E1E1E1 2px",
        borderLeftWidth: "0",
        width:"42px",
        lineHeight: "40px"
    }
}));

export default function LayerController ({addElement, title, src, hover, off, onClick, enabled, color}) {
    const classes = useStyles({color});
    let venue = useSelector(store => store.session.venue);

    return (
        <Can I="read" a={venue.id + ""} field={title.toLowerCase()}>
            <Grid className={classes.layerController}>

                <Grid style={{color : "grey"}}>{title}</Grid>

                <Grid>
                    <ButtonBase  onClick={onClick} disableTouchRipple style={enabled ? {border: "solid "+color+" 2px"} : {border: "solid #E1E1E1 2px"}}>
                        <img src={enabled ? src : off} alt={title} onMouseOver={e => (e.currentTarget.src = hover)}
                             onMouseOut={e => (e.currentTarget.src = (enabled ? src : off))}/>
                    </ButtonBase >
                    <Can I="write" a={venue.id + ""} field={title.toLowerCase()}>
                        <ButtonBase disableTouchRipple className={enabled ? classes.addElementEnabled : classes.addElementDisabled} onClick={enabled && addElement}>
                            +
                        </ButtonBase>
                    </Can>
                </Grid>
            </Grid>
        </Can>
    );
}
import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import React from "react";
import dayjs from "dayjs";
import Typography from "@mui/material/Typography";
import {Divider,ListItem,ListItemAvatar,ListItemText} from "@mui/material";
import List from "@mui/material/List";

const useStyles = makeStyles((theme) => ({
    preview : {
        color : theme.palette.primary.light,
        "white-space" : "pre-line",
        paddingRight: theme.spacing(2),
        paddingBottom: theme.spacing(5),
        maxHeight: "34rem"
    },
    title: {
        padding: theme.spacing(1),
        fontWeight: "bold"
    },
    listItemText:{
        fontSize:'1.3rem',
    }
}));

export default function PreviewRepetitionModal({open, onClose, values}) {

    let classes = useStyles();
    let preview = [];

    if(values.repeatType === "month" || values.repeatType === "MONTH") {
        let startMonth = new Date(values["startDate"]);
        let year = startMonth.getFullYear();
        let month = startMonth.getMonth();
        for(let i = 0; i < values.repeatNum; i++){
            preview.push({
                start: dayjs(new Date(year, month++, 1)).format('DD/MM/YYYY'),
                end: dayjs(new Date(year, month, 0)).format('DD/MM/YYYY')
            })
        }
    }

    if(values.repeatType === "custom" || values.repeatType === "CUSTOM") {
        let startDate = new Date(values["startDate"])
        let endDate = new Date(values["endDate"])
        const diffTime = Math.abs(endDate - startDate);
        const dayLife = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        for(let i = 0; i < values.repeatNum; i++){
            let endDate = new Date();
            endDate.setDate(startDate.getDate() + dayLife)
            preview.push({
                start: dayjs(startDate).format('DD/MM/YYYY'),
                end: dayjs(endDate).format('DD/MM/YYYY')
            })
            startDate.setDate(endDate.getDate() + 1);
        }
    }

    return (
        <BaseModal open={open} onClose={onClose}>
            <Grid container className={classes.preview} direction="column">
                <Grid item xs>
                    <Typography gutterBottom variant="h4" className={classes.title} >
                        Anteprima Classifiche
                    </Typography>
                    <List>
                    {
                        preview.map((p, i) => {
                            return <>
                                <ListItem key={i}>
                                    <ListItemAvatar>
                                        <Typography gutterBottom variant="h5">
                                            {i+1}.
                                        </Typography>
                                    </ListItemAvatar>
                                    <ListItemText primary={p.start + " - " +p.end} classes={{primary:classes.listItemText}}/>
                                </ListItem>
                                {i !== preview.length-1 && <Divider variant="inset" component="li"/>}
                            </>
                        })
                    }
                    </List>
                    </Grid>
            </Grid>
        </BaseModal>
    );
}
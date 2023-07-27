import {useGetMessageDetails} from "../../../services/ContentManager";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import Button from "@mui/material/Button";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import React from "react";
import NTMXGrid from "../../../components/NTMXGrid";
import RenderBoolean from "../../../components/cellRender/RenderBoolean";
import RenderUserRedirect from "../../../components/cellRender/RenderUserRedirect";

const useStyles = makeStyles(theme => ({
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    title: {
        padding: theme.spacing(1),
        fontWeight: "bold",
        color: theme.palette.primary.main,
        fontSize: 35,
        textAlign: "center"
    },
    text: {
        paddingLeft: theme.spacing(1),
        fontWeight: "bold",
        color: theme.palette.text.primary
    },
    box: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        borderBottom: `${theme.palette.secondary.main} 1px solid`,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        flexGrow: 1,
    },
}));

export default function MessageInfo({goBack, messageId}){

    let classes = useStyles();
    const {message = {}} = useGetMessageDetails(messageId)

    const defaultColumns = [
        {
            headerName: 'Utente',
            field: 'username',
            width: 200,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
        },
        {
            headerName: 'Letta',
            field: 'read',
            width: 200,
            renderCell: (params) => <RenderBoolean params={params}/>
        }
    ];

    return (
        <Grid container justifyContent="space-between">
            <Grid container justifyContent="space-between">
                <Button onClick={goBack} className={classes.backButton}>
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
            </Grid>
            <Grid xs={12}>
                <div className={classes.title}>{message.title}</div>
            </Grid>
            <NTMXGrid
                columns={defaultColumns}
                rows={message.receivers || []}
                getRowId={(row) => message && row.id}
            />
        </Grid>
    );
}

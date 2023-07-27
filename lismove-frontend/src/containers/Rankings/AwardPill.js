import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import {
    AccordionDetails,
    Avatar,
    Accordion,
    AccordionSummary,
    IconButton
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import {TextItem} from "../../components/TextItem";
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import RemoveIcon from '@mui/icons-material/Remove';
import AddIcon from "@mui/icons-material/Add";
import RankAwardModal from "../../components/modals/RankAwardModal";
import ConfirmIconButton from "../../components/buttons/ConfirmIconButton";
import {AWARDS,deleteElem,getErrorMessage,post,put,RANKINGS} from "../../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import NTMXGrid from "../../components/NTMXGrid";
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';

const useStyles = makeStyles((theme) => ({
    root: {
        margin: theme.spacing(1),
    },
    header: {
        fontWeight: "bold",
    },
    image: {
        height: "4rem",
        width: "4rem",
        marginRight: theme.spacing(2),
    },
    expanded: {
        backgroundColor: theme.palette.primary.dark
    },
    textItem: {
        color: "white",
        borderColor: theme.palette.primary.light,
        height: "100%",
    },
    winnersTable: {
        margin: theme.spacing(2),
        height: "18rem",
    }
}));

export function AwardPill({award, editable, rankId}) {

    let classes = useStyles();
    let [expanded, setExpanded] = useState(false);
    let [editing, setEditing] = useState(false);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    let title;
    if (expanded) title = "";
    else {
        title = award.name.substring(0,45);
        if(award.name.length > 45) title += "...";
    }

    const onDelete = (awardId) => {
        if((awardId+"").indexOf("-") !== -1){
            const awardIds = awardId.split("-");
            for(let i = 0; i < award.amount; i++){
                let errors = 0;
                deleteElem(AWARDS +"/"+RANKINGS+"/"+awardIds[i])
                    .then(() => i === award.amount-1 && errors === 0 && enqueueSnackbar("Deleted", {variant: "success"}))
                    .catch(e => {
                        enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                        errors++
                    })
                    .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rankId}]))
            }
        } else {
            deleteElem(AWARDS+"/"+RANKINGS+"/"+awardId)
                .then(() => enqueueSnackbar("Deleted", {variant: "success"}))
                .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
                .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rankId}]));
        }
    }

    const onEditEnd = (newValues) => {

        delete newValues.positionType
        if(newValues.type === 0) newValues.type = "MONEY";
        if(newValues.type === 1) newValues.type = "POINTS";

        if((newValues.id+"").indexOf("-") !== -1){
            const awardIds = newValues.id.split("-");
            delete newValues.id
            for(let i = 0; i < award.amount; i++){
                let errors = 0;
                put(AWARDS +"/"+RANKINGS+"/"+awardIds[i],{body: newValues})
                    .then(() => i === award.amount-1 && errors === 0 && enqueueSnackbar("Saved", {variant: "success"}))
                    .catch(e => {
                        enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                        errors++
                    })
                    .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rankId}]))
            }
        } else {
            put(AWARDS+"/"+RANKINGS+"/"+newValues.id,{body: newValues})
                .then(() => enqueueSnackbar("Saved", {variant: "success"}))
                .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
                .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rankId}]));
        }
    }

    const onAdd = () => {
        delete award.id
        post(AWARDS +"/"+RANKINGS, {body: award})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e),{variant : "error"}))
            .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rankId}]))
    }

    function RemoveButton () {
        if(award.amount > 1){
            return (
                <IconButton
                    onClick={() => onDelete(award.id.substring(0,award.id.indexOf("-")))}
                    size="large">
                    <RemoveIcon className={classes.iconButton}/>
                </IconButton>
            );
        } else {
            return <ConfirmIconButton
                onConfirm={() => onDelete(award.id)}
                title="Conferma cancellazione premio"
                text={"Si desidera veramente cancellare il premio \"" + award.name +"\""}
            >
                <RemoveIcon className={classes.iconButton}/>
            </ConfirmIconButton>
        }
    }

    function WinnersTable(){
        const users = award.users ? award.users.split("~") : [];
        let rows = [];
        for(let i = 0; i<award.amount; i++){
            rows.push({id: i, user: users[i] || "Non assegnato"})
        }
        const columns = [
            {
                headerName: 'Premio',
                field: 'id',
                width: 200,
                valueGetter: (params) => params.value +1
            },
            {
                headerName: 'Vincitori',
                field: 'user',
                width: 200,
            },
        ]

        return <NTMXGrid columns={columns} rows={rows} disableToolbar density="compact" height={"18rem"} defaultPageSize={5}/>
    }

    return (
        <Accordion
            TransitionProps={{unmountOnExit: true}}
            classes={{root: classes.root, expanded: classes.expanded}}
            square
            onChange={(e, expanded) => setExpanded(expanded)}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon color={expanded ? "secondary" : "primary"}/>}
                aria-controls="panel1a-content"
                id={award.id}
            >
                <Grid alignItems={"center"} container direction="row">
                    <Grid xs={2}>
                        <Avatar src={award.imageUrl} alt="awardImage" className={classes.image}>
                            <EmojiEventsIcon fontSize={"large"}/>
                        </Avatar>
                    </Grid>
                    <Grid xs={3}>
                        <Typography color={"textPrimary"} className={classes.header}>{award.position ? award.position + "° posizione" : "Posizioni " + award.range}</Typography>
                    </Grid>
                    <Grid xs={7}>
                        <Typography color={"textPrimary"} className={classes.header}>{title}</Typography>
                    </Grid>
                </Grid>
            </AccordionSummary>
            <AccordionDetails>
                <Grid container alignItems={"center"}>
                    <TextItem value={award.name} className={classes.textItem} label={"Nome"} xs={12}/>
                    <TextItem value={award.description} className={classes.textItem} label={"Descrizione"}  xs={12}/>
                    <TextItem value={award.value} className={classes.textItem} label={"Valore in " + (award.type === "MONEY" ? "euro" : "punti")} xs={12}/>
                    {award.range && <Grid container xs={6}>
                            <Grid item xs={editable ? 8 : 12}>
                                <TextItem xs={12} md={12} value={award.amount} className={classes.textItem} label={"Numero premi"}/>
                            </Grid>
                            {
                                editable && <Grid container xs={4} alignContent={"flex-end"}>
                                    <RemoveButton/>
                                    <IconButton onClick={onAdd} size="large">
                                        <AddIcon className={classes.iconButton}/>
                                    </IconButton>
                                </Grid>
                            }
                        </Grid>
                    }
                    {
                        !editable && award.range && <Grid container xs={12} className={classes.winnersTable}>
                                <WinnersTable/>
                        </Grid>
                    }
                    {
                        !editable && award.position && <TextItem value={award.user || "Non assegnato"} className={classes.textItem} label={"Utente"} xs={12}/>
                    }
                    {
                        editable && <Grid item xs={12} style={{textAlign : "right"}}>
                            <IconButton onClick={() => setEditing(true)} size="large">
                                <EditIcon/>
                            </IconButton>
                            <ConfirmIconButton
                                onConfirm={() => onDelete(award.id)}
                                title="Conferma eliminazione"
                                text={"Si desidera veramente eliminare il premio \"" + award.name +"\""}
                            >
                                <DeleteIcon className={classes.iconButton}/>
                            </ConfirmIconButton>
                        </Grid>
                    }
                </Grid>
            </AccordionDetails>
            <RankAwardModal open={!!editing} onClose={() => setEditing(false)} onSubmit={onEditEnd} defaultValues={award} isEditing/>
        </Accordion>
    );
}
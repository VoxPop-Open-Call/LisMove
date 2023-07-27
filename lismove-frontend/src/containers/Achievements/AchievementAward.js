import Grid from "@mui/material/Grid";
import makeStyles from '@mui/styles/makeStyles';
import Button from "@mui/material/Button";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import AddNewButton from "../../components/buttons/AddNewButton";
import React,{useState} from "react";
import { Avatar, IconButton } from "@mui/material";
import {useGetAchievementsAwards} from "../../services/ContentManager";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import AchievementAwardModal from "../../components/modals/AchievementAwardModal";
import {ACHIEVEMENTS,AWARDS,getErrorMessage,post,put,RANKINGS} from "../../services/Client";
import {TextItem} from "../../components/TextItem";
import EditIcon from "@mui/icons-material/Edit";
import EmojiEventsIcon from "@mui/icons-material/EmojiEvents";


const useStyles = makeStyles(theme => ({
    backButton : {
        color : theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    container: {
        padding: "2rem",
        backgroundColor: theme.palette.primary.light,
        width: "auto"
    },
    textItem: {
        height: "100%",
    },
    image: {
        height: "4rem",
        width: "4rem",
        marginRight: theme.spacing(2),
    },
}));

export default function AchievementAward({ goBack, achievement}) {

    let classes = useStyles();
    const editable = achievement.startDate > Date.now();
    let {awards = [], status} = useGetAchievementsAwards(achievement.id);
    let [awardModal, setAwardModal] = useState(false);
    let [editing, setEditing] = useState(false);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    const createAward = (values) => {
        values.achievement = achievement.id
        enqueueSnackbar("Saving...", {variant: "info"});
        post(AWARDS +"/"+ACHIEVEMENTS, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e),{variant : "error"}))
            .finally(() => queryClient.invalidateQueries([AWARDS,{aid : achievement.id}]))
    }

    const onEditEnd = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(AWARDS+"/"+ACHIEVEMENTS+"/"+values.id,{body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([AWARDS,{aid : achievement.id}]));
    }

    return (
        <Grid container justifyContent="space-between">
            <Grid container justifyContent="space-between">
                <Button onClick={goBack} className={classes.backButton}>
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
            </Grid>
            <Grid container xs={12} className={classes.container} alignItems={"center"} justifyContent={"center"}>
                {awards.length === 0 && status !== "loading" && <AddNewButton text={"aggiungi premio"} onClick={() => setAwardModal(true)}/>}
                {awards.length !== 0 &&
                    <Grid container xs={12} md={6}>
                        <Grid xs={8}>
                            <Avatar src={awards[ 0 ].imageUrl} alt="awardImage" className={classes.image}>
                                <EmojiEventsIcon fontSize={"large"}/>
                            </Avatar>
                        </Grid>
                        {
                            editable && <Grid item xs={4} style={{textAlign : "right"}}>
                                <IconButton onClick={() => setEditing(true)} size="large">
                                    <EditIcon/>
                                </IconButton>
                            </Grid>
                        }
                        <TextItem value={awards[ 0 ].name} className={classes.textItem} label={"Nome"} xs={12} md={12}/>
                        <TextItem value={awards[ 0 ].description} className={classes.textItem} label={"Descrizione"} xs={12} md={12}/>
                        <TextItem value={awards[ 0 ].value} className={classes.textItem}
                                  label={"Valore in " + (awards[ 0 ].type === "MONEY" ? "euro" : "punti")} xs={12} md={12}/>
                    </Grid>
                }
            </Grid>
            <AchievementAwardModal open={!!editing} onClose={() => setEditing(false)} onSubmit={onEditEnd} defaultValues={awards[0]} isEditing/>
            <AchievementAwardModal open={!!awardModal} onClose={() => setAwardModal(false)} onSubmit={createAward}/>
        </Grid>
    );
}
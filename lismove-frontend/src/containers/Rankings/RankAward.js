import Grid from "@mui/material/Grid";
import makeStyles from '@mui/styles/makeStyles';
import Button from "@mui/material/Button";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import React,{useState} from "react";
import {AwardPill} from "./AwardPill";
import AddNewButton from "../../components/buttons/AddNewButton";
import  RankAwardModal from "../../components/modals/RankAwardModal";
import {AWARDS,getErrorMessage,post,RANKINGS} from "../../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import {useGetRankingAwards} from "../../services/ContentManager";

const useStyles = makeStyles(theme => ({
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    container: {
        padding: "2rem",
        backgroundColor: theme.palette.primary.light,
        width: "auto"
    },
    addAward: {
        backgroundColor: theme.palette.primary.light,
        width: "100%",
        height: "100%",
    }
}));

export default function RankAward({ goBack, rank}) {

    let classes = useStyles();
    const editable = rank.startDate > Date.now();
    let [addAward, setAddAward] = useState(false);
    let {awards = []} = useGetRankingAwards(rank.id);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    const createAward = (values) => {
        let amount = parseInt(values.amount) || 1;
        delete values.amount;
        values.ranking = rank.id;
        delete values.positionType;
        if(values.minPosition) values.range = values.minPosition + " - " + values.maxPosition
        delete values.minPosition
        delete values.maxPosition

        enqueueSnackbar("Saving...", {variant: "info"});
        for(let i = 0; i < amount; i++){
            let errors = 0;
            post(AWARDS +"/"+RANKINGS, {body: values})
                .then(() => i === amount-1 && errors === 0 && enqueueSnackbar("Saved", {variant: "success"}))
                .catch(e => {
                    enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                    errors++
                })
                .finally(() => queryClient.invalidateQueries([AWARDS,{rid : rank.id}]))
        }
    }

    const getGrouped = (m, o) => {
        let item = m.get(o.range || o.position);
        if (!item) return m.set(o.range || o.position, Object.assign({amount: 1}, o));
        item.amount++;
        item.id = item.id + "-" + o.id
        item.user = item.user + "~" + o.user
        return m;
    };

    let collapsedAwards = Array.from(awards.reduce(getGrouped, new Map).values());

    collapsedAwards.sort((a,b) => {
        const positionA = parseInt(a.position || a.range.substring(0, a.range.indexOf("-")-1))
        const positionB = parseInt(b.position || b.range.substring(0, b.range.indexOf("-")-1))
        return positionA - positionB;
    })

    return (
        <Grid container justifyContent="space-between">
            <Grid container justifyContent="space-between">
                <Button onClick={goBack} className={classes.backButton}>
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
                {editable && <AddNewButton text={"aggiungi premio"} onClick={() => setAddAward(true)}/>}
            </Grid>
            <Grid container xs={12} className={classes.container}>

                {
                    collapsedAwards.map((award, index) => <Grid item lg={6} xs={12} key={index}>
                        <AwardPill award={award} editable={editable} rankId={rank.id}/>
                    </Grid>)
                }

            </Grid>
            <RankAwardModal open={!!addAward} onClose={() => setAddAward(false)} onSubmit={createAward}/>
        </Grid>
    );
}
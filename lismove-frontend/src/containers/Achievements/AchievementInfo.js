import {useGetAchievement} from "../../services/ContentManager";
import makeStyles from '@mui/styles/makeStyles';
import { CircularProgress } from "@mui/material";
import React from "react";
import Grid from "@mui/material/Grid";
import Button from "@mui/material/Button";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import dayjs from "dayjs";
import NTMXGrid from "../../components/NTMXGrid";
import {TextItem} from "../../components/TextItem";
import RenderUserRedirect from "../../components/cellRender/RenderUserRedirect";

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
    }
}));

export default function AchievementInfo({goBack, achievementId}){

    let classes = useStyles();
    const {achievement = {}, status} = useGetAchievement(achievementId);

    const defaultColumns = [
        {
            headerName: 'Username',
            field: 'username',
            width: 200,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.user}/>
        },
        {
            headerName: 'Punti',
            field: 'score',
            width: 200
        },
    ];

    if(status === "loading" ) return <Grid container><CircularProgress/></Grid>;

    return <Grid container justifyContent="space-between">
        <Grid container justifyContent="space-between">
            <Button onClick={goBack} className={classes.backButton}>
                <ArrowBackIcon className={classes.backButtonIcon}/>
                Torna indietro
            </Button>
        </Grid>
        {achievement &&
            <>
                <Grid xs={12}>
                    <div className={classes.title}>{achievement.name}</div>
                </Grid>
                <TextItem value={dayjs(new Date(achievement.startDate)).format("DD/MM/YYYY")} label={"Inizio"} xs={12} md={6}/>
                <TextItem value={dayjs(new Date(achievement.endDate)).format("DD/MM/YYYY")} label={"FIne"} xs={12} md={6}/>
            </>
        }
        <NTMXGrid
            columns={defaultColumns}
            rows={achievement.users || []}
            getRowId={(row) => row && row.user}
        />
    </Grid>
}
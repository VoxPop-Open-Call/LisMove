import {useGetRank} from "../../services/ContentManager";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import Button from "@mui/material/Button";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import React from "react";
import { Avatar } from "@mui/material";
import NTMXGrid from "../../components/NTMXGrid";
import userImage from "../../images/top_bar-profilo-over.svg";
import dayjs from "dayjs";
import Box from "@mui/material/Box";

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

export default function RankInfo({goBack, rankId}){

    let classes = useStyles();
    const {rank = {}} = useGetRank(rankId)

    const defaultColumns = [
        {
            headerName: 'Posizione',
            field: 'position',
            width: 200
        },
        {
            headerName: 'Immagine',
            field: 'avatarUrl',
            width: 200,
            renderCell: (params) => <Avatar src={params.value || userImage} alt="userImage"/>
        },
        {
            headerName: 'Username',
            field: 'username',
            width: 200,
        },
        {
            headerName: 'Punti',
            field: 'points',
            width: 200
        },
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
                <div className={classes.title}>{rank.title}</div>
            </Grid>
            <Grid xs={6}>
                <div className={classes.text}>Inizio</div>
                <Box className={classes.box}>
                    {dayjs(new Date(rank.startDate)).format("DD/MM/YYYY")}
                </Box>
            </Grid>
            <Grid xs={6}>
                <div className={classes.text}>Fine</div>
                <Box className={classes.box}>
                    {dayjs(new Date(rank.endDate)).format("DD/MM/YYYY")}
                </Box>
            </Grid>
            <NTMXGrid
                columns={defaultColumns}
                rows={rank.rankingPositions || []}
                getRowId={(row) => rank && row.position}
            />
        </Grid>
    );
}

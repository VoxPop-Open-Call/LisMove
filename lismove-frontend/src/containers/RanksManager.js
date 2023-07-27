import Grid from "@mui/material/Grid";
import NTMXGrid,{timestampTypeToDate} from "../components/NTMXGrid";
import React,{useState} from "react";
import {Divider} from "@mui/material";
import StartIconButton from "../components/buttons/StartIconButton";
import CreateIcon from '@mui/icons-material/Create';
import CreateRankModal from "../components/modals/CreateRankModal";
import {getErrorMessage,ORGANIZATIONS,post,put,RANKINGS,RANKS} from "../services/Client";
import getUsefulValues from "../constants/rankingUsefulValues";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import getRankingFilter from "../constants/rankingFilter";
import {sessionType} from "../constants/sessionType";
import RenderCell from "../components/cellRender/RenderCell";
import dayjs from "dayjs";
import VisibilityIcon from "@mui/icons-material/Visibility";
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import RankAward from "./Rankings/RankAward";
import InfoRepetitionRankings from "../components/cellRender/InfoRepetitionRankings";
import RankInfo from "./Rankings/RankInfo";
import IconButton from "@mui/material/IconButton";

export default function RanksManager({ranks, national = false, organizationId, customField}){

    let [isCreating, setIsCreating] = useState(false);
    let [showInfo, setShowInfo] = useState(null);
    let [showAwards, setShowAwards] = useState(null);

    ranks = ranks.slice().sort((a , b) => a.startDate - b.startDate);
    const unexpiredRanks = ranks !== [] && ranks.filter((e) => e.endDate > Date.now());
    const expiredRanks = ranks !== [] && ranks.filter((e) => e.endDate <= Date.now());
    const usefulValues = getUsefulValues(national);
    const rankingFilter = getRankingFilter(customField);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    const today = dayjs(new Date()).format("YYYY-MM-DD");

    const getFilterValue = (params) => {
        if(params.row["filter"] !== null && params.row["filter"] !== undefined){
            if(params.row["filter"] === 0) {
                return sessionType.find(st => st.id == params.value).name       //usa == e non ==== perchè uno dei due puo' essere una stringa e l'altro un numero
            }
            if(params.row["filter"] === 1) {
                return params.value
            }
            if(params.row["filter"] === 2) {
                return params.value === "M" ? "Maschio" : "Femmina";
            }
        }
        return "";
    }

    const defaultColumns = [
        {
            headerName: 'Titolo',
            field: 'title',
            width: 200,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit} required/>
        },
        {
            headerName: 'Valore',
            field: 'value',
            width: 200,
            valueGetter: (params) => usefulValues.find(elem => elem.id === params.value).name,
        },
        {
            headerName: 'Filtro',
            field: 'filter',
            width: 200,
            valueGetter: (params) => (params.value !== null && params.value !== undefined ) ? rankingFilter.find(elem => elem.id === params.value).name : "",
        },
        {
            headerName: 'Valore filtro',
            field: 'filterValue',
            width: 200,
            valueGetter: (params) => getFilterValue(params)
        },
        {
            headerName: 'Data inizio',
            field: 'startDate',
            width: 200,
            ...timestampTypeToDate
        },
        {
            headerName: 'Data fine',
            field: 'endDate',
            width: 200,
            renderCell: (params) => <RenderCell params = {params} saveEdit={params.row.endDate > Date.now() && saveEdit} required type="date" min={today}/>,
        },
        {
            headerName: 'Ripetizioni',
            field: 'repeatNum',
            width: 150,
            renderCell: (params) => <InfoRepetitionRankings params={params}/>
        },
        {
            headerName: ' ',
            field: "info",
            width: 110,
            renderCell: (params) =>  <>
                <IconButton onClick={() => setShowInfo(params.row)} size="large"> <VisibilityIcon/> </IconButton>
                <IconButton onClick={() => setShowAwards(params.row)} size="large"> <EmojiEventsIcon/> </IconButton>
            </>
        }
    ];

    const createRank = (values) => {
        if(!national) {
            values.organization = parseInt(organizationId);
        }
        const key = national ? ["National", RANKS] : [ORGANIZATIONS, {id: organizationId}, RANKINGS];
        enqueueSnackbar("Saving...", {variant: "info"});
        post(RANKINGS, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(key));
    }

    const saveEdit = (id, field, newValue) => {
        const key = national ? ["National", RANKS] : [ORGANIZATIONS, {id: organizationId}, RANKS];
        enqueueSnackbar("Saving...", {variant: "info"});
        put(RANKINGS, {body: {[field]: newValue}, elem: id})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(key));
    }

    if(showInfo) return <RankInfo goBack={() => setShowInfo(null)} rankId={showInfo.id}/>
    if(showAwards) return <RankAward goBack={() => setShowAwards(null)} rank={showAwards}/>

    return <Grid>
        <NTMXGrid
            columns={defaultColumns}
            rows={unexpiredRanks || []}
            title="Classifiche attive"
            rightButton={<Grid container justifyContent={"flex-end"}>
                <StartIconButton onClick={() => setIsCreating(true)} title="Crea classifica" startIcon={<CreateIcon/>}/>
            </Grid>}
        />
        <Divider/>
        <NTMXGrid
            columns={defaultColumns}
            rows={expiredRanks || []}
            title="Classifiche terminate"
        />
        <CreateRankModal open={!!isCreating} onClose={() => setIsCreating(false)} onSubmit={createRank}
                         usefulValues={usefulValues} rankingFilter={rankingFilter}/>
    </Grid>

}

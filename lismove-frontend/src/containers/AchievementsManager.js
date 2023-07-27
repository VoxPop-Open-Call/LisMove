import NTMXGrid,{timestampTypeToDate} from "../components/NTMXGrid";
import getUsefulValues from "../constants/rankingUsefulValues";
import getRankingFilter from "../constants/rankingFilter";
import {sessionType} from "../constants/sessionType";
import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import {Divider} from "@mui/material";
import CreateAchievementsModal from "../components/modals/CreateAchievementsModal";
import {ACHIEVEMENTS,getErrorMessage,ORGANIZATIONS,post,put} from "../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import StartIconButton from "../components/buttons/StartIconButton";
import CreateIcon from "@mui/icons-material/Create";
import {useGridApiRef} from "@mui/x-data-grid-pro";
import {getTableColumns} from "../services/ContentManager";
import dayjs from "dayjs";
import RenderCell from "../components/cellRender/RenderCell";
import RenderCellAvatar from "../components/cellRender/RenderCellAvatar";
import IconButton from "@mui/material/IconButton";
import VisibilityIcon from "@mui/icons-material/Visibility";
import EmojiEventsIcon from "@mui/icons-material/EmojiEvents";
import AchievementInfo from "./Achievements/AchievementInfo";
import AchievementAward from "./Achievements/AchievementAward";
const duration = require('dayjs/plugin/duration')
dayjs.extend(duration)
const storageKey = "achievements-table-columns"
const warningAchievementAlreadyStarted = "Modifica rischiosa, coppa già iniziata!"
const infoLogoDimension = "L'immagine deve essere quadrata, da 400px x 400px in poi"

export default function AchievementsManager({achievements, national = false, organizationId, customField}){

    achievements = achievements.slice().sort((a , b) => a.startDate - b.startDate);
    const unexpiredAchievements = achievements !== [] && achievements.filter((e) => e.endDate > Date.now());
    const expiredAchievements = achievements !== [] && achievements.filter((e) => e.endDate <= Date.now());
    const usefulValues = getUsefulValues(national);
    const achievementsFilter = getRankingFilter(customField);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let [columns, setColumns] = useState([])
    let [isCreating, setIsCreating] = useState(false);
    let [showInfo, setShowInfo] = useState(null);
    let [showAwards, setShowAwards] = useState(null);
    const gridApiRef = useGridApiRef()

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

    const createAchievement = (values) => {
        if(!national) {
            values.organization = parseInt(organizationId);
        }
        const key = national ? ["National", ACHIEVEMENTS] :[ORGANIZATIONS, {id: organizationId}, ACHIEVEMENTS];
        enqueueSnackbar("Saving...", {variant: "info"});
        post(ACHIEVEMENTS, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(key));
    }

    const defaultColumns = [
        {
            headerName: 'Logo',
            field: 'logo',
            width: 130,
            renderCell: (params) => <RenderCellAvatar params={params} saveEdit={params.row.endDate > Date.now() && saveEdit}
                                                      folder={"organizations/achievements"} prefix={organizationId} label={"Trascina l'immagine"}
                                                      infoMessage={infoLogoDimension} warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>
        },
        {
            headerName: 'Nome',
            field: 'name',
            width: 200,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit}
                                                warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>
        },
        {
            headerName: 'Data inizio',
            field: 'startDate',
            width: 200,
            hide: true,
            renderCell: ((params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit}
                                                 type="date" max={dayjs(params.row.endDate).format("YYYY-MM-DD")}
                                                 warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>),
            ...timestampTypeToDate
        },
        {
            headerName: 'Data fine',
            field: 'endDate',
            width: 200,
            hide: true,
            renderCell: ((params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit}
                                                 type="date" min={dayjs(params.row.startDate).format("YYYY-MM-DD")}
                                                 warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>),
            ...timestampTypeToDate
        },
        {
            headerName: 'Durata in Giorni',
            field: 'duration',
            width: 200,
            renderCell: ((params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit} type="number"
                                                 decimals={0} warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>),
        },
        {
            headerName: 'Valore',
            field: 'value',
            width: 200,
            valueGetter: (params) => usefulValues.find(elem => elem.id === params.value) ? usefulValues.find(elem => elem.id === params.value).name : "",
            renderCell: (params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit} type="select" options={usefulValues}
                                                warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>
        },
        {
            headerName: 'Target',
            field: 'target',
            width: 200,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit} type="number"
                                                warningMessage={params.row.startDate < Date.now() && warningAchievementAlreadyStarted}/>
        },
        {
            headerName: 'Filtro',
            field: 'filter',
            width: 200,
            valueGetter: (params) => (params.value !== null && params.value !== undefined ) ? achievementsFilter.find(elem => elem.id === params.value).name : "",
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {params.row.endDate > Date.now() && saveEdit} type="select" options={[{key:'', value:''}, ...achievementsFilter]}/>
        },
        {
            headerName: 'Valore filtro',
            field: 'filterValue',
            width: 200,
            valueGetter: (params) => getFilterValue(params)
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
    ]

    const saveEdit = (id, field, value) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        const key = national ? ["National", ACHIEVEMENTS] :[ORGANIZATIONS, {id: organizationId}, ACHIEVEMENTS];
        if(field === "startDate" || field === "endDate") value = new Date(value).getTime()
        put(ACHIEVEMENTS, {body: {[field]: value}, elem: id})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(key));
    }

    let cachedRef;
    useEffect(() => {
        if(gridApiRef.current) {
            cachedRef = gridApiRef.current;
            setColumns(getTableColumns(storageKey, defaultColumns));
        }
        return () => localStorage.setItem(storageKey, JSON.stringify(cachedRef.getAllColumns().map(c => {return {headerName:c.headerName, hide:c.hide}})))
    }, [gridApiRef])

    if(showInfo) return <AchievementInfo goBack={() => setShowInfo(null)} achievementId={showInfo.id}/>
    if(showAwards) return <AchievementAward goBack={() => setShowAwards(null)} achievement={showAwards}/>

    return <Grid>
        <NTMXGrid
            columns={columns}
            rows={unexpiredAchievements || []}
            title="Coppe attive"
            apiRef={gridApiRef}
            rightButton={<Grid container justifyContent={"flex-end"}>
                <StartIconButton onClick={() => setIsCreating(true)} title="Crea coppa" startIcon={<CreateIcon/>}/>
            </Grid>}
        />
        <Divider/>
        <NTMXGrid
            columns={defaultColumns}
            rows={expiredAchievements || []}
            title="Coppe terminate"
        />
        <CreateAchievementsModal open={!!isCreating} onClose={() => setIsCreating(false)} onSubmit={createAchievement}
                                 usefulValues={usefulValues} achievementsFilter={achievementsFilter} organizationId={organizationId}
                                 infoLogoDimension={infoLogoDimension}/>
    </Grid>
}
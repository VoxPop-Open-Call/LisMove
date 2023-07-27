import {useHistory,useParams} from "react-router-dom";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import { ButtonGroup, Paper } from "@mui/material";
import {TextItem} from "../../components/TextItem";
import Button from "@mui/material/Button";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import MapSession from "../../components/mapview/MapSession";
import {get,getErrorMessage,put,SESSIONS,USERS} from "../../services/Client";
import React,{useEffect, useState} from "react";
import RenderCell from "../../components/cellRender/RenderCell";
import RenderBoolean from "../../components/cellRender/RenderBoolean";
import NTMXGrid,{timestampTypeToDateTime} from "../../components/NTMXGrid";
import StartIconButton from "../../components/buttons/StartIconButton";
import ReplayIcon from '@mui/icons-material/Replay';
import PartialModal from "../../components/modals/PartialModal";
import {useSnackbar} from "notistack";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import {useGetSessionInfo} from "../../services/ContentManager";
import dayjs from "dayjs";
import {partialType,partialTypeName} from "../../constants/partialType";
import FilterListIcon from "@mui/icons-material/FilterList";
import ListIcon from "@mui/icons-material/List";
import RoomIcon from '@mui/icons-material/Room';

const useStyles = makeStyles(theme => ({
    page: {
        padding: theme.spacing(2)
    },
    container: {
        marginBottom: theme.spacing(2)
    },
    mapContainer: {
        width: "100%",
        height: "60vh",
    },
    box: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        borderBottom: `${theme.palette.secondary.main} 1px solid`,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        flexGrow: 1,
    },
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    button: {
        marginBottom: theme.spacing(1),
        marginTop: theme.spacing(1),
    }
}));

const partialStatus = [
    {
        id: 0,
        name: "Valido"
    },
    {
        id: 1,
        name: "Errore generale"
    },
    {
        id: 2,
        name: "Distanza non accurata"
    },
    {
        id: 3,
        name: "Velocità media non valida"
    },
]

const defaultColumns = [
    {
        headerName: 'Ora',
        field: 'timestamp',
        width: 240,
        ...timestampTypeToDateTime
    },
    {
        headerName: 'Latitudine',
        field: 'latitude',
        width: 280,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="°" inputType="number"/>,
        hide: true,
    },
    {
        headerName: 'Longitudine',
        field: 'longitude',
        width: 280,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="°" inputType="number"/>,
        hide: true,
    },
    {
        headerName: 'Altitudine',
        field: 'altitude',
        width: 280,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="°" inputType="number"/>,
        hide: true,
    },
    {
        headerName: 'Distanza Sensore',
        field: 'sensorDistance',
        width: 170,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="km" inputType="number"/>
    },
    {
        headerName: 'Distanza GPS',
        field: 'gpsDistance',
        width: 170,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="km" inputType="number"/>
    },
    {
        headerName: 'Distanza GMaps',
        field: 'gmapsDistance',
        width: 170,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} measureUnit="km" inputType="number"/>,
        hide: true,
    },
    {
        headerName: 'Giri di ruota',
        field: 'deltaRevs',
        width: 160,
        type: "number",
        headerAlign: 'left',
        align: "left",
        renderCell: (params) => <RenderCell params = {params} inputType="number"/>,
        hide: true,
    },
    {
        headerName: 'Urbana',
        field: 'urban',
        width: 150,
        renderCell: (params) => <RenderBoolean params={params}/>,
        hide: true,
    },
    {
        headerName: 'Valida',
        field: 'valid',
        width: 150,
        renderCell: (params) => <RenderBoolean params={params}/>
    },
    {
        headerName: 'Tipo',
        field: 'type',
        width: 280,
        renderCell: params => partialTypeName[params.value]
    },
    {
        headerName: 'Stato',
        field: 'status',
        width: 280,
        valueGetter: (params) => params.value !== undefined && partialStatus.find(s => s.id === params.value).name,
    },
    {
        headerName: 'Extra',
        field: 'extra',
        width: 280,
        hide: true
    }
];

export default function SessionInfo(){

    let classes = useStyles();
    let {id} = useParams();
    let history = useHistory();
    let {session} = useGetSessionInfo(id)
    let [values, setValues] = useState();
    let [homePoint, setHomePoint] = useState();
    let [workPoints, setWorkPoints] = useState();
    let [username, setUsername] = useState();
    let [isChangingParameters, setIsChangingParameters] = useState(false);
    let [polylineType, setPolylineType] = useState("polyline");
    let [showDebug, setShowDebug] = useState(false);
    let [isShowingPartialPoints, setIsShowingPartialPoints] = useState(true);

    const {enqueueSnackbar} = useSnackbar();

    const getFilteredPartials = () => {
        if(session && session.partials) {
            if(showDebug) return(session.partials)
            else return(session.partials.filter(p => (p.type < partialType.SESSION)))
        }
    }

    useEffect(() => {
        setValues(session)
        session && get(`users/${session.uid}/home-address`, {params: {activeAt: session.startTime}}).then(r => {
            setHomePoint({
                lat : r.latitude,
                lng : r.longitude,
                tolerance: r.tolerance*1000
            })
        })
        session && get(`users/${session.uid}/work-addresses`, {params: {activeAt: session.startTime}}).then(r => {
            let newValues = [];
            r.map(wa => newValues.push({lat: wa.latitude, lng: wa.longitude, tolerance: wa.tolerance*1000}))
            setWorkPoints(newValues)
        })
        session && get(USERS ,{elem: session.uid}).then(u => setUsername(u.username))
    }, [session])

    let gyroDistance = values && values.gyroDistance ? values.gyroDistance + " Km" : "";
    let gpsDistance = values && values.gpsDistance ? values.gpsDistance + " Km" : "";
    let gmapsDistance = values && values.gmapsDistance ? values.gmapsDistance + " Km" : "";

    const reloadPartials = (parameters) => {
        let newParameters = {...parameters}
        newParameters.partialQty = parameters.partialQty /100;
        newParameters.partialDeviation = parameters.partialDeviation /100;
        enqueueSnackbar("Reloading...", {variant: "info"});
        put(SESSIONS, {params: newParameters, elem: id+"/validate"})
            .then(({data}) => {
                setValues(data);
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
    }

    return (
        <Paper className={classes.page}>

            <Grid container justifyContent="space-between">
                <Grid item>
                    <Button onClick={() => history.replace("/sessions")} className={classes.backButton}>
                        <ArrowBackIcon className={classes.backButtonIcon}/>
                        Torna indietro
                    </Button>
                </Grid>
                <Grid item>
                    <StartIconButton title="Cambia parametri" onClick={() => setIsChangingParameters(true)} startIcon={<ReplayIcon/>}/>
                </Grid>
            </Grid>

            <Grid container alignItems={"flex-end"} className={classes.container}>
                <TextItem value={values && values.id} label="ID Sessione"/>
                <TextItem value={username} label="Username"/>
                <TextItem value={values && dayjs(new Date(values.startTime)).format("HH:mm:ss DD/MM/YYYY")} label="Data Inizio"/>
                <TextItem value={values && dayjs(new Date(values.endTime)).format("HH:mm:ss DD/MM/YYYY")} label="Data Fine"/>
                <TextItem value={gyroDistance} label="Distanza Giroscopio"/>
                <TextItem value={gpsDistance} label="Distanza GPS"/>
                <TextItem value={gmapsDistance} label="Distanza Google Maps"/>
                <TextItem value={values && <FiberManualRecordIcon style={{color: values.valid ? "#43ff00" : "#ff0000"}}/>} label="Valida"/>
                <TextItem value={values && <FiberManualRecordIcon style={{color: values.homeWorkPath ? "#43ff00" : "#ff0000"}}/>} label="Casa -> Scuola/Lavoro"/>
                <TextItem value={values && <FiberManualRecordIcon style={{color: values.certificated ? "#43ff00" : "#ff0000"}}/>} label="Certificata"/>
            </Grid>

            <Grid container justifyContent="space-between">
                <Grid item xs={3}>
                {values && values.partials && <>
                    {isShowingPartialPoints ?
                        <StartIconButton title="Nascondi parziali" onClick={() => setIsShowingPartialPoints(false)}
                                         startIcon={<RoomIcon/>}/>
                        :
                        <StartIconButton title="Mostra parziali" onClick={() => setIsShowingPartialPoints(true)}
                                         startIcon={<RoomIcon/>}/>}
                </>}
                </Grid>

                <ButtonGroup color="secondary" aria-label="outlined secondary button group">
                    <Button variant="outlined" color={polylineType === "polyline" ? "secondary" : "primary"}
                            className={classes.button} onClick={() => setPolylineType("polyline")}>
                        Percorso Visualizzato dall'utente
                    </Button>
                    <Button variant="outlined" color={polylineType === "rawPolyline" ? "secondary" : "primary"}
                            className={classes.button} onClick={() => setPolylineType("rawPolyline")}>
                        Collegamento diretto
                    </Button>
                    <Button variant="outlined" color={polylineType === "gmapsPolyline" ? "secondary" : "primary"}
                            className={classes.button} onClick={() => setPolylineType("gmapsPolyline")}>
                        Percorso GMaps
                    </Button>
                </ButtonGroup>
                <Grid item xs={3}/>
            </Grid>

            <Grid container className={classes.mapContainer}>
                {
                    values &&
                    <MapSession polylines={{polyline: values.polyline, rowPolyline: values.rowPolyline, gmapsPolyline: values.gmapsPolyline}}
                                zoom={15} points={(values.partials || []).filter(p => p.type < partialType.SESSION || p.type === partialType.SKIPPED)}
                                polylineType={polylineType} isShowingPartialPoints={isShowingPartialPoints}
                                homePoint={homePoint} workPoints={workPoints}
                                center={session.partials && session.partials.find(p => (p.type < partialType.START))}/>
                }
            </Grid>

            {session && session.partials &&
                <NTMXGrid
                    columns={defaultColumns}
                    rows={getFilteredPartials()}
                    title="Parziali"
                    getRowId={(row) => row.timestamp}
                    rightButton={<Grid container justifyContent={"flex-end"}>
                        <StartIconButton
                            onClick={() => setShowDebug(!showDebug)}
                            title={showDebug ? "Nascondi Debug" : "Mostra Debug"}
                            startIcon={showDebug ? <FilterListIcon/> : <ListIcon/>}/>
                    </Grid>}
                />
            }

            <PartialModal open={!!isChangingParameters} onClose={() => setIsChangingParameters(false)} onSubmit={reloadPartials}/>

        </Paper>
    );
}

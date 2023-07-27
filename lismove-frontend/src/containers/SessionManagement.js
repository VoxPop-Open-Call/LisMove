import {IconButton,Paper} from "@mui/material";
import NTMXGrid, {
    timestampToDuration,
    timestampTypeToDateTime
} from "../components/NTMXGrid";
import {getTableColumns,useGetLatestSessions} from "../services/ContentManager";
import {getErrorMessage,put,SESSIONPOINTS,SESSIONS,USERS} from "../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import RenderCell from "../components/cellRender/RenderCell";
import React, {useEffect, useState} from "react";
import RenderBoolean from "../components/cellRender/RenderBoolean";
import VisibilityIcon from '@mui/icons-material/Visibility';
import {Link,useHistory} from "react-router-dom";
import {useGridApiRef} from "@mui/x-data-grid-pro";
import dayjs from "dayjs";
import RenderUserRedirect from "../components/cellRender/RenderUserRedirect";
import renderCellExpand from "../components/cellRender/renderCellExpand";
import Grid from "@mui/material/Grid";
import StartIconButton from "../components/buttons/StartIconButton";
import FilterListIcon from '@mui/icons-material/FilterList';
import ListIcon from '@mui/icons-material/List';

const duration = require('dayjs/plugin/duration')
dayjs.extend(duration)

const storageKey = "session-table-columns"

const sessionType = [
    {
        id: 0,
        name: "Bicicletta muscolare"
    },
    {
        id: 1,
        name: "Bicicletta elettrica"
    },
    {
        id: 2,
        name: "Monopattino"
    },
    {
        id: 3,
        name: "Piedi"
    },
    {
        id: 4,
        name: "Carpooling"
    },
]

export function SessionManagement() {

    let {latestSessions:sessions} = useGetLatestSessions(2500);
    let {latestSessions} = useGetLatestSessions(200);
    let [localSessions, setLocalSessions] = useState([])
    let [toCheckFilter, setToCheckFilter] = useState(false)
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let history = useHistory();
    let [columns, setColumns] = useState([])
    const gridApiRef = useGridApiRef()

    useEffect(() => {
        if(toCheckFilter) setLocalSessions(getFilteredSessions(sessions))
        else setLocalSessions(sessions)
    }, [sessions])

    useEffect(() => {
        if(!sessions) {
            if(toCheckFilter) setLocalSessions(getFilteredSessions(latestSessions))
            else setLocalSessions(latestSessions)
        }
    }, [latestSessions])

    useEffect(() => {
        if(toCheckFilter){
            setLocalSessions(getFilteredSessions(localSessions))
        } else {
            if(sessions) setLocalSessions(sessions)
            else if(latestSessions) setLocalSessions(latestSessions)
        }
    }, [toCheckFilter])

    const getFilteredSessions = (sessions) => {
        return sessions ? sessions.filter(s => (s.gyroDistance === 0 && s.gpsDistance > 1 && s.certificated === true) || (s.gyroDistance > s.gpsDistance) || (s.verificationRequired === true)) : []
    }

    const defaultColumns = [
        {
            headerName: 'Session UUID',
            field: 'id',
            width: 310,
            hide:true
        },
        {
            headerName: 'User UID',
            field: 'uid',
            width: 220,
            hide: true,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
        },
        {
            headerName: 'Tipo Sessione',
            field: 'type',
            width: 280,
            hide: true,
            valueGetter: (params) => params.value !== undefined ? sessionType.find(s => s.id === params.value).name : "-",
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession} type={"select"} options={sessionType}/>
        },
        {
            headerName: 'Email',
            field: 'email',
            width: 220,
            hide: true
        },
        {
            headerName: 'Telefono',
            field: 'phoneNumber',
            width: 170,
            hide: true,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {(id, field, newValue) => saveEditUser(params.row.uid, field, newValue)}/>
        },
        {
            headerName: 'Username',
            field: 'username',
            width: 170,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
        },
        {
            headerName: 'Nome',
            field: 'firstName',
            width: 130,
            hide: true
        },
        {
            headerName: "Cognome",
            field: "lastName",
            width: 130,
            hide: true
        },
        {
            headerName: 'Rank Totale',
            field: 'totalRank',
            width: 180,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true
        },
        {
            headerName: 'Rank Attuale',
            field: 'currentRank',
            width: 180,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true
        },
        {
            headerName: 'Tipo Bicicletta',
            field: 'bikeType',
            width: 280,
            hide: true,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession}/>
        },
        {
            headerName: 'Diametro Ruota',
            field: 'wheelDiameter',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession} type="number" decimals={2}/>
        },
        {
            headerName: 'Descrizione',
            field: 'description',
            width: 280,
            hide: true,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession}/>
        },
        {
            headerName: 'Valida',
            field: 'valid',
            width: 120,
            valueGetter: params => params.value ? "true" : "false",
            renderCell: (params) => <RenderBoolean params={params} /*saveEdit = {saveEditSession}*//>
        },
        {
            headerName: 'Certificata',
            field: 'certificated',
            width: 120,
            valueGetter: params => params.value ? "true" : "false",
            renderCell: (params) => <RenderBoolean params={params} /*saveEdit = {saveEditSession}*//>
        },
        {
            headerName: 'Data Inizio',
            field: 'startTimeDate',
            valueGetter: (params) => params.row['startTime'] ? dayjs(new Date(params.row['startTime'])).format("DD/MM/YYYY") : "",
            width: 180,
            // ...timestampTypeToDate
        },
        {
            headerName: 'Ora Inizio',
            field: 'startTimeTime',
            valueGetter: (params) => params.row['startTime'] ? dayjs(new Date(params.row['startTime'])).format("HH:mm:ss") : "",
            width: 180,
            // ...timestampTypeToTime
        },
        {
            headerName: 'Data Fine',
            field: 'endTimeDate',
            valueGetter: (params) => params.row['endTime'] ? dayjs(new Date(params.row['endTime'])).format("DD/MM/YYYY") : "",
            width: 180,
            // ...timestampTypeToDate
        },
        {
            headerName: 'Ora Fine',
            field: 'endTimeTime',
            valueGetter: (params) => params.row['endTime'] ? dayjs(new Date(params.row['endTime'])).format("HH:mm:ss") : "",
            width: 180,
        },
        {
            headerName: 'Durata',
            field: 'duration',
            width: 180,
            ...timestampToDuration
        },
        {
            headerName: 'Distanza Gyro',
            field: 'gyroDistance',
            width: 200,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => <RenderCell params = {params} measureUnit="km" type="number"/>
        },
        {
            headerName: 'Distanza GPS',
            field: 'gpsDistance',
            width: 200,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => <RenderCell params = {params} measureUnit="km" type="number"/>
        },
        {
            headerName: 'Distanza GoogleMaps',
            field: 'gmapsDistance',
            width: 200,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => <RenderCell params = {params} measureUnit="km" type="number"/>
        },
        {
            headerName: 'Punti Nazionali',
            field: 'nationalPoints',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession} type="number" decimals={0}/>
        },
        {
            headerName: 'Punti Iniziativa',
            field: 'sessionPoints',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            valueGetter: params => {
                if(params.row.sessionPoints){
                    let values = []
                    params.row.sessionPoints.map((sp) => {
                        values.push(/*{key: sp.organizationTitle, value: parseInt(sp.points), id: sp.id, field: "points",
                            type: "number", showValue : */sp.organizationTitle + ": " + sp.points/*}*/)
                    })
                    return values
                }
                else return ""
            },
            renderCell: params => renderCellExpand(params/*, saveEditSessionPoints*/)
        },
        {
            headerName: 'Euro',
            field: 'euro',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            valueGetter: params => {
                if(params.row.sessionPoints){
                    let values = []
                    params.row.sessionPoints.map((sp) => {
                        let value = sp.euro ? parseFloat(sp.euro).toFixed(2) : " - "
                        values.push(/*{key: sp.organizationTitle, value: value: parseFloat(sp.euro), id: sp.id,
                            startAdornment: "€", type: "number", field: "euro",
                            showValue: */sp.organizationTitle + ": " + value + "€"/*}*/)
                    })
                    return values
                }
                else return ""
            },
            renderCell: params => renderCellExpand(params/*, saveEditSessionPoints*/)
        },
        {
            headerName: 'Co2',
            field: 'co2',
            width: 120,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession} measureUnit="g" type="number" decimals={2}/>
        },
        {
            headerName: 'Km Iniziativa',
            field: 'initiativeKm',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            valueGetter: params => {
                if(params.row.sessionPoints){
                    let values = []
                    params.row.sessionPoints.map((sp) => {
                        let value = parseFloat(sp.distance).toFixed(2)
                        values.push(/*{key: sp.organizationTitle, value: value, id: sp.id,
                            startAdornment: "km", type: "number", field: "distance",
                            showValue: */sp.organizationTitle + ": " + value + "km"/*}*/)
                    })
                    return values
                }
                else return ""
            },
            renderCell: params => renderCellExpand(params/*, saveEditSessionPoints*/)
        },
        {
            headerName: 'Km Nazionali',
            field: 'nationalKm',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            renderCell: (params) => <RenderCell params = {params} /*saveEdit = {saveEditSession}*/ measureUnit="km" type="number"/>
        },
        {
            headerName: 'Percorso casa/lavoro',
            field: 'homeWorkPath',
            width: 280,
            hide: true,
            valueGetter: params => params.value ? "true" : "false",
            renderCell: (params) => <RenderBoolean params={params} /*saveEdit={saveEditSession}*//>
        },
        {
            headerName: 'Indirizzo Casa',
            field: 'homeAddress',
            width: 250,
            hide: true,
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEdit}/>
        },
        {
            field: 'homeAddressCoordinates',
            headerName: 'Coordinate Casa',
            width: 250,
            valueGetter: getHomeCoordinates,
            hide: true,
        },
        {
            headerName: 'Indirizzo Lavoro',
            field: 'workAddress',
            width: 250,
            hide: true,
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEdit}/>
        },
        {
            field: 'workAddressCoordinates',
            headerName: 'Coordinate Lavoro',
            width: 250,
            valueGetter: getWorkCoordinates,
            hide: true,
        },
        {
            headerName: 'Moltiplicatore',
            field: 'initiativeMultiplier',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            hide: true,
            valueGetter: params => {
                if(params.row.sessionPoints){
                    let values = []
                    params.row.sessionPoints.map((sp) => {
                        values.push(/*{key: sp.organizationTitle, value: parseFloat(sp.multiplier),
                            startAdornment: "x", type: "number", id: sp.id, field: "multiplier",
                            showValue: */sp.organizationTitle + ": x" + sp.multiplier/*}*/)
                    })
                    return values
                }
                else return ""
            },
            renderCell: params => renderCellExpand(params/*, saveEditSessionPoints*/)
            //renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEditSession} type="number"/>
        },
        {
            headerName : "Da revisionare",  //null: non da revisionare, true: da revisionare, false: già revisionata
            field : "verificationRequired",
            width : 250,
            hide: true,
            valueGetter: params => {
                if(params.value !== null && params.value !== undefined) return params.value ? "Da revisionare" : "Già revisionato"
                else return ""
            }
        },
        {
            headerName : "Note",
            field : "verificationRequiredNote",
            width : 250,
            hide: true
        },
        {
            headerName: 'Batteria Sensore Partenza',
            field: 'startBattery',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => params.value ? params.value + "%" : "",
            hide: true
        },
        {
            headerName: "Batteria Sensore Arrivo",
            field: 'endBattery',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => params.value ? params.value + "%" : "",
            hide: true
        },
        {
            headerName: 'Batteria Telefono Partenza',
            field: 'phoneStartBattery',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => params.value ? params.value + "%" : "",
            hide: true
        },
        {
            headerName: "Batteria Telefono Arrivo",
            field: 'phoneEndBattery',
            width: 280,
            type: "number",
            headerAlign: 'left',
            align: "left",
            renderCell: (params) => params.value ? params.value + "%" : "",
            hide: true
        },
        {
            headerName : "Sensore",
            field : "sensor",
            width : 250,
            hide: true
        },
        {
            headerName : "Nome sensore",
            field : "sensorName",
            width : 250,
            hide: true
        },
        {
            headerName : "Firmware",
            field : "firmware",
            width : 250,
            hide: true
        },
        {
            headerName : "Versione app",
            field : "appVersion",
            width : 250,
            hide: true
        },
        {
            headerName : "Piattaforma",
            field : "platform",
            width : 250,
            hide: true
        },
        {
            headerName : "Modello dispositivo",
            field : "phoneModel",
            width : 250,
            hide: true
        },
        {
            headerName : "Invio Grifo",
            field : "forwardedAt",
            width : 250,
            hide: true,
            ...timestampTypeToDateTime
        },
        {
            headerName: "Dettagli session",
            field: "polyline",
            width: 170,
            sortable: false,
            disableColumnMenu: true,
            resizable: false,
            renderCell: (params) =>  params.value && <Link to={"/sessions/" + params.row.id} target="_blank"><IconButton size="large"><VisibilityIcon/></IconButton></Link>
        }
    ];

    let cachedRef;
    useEffect(() => {
        if(gridApiRef.current) {
            cachedRef = gridApiRef.current;
            setColumns(getTableColumns(storageKey, defaultColumns));
        }
        return () => localStorage.setItem(storageKey, JSON.stringify(cachedRef.getAllColumns().map(c => {return {headerName:c.headerName, hide:c.hide}})))
    }, [gridApiRef])

    const saveEditSessionPoints = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        let savedShown = false
        let errorShown = false
        values.map((v,i) => {
            put(SESSIONPOINTS, {body: v.value, elem: v.id})
                .then(() => {
                    if(!savedShown){
                        enqueueSnackbar("Saved",{variant : "success"})
                        savedShown = true
                    }
                })
                .catch(e => {
                    if(!errorShown) {
                        enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                        errorShown = true
                    }
                })
                .finally(() => i+1 === values.length && queryClient.invalidateQueries(SESSIONS));
        })
    }

    const saveEditSession = (id, field, value) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(SESSIONS, {body: {[field]: value}, elem: id})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
                let newSessions = localSessions
                newSessions.find(s => s.id === id)[field] = value
                setLocalSessions(newSessions)
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(SESSIONS));
    }

    const saveEditUser = (uid, field, value) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(USERS, {body: {[field]: value}, elem: uid})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(SESSIONS));
    }

    function getHomeCoordinates(params) {
        if(!getValue(params, 'homeAddressLng') && !getValue(params, 'homeAddressLat')) return ""
        return `${getValue(params, 'homeAddressLat') || ''} - ${
            getValue(params, 'homeAddressLng') || ''
        }`;
    }

    function getWorkCoordinates(params) {
        if(!getValue(params, 'workAddressLng') && !getValue(params, 'workAddressLat')) return ""
        return `${getValue(params, 'workAddressLat') || ''} - ${
            getValue(params, 'workAddressLng') || ''
        }`;
    }

    function getValue(params, field){
        if(!params.row[field]) return ""
        return params.row[field]
    }

    return <Paper style={{padding: "2rem"}}>
        <NTMXGrid
            columns={columns}
            rows={localSessions || []}
            title="Sessioni"
            getRowId={(row) => localSessions && row.id}
            onColumnVisibilityChange={() => {
                if(gridApiRef.current.getColumn("polyline").hide) gridApiRef.current.setColumnVisibility("polyline",true)
            }}
            apiRef={gridApiRef}
            rightButton={<Grid container justifyContent={"flex-end"}>
                <StartIconButton
                    onClick={() => setToCheckFilter(!toCheckFilter)}
                    title={!toCheckFilter ? "Da controllare" : "Lista completa"}
                    startIcon={!toCheckFilter ? <FilterListIcon/> : <ListIcon/>}/>
            </Grid>}
        />
    </Paper>

}

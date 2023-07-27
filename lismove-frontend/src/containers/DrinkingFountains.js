import {firebaseDatabase} from "../firebase";
import React,{useEffect,useState} from "react";
import {Paper} from "@mui/material";
import NTMXGrid,{timestampTypeToDateTime} from "../components/NTMXGrid";
import RenderCoordinates from "../components/cellRender/RenderCoordinates";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import RedoIcon from '@mui/icons-material/Redo';
import StartIconButton from "../components/buttons/StartIconButton";
import RenderUserRedirect from "../components/cellRender/RenderUserRedirect";
import DeleteIcon from "@mui/icons-material/Delete";
import ConfirmIconButton from "../components/buttons/ConfirmIconButton";
import Grid from "@mui/material/Grid";
import {useSnackbar} from "notistack";

export default function DrinkingFountains(){

    const starCountRef = firebaseDatabase.ref('/drinkingFountains')
    let [fountains, setFountains] = useState([]);
    const {enqueueSnackbar} = useSnackbar();

    const loadFountains = () => {
        starCountRef.on('value', querySnapShot => {
            let newFountains = []
            Object.entries(querySnapShot.val()).forEach(([key, value]) => {
                newFountains.push({key, ...value})
            })
            setFountains(newFountains)
        })
    }

    useEffect(loadFountains, [])

    const onRestore = (params) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        const values = params.row
        starCountRef.update({
            [values.key] : {
                lat: values.lat,
                lng: values.lng,
                name: values.name,
                uid: values.uid,
                deleted: false
            },
        }).then(() => {
            loadFountains()
            enqueueSnackbar("Saved", {variant: "success"});
        }).catch(() => enqueueSnackbar("Errore nel salvataggio", {variant: "error"}));
    }

    const onDelete = (params) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        starCountRef.update({
            [params.row.key] : {},
        }).then(() => {
            loadFountains()
            enqueueSnackbar("Saved", {variant: "success"});
        }).catch(() => enqueueSnackbar("Errore nel salvataggio", {variant: "error"}));
    }

    const defaultColumns = [
        {
            headerName: 'Coordinate Fontana',
            field: 'fountainCoordinates',
            width: 300,
            renderCell: (params) => <RenderCoordinates lat={params.row.lat} lng={params.row.lng}/>
        },
        {
            headerName: 'Creatore',
            field: 'uid',
            width: 350,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.value} showUsername/>
        },
        {
            headerName: 'Data Creazione',
            field: 'createdAt',
            width: 200,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Attivo',
            field: 'deleted',
            width: 200,
            renderCell: (params) =>  <Grid container alignItems="center">
                <FiberManualRecordIcon style={{color: params.value === true ? "#ff0000" : "#43ff00"}}/>
                {params.value === true && <StartIconButton onClick={() => onRestore(params)} title="Ripristina" startIcon={<RedoIcon/>}/>}
            </Grid>
        },
        {
            headerName: 'Cancellato da',
            field: 'deletedBy',
            width: 350,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.value} showUsername/>
        },
        {
            headerName: 'Data Cancellazione',
            field: 'deletedAt',
            width: 220,
            ...timestampTypeToDateTime
        },
        {
            headerName: "Elimina",
            field: "delete",
            width: 90,
            sortable: false,
            disableColumnMenu: true,
            resizable: false,
            renderCell: params => <Grid container justifyContent="center">
                <ConfirmIconButton
                    onConfirm={() => onDelete(params)}
                    title="Conferma eliminazione"
                    text={"Si desidera eliminare definitivamente la fontana?"}
                >
                    <DeleteIcon/>
                </ConfirmIconButton>
            </Grid>
        }
    ]

    return <Paper style={{padding: "2rem"}}>

        <NTMXGrid
            columns={defaultColumns}
            rows={fountains}
            title="Fontanelle"
            getRowId={(row) => fountains && row.key}
        />

    </Paper>
}

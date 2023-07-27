import {useParams} from "react-router-dom";
import makeStyles from '@mui/styles/makeStyles';
import {useGetOrganizationSeats} from "../../services/ContentManager";
import React,{useState} from "react";
import NTMXGrid from "../../components/NTMXGrid";
import DeleteIcon from '@mui/icons-material/Delete';
import ConfirmIconButton from "../../components/buttons/ConfirmIconButton";
import {deleteElem,getErrorMessage,ORGANIZATIONS,post,put,SEATS} from "../../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import Grid from "@mui/material/Grid";
import StartIconButton from "../../components/buttons/StartIconButton";
import AddIcon from "@mui/icons-material/Add";
import CreateSeatModal from "../../components/modals/CreateSeatModal";
import EditableCoordinates from "../../components/cellRender/EditableCoordinates";
import RenderCell from "../../components/cellRender/RenderCell";

const useStyles = makeStyles(theme => ({
    iconButton: {
        color: theme.palette.text.primary
    }
}));

function getCoordinates(params) {
    if(!params.row['latitude'] && !params.row['longitude']) return ""
    return `${params.row['latitude'] || ''} - ${
        params.row['longitude'] || ''
    }`;
}

export default function Seats() {

    let classes = useStyles();
    let {id} = useParams();
    let {seats} = useGetOrganizationSeats(id);
    seats = seats.slice().sort((a,b) => a.id - b.id)
    let [isCreating, setIsCreating] = useState(false);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    const deleteSeat = (sid) => {
        deleteElem(ORGANIZATIONS+"/"+id+"/"+SEATS+"/"+sid)
            .then(() => enqueueSnackbar("Deleted", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, SEATS]));
    }

    const createSeat = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        post(ORGANIZATIONS+"/"+id+"/"+SEATS, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, SEATS]));
    }

    const saveEdit = (sid, newValues) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(ORGANIZATIONS+"/"+id+"/"+SEATS+"/"+sid, {body: newValues})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, SEATS]));
    }

    const defaultColumns = [
        {
            headerName: 'Nome',
            field: 'name',
            width: 220,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {(sid, field, value) => saveEdit(sid, {[field]: value})} required/>
        },
        {
            headerName: 'Indirizzo',
            field: 'address',
            width: 220,
        },
        {
            headerName: 'Numero',
            field: 'number',
            width: 220,
        },
        {
            headerName: 'Città',
            field: 'cityName',
            width: 220,
        },
        {
            headerName: 'Coordinate',
            field: 'coordinates',
            width: 300,
            renderCell: (params) => <EditableCoordinates latitude={params.row.latitude ? params.row.latitude : ""}
                                                         longitude={params.row.longitude ? params.row.longitude : ""}
                                                         onChange={(lat,lng) => saveEdit(params.id,{latitude: lat, longitude: lng})}/>,
        },
        {
            headerName: 'Tolleranza',
            field: 'destinationTolerance',
            width: 220,
            renderCell: (params) => <RenderCell params = {params} required type="number" min={0} step={0.01} measureUnit="km"
                                                saveEdit = {(sid, field, value) => saveEdit(sid, {[field]: value})}/>
        },
        {
            headerName: ' ',
            field: 'delete',
            width: 67,
            renderCell: (params => {
                return <ConfirmIconButton
                    onConfirm={() => deleteSeat(params.row['id'])}
                    title="Conferma eliminazione"
                    text={"Si desidera veramente eliminare la sede \"" + params.row['name']+"\""}
                >
                    <DeleteIcon className={classes.iconButton}/>
                </ConfirmIconButton>
            }),
        }
    ];

    return (
        <div>
            <NTMXGrid
                columns={defaultColumns}
                rows={seats || []}
                title="Sedi"
                rightButton={
                    <Grid container justifyContent={"flex-end"}>
                        <StartIconButton onClick={() => setIsCreating(true)} title="aggiungi sede" startIcon={<AddIcon/>}/>
                    </Grid>}
            />
            <CreateSeatModal open={!!isCreating} onClose={() => setIsCreating(false)} onSubmit={createSeat}/>
        </div>
    );
}

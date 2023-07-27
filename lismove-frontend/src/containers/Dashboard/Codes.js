import {useParams} from "react-router-dom";
import {useGetOrganizationCodes} from "../../services/ContentManager";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import NTMXGrid,{timestampTypeToDate,timestampTypeToDateTime} from "../../components/NTMXGrid";
import React,{useState} from "react";
import GenerateCodesModal from "../../components/modals/GenerateCodesModal";
import {CODES,getErrorMessage,ORGANIZATIONS,post,put} from "../../services/Client";
import Grid from "@mui/material/Grid";
import StartIconButton from "../../components/buttons/StartIconButton";
import EditIcon from "@mui/icons-material/Edit";
import EditCodesModal from "../../components/modals/EditCodesModal";
import AddIcon from "@mui/icons-material/Add";
import RenderUserRedirect from "../../components/cellRender/RenderUserRedirect";


const defaultColumns = [
    {
        headerName: 'Codice',
        field: 'code',
        width: 220,
    },
    {
        headerName: 'Data Inizio',
        field: 'startDate',
        width: 180,
        ...timestampTypeToDate
    },
    {
        headerName: 'Data Fine',
        field: 'endDate',
        width: 180,
        ...timestampTypeToDate
    },
    {
        headerName: 'Data Attivazione',
        field: 'activationDate',
        width: 180,
        ...timestampTypeToDateTime
    },
    {
        headerName: 'Utente',
        field: 'user',
        width: 180,
        renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
    }
];

export default function Codes() {

    let {id} = useParams();
    let {codes} = useGetOrganizationCodes(id);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let [isGenerating, setIsGenerating] = useState(false);
    let [isEditing, setIsEditing] = useState(false);
    let [selectedRows, setSelectedRows] = useState([]);
    codes = codes.slice().sort((a , b) => b.lastModifiedDate - a.lastModifiedDate);

    const generateCodes = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        post(ORGANIZATIONS+"/"+id+"/"+CODES, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, CODES]));
    }

    const editCodes = (values) => {
        values = {...values, selectedEnrollments: selectedRows}
        enqueueSnackbar("Saving...", {variant: "info"});
        put(ORGANIZATIONS+"/"+id+"/"+CODES, {body: values})
            .then(() => enqueueSnackbar("Saved",{variant : "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, CODES]));
    }

    return <div>
        <NTMXGrid
            checkboxSelection
            columns={defaultColumns}
            rows={codes || []}
            title="Codici"
            rightButton={<Grid container justifyContent={"flex-end"}>
                            <StartIconButton onClick={() => setIsEditing(true)} title="Modifica" startIcon={<EditIcon/>} disabled={selectedRows.length === 0}/>
                            <StartIconButton onClick={() => setIsGenerating(true)} title="GENERA CODICI" startIcon={<AddIcon/>}/>
                        </Grid>}
            onSelectionModelChange={(newSelection) => {
                setSelectedRows(newSelection);
            }}
            selectionModel={selectedRows}
        />
        <GenerateCodesModal open={!!isGenerating} onClose={() => setIsGenerating(false)} onSubmit={generateCodes}/>
        <EditCodesModal open={!!isEditing} onClose={() => setIsEditing(false)} onSubmit={editCodes}/>
    </div>
}
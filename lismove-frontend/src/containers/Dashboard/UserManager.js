import {useParams} from "react-router-dom";
import {useGetOrganizationManagers} from "../../services/ContentManager";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import Grid from "@mui/material/Grid";
import StartIconButton from "../../components/buttons/StartIconButton";
import AddIcon from "@mui/icons-material/Add";
import NTMXGrid,{timestampTypeToDate} from "../../components/NTMXGrid";
import React,{useState} from "react";
import AddUserModal from "../../components/modals/AddUserModal";
import {getErrorMessage,ORGANIZATIONS,post,USERS} from "../../services/Client";

const defaultColumns = [
    {
        headerName: 'UID',
        field: 'uid',
        width: 260,
        hide: true
    },
    {
        headerName: 'Username',
        field: 'username',
        width: 220
    },
    {
        headerName: 'Nome',
        field: 'firstName',
        width: 220
    },
    {
        headerName: "Cognome",
        field: "lastName",
        width: 220
    },
    {
        headerName: "Genere",
        field: "gender",
        width: 130,
        hide: true
    },
    {
        headerName: 'Email',
        field: 'email',
        width: 220
    },
    {
        headerName: '  Telefono',
        field: 'phoneNumber',
        width: 170,
        hide: true,
    },
    {
        headerName: 'Data di Nascita',
        field: 'birthDate',
        width: 180,
        hide: true,
        ...timestampTypeToDate
    }
];

export default function UserManager() {

    let {id} = useParams();
    let {managers} = useGetOrganizationManagers(id);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let [isAddingManager, setIsAddingManager] = useState(false);

    const createManager = (values) => {
        let newManager = {...values}
        if(newManager.birthDate) newManager.birthDate = new Date(newManager.birthDate).getTime();
        enqueueSnackbar("Saving...", {variant: "info"});
        post(ORGANIZATIONS+"/"+id+"/"+USERS, {body: newManager})
            .then(() =>enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, USERS],));
    }

    return <div>
        <NTMXGrid
            columns={defaultColumns}
            rows={managers || []}
            title="Managers"
            getRowId={(row) => managers && row.uid}
            rightButton={<Grid container justifyContent={"flex-end"}>
                <StartIconButton onClick={() => setIsAddingManager(true)} title="Aggiungi Manager" startIcon={<AddIcon/>}/>
            </Grid>}
        />
        <AddUserModal open={!!isAddingManager} onClose={() => setIsAddingManager(false)} onSave={createManager}/>
    </div>
}
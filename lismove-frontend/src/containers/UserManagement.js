import {Paper} from "@mui/material";
import NTMXGrid,{timestampTypeToDateTime} from "../components/NTMXGrid";
import {getTableColumns,useGetUsers} from "../services/ContentManager";
import React, {useEffect, useState} from "react";
import {getErrorMessage,put,USERS} from "../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import RenderCell from "../components/cellRender/RenderCell";
import {useGridApiRef} from "@mui/x-data-grid-pro";
import EditableCoordinates from "../components/cellRender/EditableCoordinates";
import RenderUserRedirect from "../components/cellRender/RenderUserRedirect";
import RenderUserEnrollments from "../components/cellRender/RenderUserEnrollments";
import RenderUserWorkAddresses from "../components/cellRender/RenderUserWorkAddresses";
const storageKey = "user-table-columns"

function getWorkCoordinates(params) {
    if(!params.row.workAddresses) return "";
    let latitude = params.row.workAddresses[params.row.workAddresses.length-1].latitude
    let longitude = params.row.workAddresses[params.row.workAddresses.length-1].longitude
    return `${latitude || ''} - ${longitude || ''}`;
}

export function UserManagement() {


    let {users = []} = useGetUsers();
    let [addUserModal, setAddUserModal] = useState(false);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let [columns, setColumns] = useState([])
    const gridApiRef = useGridApiRef()
    let cachedRef;

    const defaultColumns = [
        {
            headerName: 'UID',
            field: 'uid',
            width: 340,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
        },
        {
            headerName: 'Email',
            field: 'email',
            width: 220
        },
        {
            headerName: 'Telefono',
            field: 'phoneNumber',
            width: 170,
            hide: true,
            renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEdit}/>
        },
        {
            headerName: 'Username',
            field: 'username',
            width: 130,
            renderCell: (params) => <RenderUserRedirect value={params.value} url={params.row.uid}/>
        },
        {
            headerName: 'Nome',
            field: 'firstName',
            width: 130
        },
        {
            headerName: "Cognome",
            field: "lastName",
            width: 130
        },
        {
            headerName: 'Codice Fiscale',
            field: 'fiscalCode',
            width: 180,
            hide: true
        },
        {
            headerName: 'Indirizzo Casa',
            field: 'homeAddress',
            width: 250,
            valueGetter: params => {
                if(params.row.homeAddresses){
                    let index = params.row.homeAddresses.length-1
                    return  params.row.homeAddresses[index].address + " n." + params.row.homeAddresses[index].number + ", " + params.row.homeAddresses[index].cityName
                }
                 else return ""
            },
        },
        {
            field: 'homeAddressCoordinates',
            headerName: 'Coordinate Casa',
            width: 350,
            renderCell: (params) => <EditableCoordinates latitude={params.row.homeAddresses ? params.row.homeAddresses[params.row.homeAddresses.length-1].latitude : ""}
                                                         longitude={params.row.homeAddresses ? params.row.homeAddresses[params.row.homeAddresses.length-1].longitude : ""}
                                                         onChange={(lat,lng) => saveEditHomeCoordinates(params.id, lat, lng)}/>,
            hide: true,
        },
        {
            headerName: 'Indirizzo Lavoro',
            field: 'workAddress',
            width: 250,
            hide: true,
            renderCell: RenderUserWorkAddresses
        },
        {
            field: 'workAddressCoordinates',
            headerName: 'Coordinate Lavoro',
            width: 350,
            valueGetter: (params) => getWorkCoordinates(params),
            hide: true,
        },
        {
            field: 'enrollments',
            headerName: 'Iniziative',
            width: 250,
            hide: true,
            renderCell: RenderUserEnrollments
        },
        {
            field: "lastLoggedIn",
            headerName: "Ultimo Accesso",
            width: 180,
            ...timestampTypeToDateTime,
            hide: true
        }
    ];

    useEffect(() => {
        if(gridApiRef.current) {
            cachedRef = gridApiRef.current;
            setColumns(getTableColumns(storageKey, defaultColumns));
        }
        return () => localStorage.setItem(storageKey, JSON.stringify(cachedRef.getAllColumns().map(c => {return {headerName:c.headerName, hide:c.hide}})))
    }, [gridApiRef])

    const saveEditHomeCoordinates = (uid, lat, lng) => {
        let newValues = {
            homeLatitude: lat,
            homeLongitude: lng
        }
        enqueueSnackbar("Saving...", {variant: "info"});
        put(USERS, {body: newValues, elem: uid})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(USERS));
    }

    const saveEdit = (uid, field, value) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(USERS, {body: {[field]: value}, elem: uid})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(USERS));
    }

    return <Paper style={{padding: "2rem"}}>

        <NTMXGrid
            columns={columns}
            rows={users || {}}
            title="Utenti"
            getRowId={(row) => users && row.uid}
            apiRef={gridApiRef}
        />

    </Paper>

}

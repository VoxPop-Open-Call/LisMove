import {IconButton, Paper, Tooltip} from "@mui/material";
import NTMXGrid from "../components/NTMXGrid";
import {getTableColumns, useGetVendors} from "../services/ContentManager";
import React, {useEffect, useState} from "react";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import {useGridApiRef} from "@mui/x-data-grid-pro";
import RenderBoolean from "../components/cellRender/RenderBoolean";
import {getErrorMessage, put, VENDORS} from "../services/Client";
import {firebaseAuth} from "../firebase";
import CircularLoading from "../components/CircularLoading";
import VisibilityIcon from "@mui/icons-material/Visibility";
import {Link} from "react-router-dom";

const storageKey = "user-table-columns";

export default function VendorManagement() {
    let queryClient = useQueryClient();
    const {enqueueSnackbar} = useSnackbar();
    let {vendors, state} = useGetVendors();
    let [columns, setColumns] = useState([])
    const gridApiRef = useGridApiRef()
    let cachedRef;

    const saveEditVendor = (id, field, value, row) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(VENDORS, {elem: row.uid, body: {[field]: value},})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([VENDORS, {vendor:firebaseAuth.currentUser.uid}]));
    };

    const defaultColumns = [
        {
            headerName: 'UID',
            field: 'uid',
            width: 220,
            hide: true,
        }, {
            headerName: ' ',
            field: 'visible',
            width: 80,
            renderCell: (params) => <Tooltip disableInteractive title={params.row.visible ? "Visibile" : "Nascosto" }><div><RenderBoolean params={params} saveEdit={saveEditVendor}/></div></Tooltip>
        }, {
            headerName: 'Ragione sociale',
            field: 'businessName',
            minWidth: 220,
        }, {
            headerName: 'Indirizzo',
            field: 'address',
            minWidth: 170,
        }, {
            headerName: 'Partita Iva',
            field: 'vatNumber',
            minWidth: 150,
        }, {
            headerName: 'IBAN',
            field: 'iban',
            minWidth: 170,
        }, {
            headerName: 'BIC',
            field: 'bic',
            minWidth: 60,
        }, {
            headerName: 'Email',
            field: 'email',
            minWidth: 170,
        }, {
            headerName: 'Nome',
            field: 'firstName',
            minWidth: 170,
        }, {
            headerName: 'Cognome',
            field: 'lastName',
            minWidth: 170,
        }, {
            headerName: 'Numero',
            field: 'phone',
            minWidth: 150,
        }, {
            headerName: 'Coupon rilasciati',
            field: 'totalValueReleasedCoupon',
            minWidth: 170,
        }, {
            headerName: 'Coupon assegnati',
            field: 'numberAssignedCoupon',
            minWidth: 170,
        },{
            headerName: 'Dettagli',
            field: 'id',
            minWidth: 130,
            renderCell:(params) => <Link to={"/vendors/" + params.row.uid}><IconButton size="large"><VisibilityIcon/></IconButton></Link>
        },
    ];

    useEffect(() => {
        if (gridApiRef.current) {
            cachedRef = gridApiRef.current;
            setColumns(getTableColumns(storageKey, defaultColumns));
        }
        return () => localStorage.setItem(storageKey, JSON.stringify(cachedRef.getAllColumns().map(c => {
            return {headerName: c.headerName, hide: c.hide}
        })))
    }, [gridApiRef])

    return <Paper style={{padding: "2rem"}}>
        <NTMXGrid
            columns={columns}
            rows={vendors || []}
            title="Vendors"
            getRowId={(row) => vendors && row.uid}
            apiRef={gridApiRef}
        />
        {state === 'loading' && <CircularLoading/>}
    </Paper>

}

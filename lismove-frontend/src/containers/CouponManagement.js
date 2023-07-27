import {Paper} from "@mui/material";
import NTMXGrid, { timestampTypeToDate } from "../components/NTMXGrid";
import { useGetCoupons} from "../services/ContentManager";
import React, {useState} from "react";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import {useGridApiRef} from "@mui/x-data-grid-pro";
import {getErrorMessage, put} from "../services/Client";
import {firebaseAuth} from "../firebase";
import CircularLoading from "../components/CircularLoading";
import RenderUserRedirect from "../components/cellRender/RenderUserRedirect";
import {COUPONS} from "../services/Client";
import Grid from "@mui/material/Grid";
import StartIconButton from "../components/buttons/StartIconButton";
import CheckIcon from '@mui/icons-material/Check';

const storageKey = "user-table-columns";

export default function CouponManagement() {
    let queryClient = useQueryClient();
    const {enqueueSnackbar} = useSnackbar();
    let {coupons, state} = useGetCoupons();
    let [selectedRows, setSelectedRows] = useState([]);
    const gridApiRef = useGridApiRef()
    let cachedRef;

    const handleRefound = (id, field, value, row) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(COUPONS + '/' + 'refound', {body: {selectedCoupons:selectedRows}})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([COUPONS, {vendor:firebaseAuth.currentUser.uid}]));
    };

    const columns = [
        {
            headerName: 'Code',
            field: 'code',
            width: 150,
        },
        {
            headerName: 'Tipo',
            field: 'awardType',
            width: 150,
            valueFormatter: ({ value }) => {
                switch (value) {
                    case 0:
                        return 'Soldi';
                    case 1:
                        return 'Punti';
                    case 2:
                        return 'Riscattato in comune';
                    case 3:
                        return 'Riscattato in negozio';
                    default:
                        return '';
                }
            }
        },{
            headerName: 'Data riscatto',
            field: 'redemptionDate',
            width: 200,
            ...timestampTypeToDate
        },{
            headerName: 'Data rimborso',
            field: 'refundDate',
            width: 180,
            ...timestampTypeToDate
        },{
            headerName: 'Data scadenza',
            field: 'expireDate',
            width: 180,
            ...timestampTypeToDate
        },{
            headerName: 'Titolo',
            field: 'title',
            width: 180,
        },{
            headerName: 'Valore',
            field: 'value',
            width: 150,
        },{
            headerName: 'User UID',
            field: 'uid',
            width: 220,
            renderCell: (params) => params.row.uid &&  <RenderUserRedirect value={params.value} url={params.row.uid}/>
        }
    ];

    return <Paper style={{padding: "2rem"}}>
        <NTMXGrid
            checkboxSelection
            columns={columns}
            rows={coupons || []}
            title="Coupons"
            getRowId={(row) => coupons && row.id}
            apiRef={gridApiRef}
            rightButton={<Grid container justifyContent={"flex-end"}>
                            <StartIconButton onClick={() => handleRefound(true)} title="Bonifica" startIcon={<CheckIcon/>} disabled={selectedRows.length === 0}/>
                        </Grid>}
            onSelectionModelChange={(newSelection) => {
                setSelectedRows(newSelection);
            }}
            selectionModel={selectedRows}
        />
        {state === 'loading' && <CircularLoading/>}
    </Paper>

}
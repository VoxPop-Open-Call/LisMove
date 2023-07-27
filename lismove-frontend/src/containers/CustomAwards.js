import Grid from "@mui/material/Grid";
import NTMXGrid,{timestampTypeToDate} from "../components/NTMXGrid";
import React,{useState} from "react";
import {Divider} from "@mui/material";
import StartIconButton from "../components/buttons/StartIconButton";
import CreateIcon from '@mui/icons-material/Create';
import {AWARDS, getErrorMessage, post} from "../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import dayjs from "dayjs";
import {useGetCustomAwards} from "../services/ContentManager";
import CustomAwardModal from "../components/modals/CustomAwardModal";

const typeLabels = ["Euro", "Punti", "Da ritirare"]

export default function CustomAwards(){

    let [isCreating, setIsCreating] = useState(false);
    let {customAwards} = useGetCustomAwards()

    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    const today = dayjs(new Date()).format("YYYY-MM-DD");
    customAwards = customAwards.slice().sort((a,b) => a.id - b.id);

    const defaultColumns = [
        {
            headerName: 'ID',
            field: 'id',
            width: 200,
        },
        {
            headerName: 'Nome',
            field: 'name',
            width: 200,
        },
        {
            headerName: 'Descrizione',
            field: 'description',
            width: 200
        },
        {
            headerName: 'Valore',
            field: 'value',
            width: 200
        },
        {
            headerName: 'Tipo',
            field: 'type',
            width: 200,
            valueGetter: (params) => params.value || params.value === 0 ? typeLabels[params.value] : ""
        },
        {
            headerName: 'N° assegnazioni massime',
            field: 'winningsAllowed',
            width: 200,
        },
    ];

    const create = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        post(`${AWARDS}/customs`, {body: values})
            .then(() => enqueueSnackbar("Saved", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries("AwardsCustom"));
    }

    return <Grid>
        <NTMXGrid
            columns={defaultColumns}
            rows={customAwards || []}
            title="Premi personalizzati"
            rightButton={<Grid container justifyContent={"flex-end"}>
                <StartIconButton onClick={() => setIsCreating(true)} title="Crea premio" startIcon={<CreateIcon/>}/>
            </Grid>}
        />
        <CustomAwardModal open={!!isCreating} onClose={() => setIsCreating(false)} onSubmit={create}/>
    </Grid>

}

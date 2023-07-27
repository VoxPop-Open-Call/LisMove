import React,{useState} from "react";
import {useGetOrganization} from "../../services/ContentManager";
import Grid from "@mui/material/Grid";
import GestureIcon from '@mui/icons-material/Gesture';
import CancelIcon from '@mui/icons-material/Cancel';
import StartIconButton from "../../components/buttons/StartIconButton";
import AddIcon from "@mui/icons-material/Add";
import AreaMap from "./AreaMap";
import SelectCitiesModal from "../../components/modals/SelectCitiesModal";
import {useParams} from "react-router-dom";
import {getErrorMessage,ORGANIZATIONS,put} from "../../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";

export default function AreaManager(){

    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let {id} = useParams();
    let {organization} = useGetOrganization(id);
    let [isEditingArea, setIsEditingArea] = useState(false);
    let [isAddingCity, setIsAddingCity] = useState(false);
    let [isDrawingArea, setIsDrawingArea] = useState(false);
    let [newIstat, setNewIstat] = useState();

    const putOrganization = (newData) => {
        enqueueSnackbar("Saving...", {variant: "info"});

        put(ORGANIZATIONS, {body: newData, elem: organization.id})
            .then(() => {
                enqueueSnackbar("Saved",{variant : "success"})
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(ORGANIZATIONS, {id: organization.id}));
    }

    const saveAdding = (newIstat) => {
        setNewIstat(newIstat)
        setIsAddingCity(false)
    }

    const onPolygonComplete = (polygon) => {
        let newPolygon = [];
        let updatedGeojson = [];

        polygon.getPaths().forEach(p => p.forEach(pa => newPolygon.push(pa.toJSON())));

        if(newPolygon.length < 3){
            polygon.visible = false;
            enqueueSnackbar("Selezionare almeno 3 punti", {variant: "error"});
            return;
        }

        if(organization.geojson) {
            updatedGeojson = JSON.parse(organization.geojson);
            updatedGeojson.push(newPolygon);
        } else updatedGeojson = [newPolygon];

        putOrganization({geojson : JSON.stringify(updatedGeojson)});

        setIsDrawingArea(false);
        polygon.visible = false;
    }

    return <div>
        <Grid container  justifyContent="space-between">
            <Grid item>
                {!isDrawingArea && !isEditingArea && <StartIconButton title="Aggiungi Comune" onClick={() => setIsAddingCity(true)} startIcon={<AddIcon/>}/>}
            </Grid>
            <Grid item>
                {!isEditingArea && (isDrawingArea ?
                    <StartIconButton title="Annulla" onClick={() => setIsDrawingArea(false)} startIcon={<CancelIcon/>}/>
                    : <StartIconButton title="Disegna nuova area" onClick={() => setIsDrawingArea(true)} startIcon={<GestureIcon/>}/>)}
            </Grid>
        </Grid>

        <AreaMap
            putOrganization={putOrganization}
            newIstat={newIstat}
            isDrawingArea={isDrawingArea}
            onPolygonComplete={onPolygonComplete}
            setIsEditingArea={setIsEditingArea}/>

        <SelectCitiesModal open={isAddingCity} onClose={() => setIsAddingCity(false)} onSubmit={saveAdding}/>
    </div>
}
import MapContainer from "../../components/MapContainer";
import makeStyles from '@mui/styles/makeStyles';
import React,{useCallback,useEffect,useRef,useState} from "react";
import {Polygon, DrawingManager} from "@react-google-maps/api";
import {useGetCities,useGetCity,useGetOrganization} from "../../services/ContentManager";
import {useParams} from "react-router-dom";
import Grid from "@mui/material/Grid";
import StartIconButton from "../../components/buttons/StartIconButton";
import SaveIcon from "@mui/icons-material/Save";
import DeleteIcon from "@mui/icons-material/Delete";
import CancelIcon from "@mui/icons-material/Cancel";
import {CITIES,get} from "../../services/Client";

const useStyles = makeStyles(theme => ({
    root: {
        width: "100%",
        height: "70vh",
    },
}));

function getCenter(city, setCenter){
    new window.google.maps.Geocoder().geocode({
        componentRestrictions: {
            country: 'IT',
            locality: city
        }
    }, (results,status) => {
        if(status == 'OK') {
            setCenter(results[0].geometry.location);
        }
    })
}

function getPolygons(paths){
    let polygons = [];
    for(let i = 0; i < paths.length; i++){
        let coordinates = [];
        for(let j = 0; j < paths[i][0].length; j++){
            coordinates.push({lng: paths[i][0][j][0], lat: paths[i][0][j][1]});
        }
        polygons.push(coordinates);
    }
    return polygons;
}

function getPolygon(paths){
    let polygon = [];
    for(let i = 0; i < paths.length; i++){
        for(let j = 0; j < paths[i].length; j++){
            polygon.push({lng: paths[i][j][0], lat: paths[i][j][1]});
        }
    }
    return polygon;
}

export default function AreaMap({newIstat, isDrawingArea, onPolygonComplete, setIsEditingArea, putOrganization}){

    let classes = useStyles();
    let {id} = useParams();
    let {organization} = useGetOrganization(id);
    let organizationGeojson = organization.geojson && JSON.parse(organization.geojson)
    let [center, setCenter] = useState(organizationGeojson && organizationGeojson[0] && organizationGeojson[0][0]);
    let [editableAreaIndex, setEditableAreaIndex] = useState(-1);
    let [editableArea, setEditableArea] = useState([]);
    const polygonRef = useRef(null);
    const listenersRef = useRef([]);

    useEffect(() => {
        if(newIstat){
            get(CITIES, {elem: newIstat}).then(city => {

                let paths = city.geojson ? city.geojson.coordinates : {};
                let newPolygons;
                getCenter(city.city,setCenter);

                if(city.geojson && city.geojson.type === "MultiPolygon") {
                    newPolygons = getPolygons(paths);
                }
                if(city.geojson && city.geojson.type === "Polygon") {
                    newPolygons = [getPolygon(paths)];
                }

                if(newPolygons) {
                    let newData;

                    if(organizationGeojson) {
                        newPolygons.forEach((p) => organizationGeojson.push(p))
                        newData = {geojson : JSON.stringify(organizationGeojson)};
                    } else newData = {geojson : JSON.stringify(newPolygons)};

                    putOrganization(newData)
                }
            })
        }

    }, [newIstat])

    const onEdit = useCallback(() => {
        if (polygonRef.current) {
            const nextPath = polygonRef.current
                .getPath()
                .getArray()
                .map(latLng => {
                    return { lat: latLng.lat(), lng: latLng.lng() };
                });
            setEditableArea(nextPath)
        }
    }, [setEditableArea]);

    const onLoad = useCallback(
        polygon => {
            polygonRef.current = polygon;
            const path = polygon.getPath();
            listenersRef.current.push(
                path.addListener("set_at", onEdit),
                path.addListener("insert_at", onEdit),
                path.addListener("remove_at", onEdit)
            );
        },
        [onEdit]
    );

    const onUnmount = useCallback(() => {
        listenersRef.current.forEach(lis => lis.remove());
        polygonRef.current = null;
    }, []);

    const cancelEdit = () => {
        setEditableArea(null)
        setEditableAreaIndex(-1)
        setIsEditingArea(false)
    }

    const saveEdit = () => {
        let updatedGeojson = JSON.parse(organization.geojson);
        updatedGeojson.splice(editableAreaIndex, 1, editableArea);
        putOrganization({geojson : JSON.stringify(updatedGeojson)});
        setEditableArea(null);
        setEditableAreaIndex(-1)
        setIsEditingArea(false)
    }

    const deleteEdit = () => {
        let updatedGeojson = JSON.parse(organization.geojson);
        updatedGeojson.splice(editableAreaIndex, 1);
        putOrganization({geojson : JSON.stringify(updatedGeojson)});
        setEditableArea(null);
        setEditableAreaIndex(-1)
        setIsEditingArea(false)
    }

    return (
        <Grid container className={classes.root}>
            <Grid container direction="row" justifyContent="space-evenly" alignItems="center">
                <Grid item>
                    {editableAreaIndex !== -1 && <StartIconButton title="Salva" onClick={saveEdit} startIcon={<SaveIcon/>}/>}
                </Grid>
                <Grid item>
                    {editableAreaIndex !== -1 && <StartIconButton title="Elimina" onClick={deleteEdit} startIcon={<DeleteIcon/>}/>}
                </Grid>
                <Grid item>
                    {editableAreaIndex !== -1 && <StartIconButton title="Annulla" onClick={cancelEdit} startIcon={<CancelIcon/>}/>}
                </Grid>
            </Grid>
            <MapContainer center={center} zoom={center ? 13 : 6}>

                {
                    organizationGeojson && organizationGeojson.map((p, i) =>
                        (!editableArea || (editableArea && editableAreaIndex !== i)) &&
                        <Polygon path={p} key={i}
                               onClick={() => {
                                   setEditableArea(p)
                                   setEditableAreaIndex(i)
                                   setIsEditingArea(true)
                               }}/>)
                }

                {editableArea &&
                    <Polygon path={editableArea}
                              key={editableAreaIndex}
                              editable
                              onMouseUp={onEdit}
                              onLoad={onLoad}
                              onUnmount={onUnmount}/>}

                {isDrawingArea &&
                    <DrawingManager
                        drawingMode={window.google.maps.drawing.OverlayType.POLYGON}
                        options={{drawingControl : false}}
                        onPolygonComplete={onPolygonComplete}
                    />
                }
            </MapContainer>
        </Grid>
    );
}
import {useParams} from "react-router-dom";
import makeStyles from '@mui/styles/makeStyles';
import {useGetOrganizationPartials} from "../../services/ContentManager";
import React,{useEffect,useState} from "react";
import Grid from "@mui/material/Grid";
import MapContainer from "../../components/MapContainer";
import {HeatmapLayer,Marker,MarkerClusterer} from "@react-google-maps/api";
import {CircularProgress} from "@mui/material";
import startIcon from "../../images/icons/pin-start.svg"
import finishIcon from "../../images/icons/pin-stop.svg"
import pauseIcon from "../../images/icons/pin-pause.svg"
import resumeIcon from "../../images/icons/pin-resume.svg"
import {partialType} from "../../constants/partialType";

const useStyles = makeStyles(theme => ({
    root: {
        width: "100%",
        height: "70vh",
    },
}));

export default function Heatmap({filters}){

    let classes = useStyles();
    let {id} = useParams();
    let {partials, status} = useGetOrganizationPartials(id, filters);
    let [storedPartials, setStoredPartials] = useState([]);
    let [zoom, setZoom] = useState(12);
    let [center, setCenter] = useState();

    useEffect(() => {
        if(partials) savePartials(partials)
    }, [partials])

    const savePartials = (loadedPartials) => {
        let nCoordinates = 0
        let midLat = 0
        let midLng = 0
        let temp = loadedPartials.map((p,i) => {
            if (i > 99 && i < 200) {
               midLat += p.lat
               midLng += p.lng
               nCoordinates++
            }
            return {
                lat: parseFloat(p.lat.toFixed(4)),
                lng: parseFloat(p.lng.toFixed(4))
            }
        })
        temp = Array.from(new Set(temp.map(JSON.stringify))).map(JSON.parse)
        let newPartials = []
        temp.map(partial => {
            partial.lat && partial.lng && newPartials.push(new window.google.maps.LatLng(partial.lat, partial.lng))
        })
        setStoredPartials(newPartials)
        setCenter(nCoordinates !== 0 ? new window.google.maps.LatLng(midLat/nCoordinates, midLng/nCoordinates) : new window.google.maps.LatLng(41.9028, 12.4964))
    }

    let markers = partials && partials.length !== 0 && partials.filter(m => m.type === partialType.START || m.type === partialType.END || m.type === partialType.PAUSE || m.type === partialType.RESUME)

    if(status === "loading" ) return <Grid container style={{padding: 30}}><CircularProgress/></Grid>;

    return <Grid container className={classes.root}>
        <MapContainer center={center} zoom={storedPartials.length !== 0 ? 12 : 6} onZoomChanged={setZoom}>

            <HeatmapLayer data={storedPartials} options={{maxIntensity : 25, radius: zoom < 13 ? 7 : 0.0008, opacity: 0.6, dissipating: zoom < 13}}/>

            {partials && partials.length !== 0 && <MarkerClusterer maxZoom={18}>{(clusterer) => markers.map((point, index) => {
                let icon;
                if(point.type === partialType.START) icon = startIcon
                if(point.type === partialType.END) icon = finishIcon
                if(point.type === partialType.PAUSE) icon = pauseIcon
                if(point.type === partialType.RESUME) icon = resumeIcon

                return <Marker key={index} position={point} clusterer={clusterer} icon={{url : icon,scaledSize : new window.google.maps.Size(35,48)}} zIndex={100}/>
            })}</MarkerClusterer>}

        </MapContainer>
    </Grid>

}
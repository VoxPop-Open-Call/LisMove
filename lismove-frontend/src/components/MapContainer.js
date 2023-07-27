import React,{useEffect,useState} from 'react'
import {GoogleMap} from '@react-google-maps/api'

const options = {
    disableDefaultUI: true,
    zoomControl: true,
    mapTypeControl: true,
    scaleControl: true,
    mapTypeControlOptions: {
        mapTypeIds: ['roadmap','satellite']
    }
};

const mapContainerStyle = {
    height: "100%",
    width: "100%"
};

const defaultCenter = {
    lat: 41.9028,
    lng: 12.4964,
};

const defaultZoom = 17;

export const libraries = ["geometry","drawing","visualization"];

export default function MapContainer({zoom, center, onCLick, children, onZoomChanged}) {

    let [mapCenter, setMapCenter] = useState(center)
    let [map, setMap] = useState(null)

    useEffect(() => {
        setMapCenter(center)
    }, [center])

    return <GoogleMap
            id="map"
            onLoad={map => setMap(map)}
            mapContainerStyle={mapContainerStyle}
            center={mapCenter || defaultCenter}
            options={options}
            zoom={zoom || defaultZoom}
            onZoomChanged={() => map && onZoomChanged && onZoomChanged(map.getZoom())}
            onClick={onCLick}
        >
            {children}
        </GoogleMap>
}
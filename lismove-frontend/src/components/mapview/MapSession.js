import {Circle,InfoWindow,Marker,Polyline} from "@react-google-maps/api";
import React,{useState} from "react";
import MapContainer from "../MapContainer";
import dayjs from "dayjs";
import startIcon from "../../images/icons/pin-start.svg"
import finishIcon from "../../images/icons/pin-stop.svg"
import pauseIcon from "../../images/icons/pin-pause.svg"
import resumeIcon from "../../images/icons/pin-resume.svg"
import skippedIcon from "../../images/icons/pin-skipped.svg"
import homeIcon from "../../images/icons/homeIcon.png"
import workIcon from "../../images/icons/workIcon.png"
import {partialType} from "../../constants/partialType";

export default function MapSession({polylines, zoom, points, polylineType, homePoint, workPoints, isShowingPartialPoints, center}){

    let [infoWindow, setInfoWindow] = useState(null)
    let polylinePaths = []
    polylines.polyline && polylines.polyline.map(p => polylinePaths.push(window.google.maps.geometry.encoding.decodePath(p)))
    let rawPaths = []
    polylines.rawPolyline && polylines.rawPolyline.map(p => rawPaths.push(window.google.maps.geometry.encoding.decodePath(p)))
    let gmapsPaths = []
    polylines.gmapsPolyline && polylines.gmapsPolyline.map(p => gmapsPaths.push(window.google.maps.geometry.encoding.decodePath(p)))


    const dottedLineSymbol = {
        path: "M 0,-1 0,1",
        strokeOpacity: 0.5,
        scale: 2.5,
    };

    const getPausePath = (path, index, paths) => {
        if(paths[index+1]){
            const dottedPath = [path[path.length-1], paths[index+1][0]]
            return <Polyline path={dottedPath}
                          options={{
                              strokeOpacity: 0,
                              icons: [
                                  {
                                      icon: dottedLineSymbol,
                                      offset: "0",
                                      repeat: "13px",
                                  },
                              ],
                          }}/>
        }
    }

    const getPolyline = () => {
        if(polylineType === "polyline") {
            return <div>
                {polylinePaths.map((path, index) => {
                        return <div key={index}>
                            <Polyline path={path}/>
                            {getPausePath(path, index, polylinePaths)}
                        </div>
                })}
            </div>
        }
        if(polylineType === "rawPolyline") { //se raw_polyline non è valorizzato a database, lo calcolo io
            if(rawPaths && rawPaths.length !== 0) return <div>
                {rawPaths.map((path, index) => {
                        return <div key={index}>
                            <Polyline path={path}/>
                            {getPausePath(path, index, rawPaths)}
                        </div>
                })}
            </div>
            let newPath = []
            points = points.slice().sort((a,b) => a.timestamp - b.timestamp);
            points.forEach(p => {
                newPath.push({ lat: p.latitude, lng: p.longitude})
            })
            return <Polyline path={newPath}/>
        }
        if(polylineType === "gmapsPolyline") {
            return <div>
                {gmapsPaths.map((path, index) => {
                        return <div key={index}>
                            <Polyline path={path}/>
                            {getPausePath(path, index, gmapsPaths)}
                        </div>
                })}
            </div>
        }
    }

    return <MapContainer zoom={zoom} center={polylinePaths[0][0]}>

        {getPolyline()}

        {points && points.map((p,index) => {
            let icon = null
            let zIndex = null
            if(p.type === partialType.START) {
                icon = {url : startIcon,scaledSize : new window.google.maps.Size(35,48)}
                zIndex = 1000
            }
            if(p.type === partialType.END) {
                icon = {url : finishIcon,scaledSize : new window.google.maps.Size(35,48)}
                zIndex = 1000
            }
            if(p.type === partialType.PAUSE) {
                icon = {url : pauseIcon,scaledSize : new window.google.maps.Size(35,48)}
                zIndex = 999
            }
            if(p.type === partialType.RESUME) {
                icon = {url : resumeIcon,scaledSize : new window.google.maps.Size(35,48)}
                zIndex = 999
            }
            if(p.type === partialType.SKIPPED) {
                icon = {url : skippedIcon,scaledSize : new window.google.maps.Size(30,42)}
            }

            if(isShowingPartialPoints || zIndex !== null)
            return <Marker key={index} position={{lat : p.latitude,lng : p.longitude}} onClick={() => setInfoWindow(index)}
                           icon={icon} zIndex={zIndex}>

                {infoWindow && infoWindow === index &&
                <InfoWindow onCloseClick={() => setInfoWindow(null)}>
                    <div>{dayjs(new Date(p.timestamp)).format("HH:mm:ss DD/MM/YYYY")}</div>
                </InfoWindow>}

            </Marker>
        })}

        {
            homePoint && <div>
                <Circle center={homePoint} radius={homePoint.tolerance}/>
                <Marker position={homePoint} icon={{url : homeIcon, scaledSize:  new window.google.maps.Size(48,48)}} zIndex={999}/>
            </div>

        }

        {
            workPoints && workPoints.map((point, index) => {
                return <div>
                    <Circle center={point} radius={point.tolerance}/>
                    <Marker key={index} position={point} icon={{url : workIcon, scaledSize:  new window.google.maps.Size(48,48)}} zIndex={999}/>
                </div>
            })
        }

    </MapContainer>
}

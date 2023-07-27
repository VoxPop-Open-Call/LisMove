import React,{useEffect,useRef,useState} from "react";
import {Circle,Layer,Line} from "react-konva";

export default function PathsLayer({selectedMap, paths, dragNode, onClickNode, onClickLink, onDragNode, ratio, scale, setSelected, selected, findPositionOnMap, originNode, cursorPositionLink, originNodeForLinkBetweenFloors}){

    let [isDragging, setIsDragging] = useState(false);
    const pathsLayer = useRef();

    let filteredPaths = Object.assign({}, paths);
    filteredPaths.nodes = paths.nodes && filteredPaths.nodes.filter(n => n.map === selectedMap);

    const filteredLinks = paths && paths.links && paths.links.filter(l =>
        filteredPaths.nodes.find(n => n.id === l.origin) && filteredPaths.nodes.find(n => n.id === l.destination)
    );

    let [cursorPosition, setCursorPosition] = useState({});
    let origin = originNode && paths.nodes.find(n => n.id === originNode);

    return (
        <Layer ref = {pathsLayer}>
            {filteredLinks && filteredLinks.map(l => {
                let origin = isDragging && dragNode === l.origin ? Object.assign({},{x: (cursorPosition.x), y: (cursorPosition.y)}) : filteredPaths.nodes.find(n => n.id === l.origin);
                let destination = isDragging && dragNode === l.destination ? Object.assign({},{x: (cursorPosition.x), y: (cursorPosition.y)}) : filteredPaths.nodes.find(n => n.id === l.destination);

                return <Line
                    points={[origin.x * ratio, origin.y * ratio, destination.x * ratio, destination.y * ratio]}
                    stroke={selected.link === l.id ? "#0A6889" : "#01FFDE"}
                    strokeWidth={4 / scale}
                    onClick={() => {
                        onClickLink(l);
                        selected.link === l.id ? setSelected({}) : setSelected({link: l.id});
                    }}
                />
            })}
            {origin && <Line
                    points={[origin.x * ratio, origin.y * ratio, cursorPositionLink.x * ratio, cursorPositionLink.y * ratio]}
                    stroke={"#01FFDE"} strokeWidth={4 / scale}/>
            }
            {filteredPaths && filteredPaths.nodes && filteredPaths.nodes.map(n =>{

                let fillColor =  "#01FFDE";

                if(!originNodeForLinkBetweenFloors && paths.links.find(l =>                //controlla se esiste un link con un estremo uguale a n.id e l'altro non appartenente ai nodi della mappa che stiamo considerando
                    (l.origin === n.id && !filteredPaths.nodes.find(n1 => n1.id === l.destination)) ||
                    (!filteredPaths.nodes.find(n1 => n1.id === l.origin) && l.destination === n.id)))
                        fillColor = "#0A6889";

                if(originNodeForLinkBetweenFloors && paths.links.find(l =>                //controlla se esiste un link con un estremo uguale a n.id e l'altro non appartenente ai nodi della mappa che stiamo considerando
                    (l.origin === n.id && l.destination === originNodeForLinkBetweenFloors) ||
                    (l.origin === originNodeForLinkBetweenFloors && l.destination === n.id)))
                    fillColor = "#0A6889";

                if(selected.node === n.id) fillColor = "#fff";

                return <Circle
                    x = {n.x * ratio}
                    y = {n.y * ratio}
                    radius={5 / scale}
                    strokeWidth={3 / scale}
                    fill={fillColor}
                    stroke="#01FFDE"
                    draggable={dragNode === n.id}
                    onDragStart={() => setIsDragging(true)}
                    onDragEnd={() => {
                        setIsDragging(false);
                        setSelected({});
                        onDragNode(findPositionOnMap(ratio),n);
                    }}
                    onDragMove={() => setCursorPosition(findPositionOnMap(ratio))}
                    onClick={() => {
                        onClickNode(n);
                        originNode || selected.node === n.id ? setSelected({}) : setSelected({node: n.id});
                    }}
                />})
            }
        </Layer>);
}
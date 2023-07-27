import React, {useEffect, useRef, useState} from "react";
import IndoorMap from "./IndoorMap";
import Grid from "@mui/material/Grid";
import HoverableIconButton from "../hoverables/HoverableIconButton";
import {download, MAPS} from "../../services/Client";
import {useParams} from "react-router-dom";

export default function MapView({map, positions,
									gateways, onGatewayClick, dragGateway, onDragGateway,
									beacons, onBeaconClick, dragBeacon, onDragBeacon,
									poi, onPoiClick, dragPoi, onDragPoi,
									events, onEventClick, dragEvent, onDragEvent, resizeEvent, onResizeEvent,
									paths, onNodeClick, onLinkClick, dragNode, onDragNode, originNode,
									customCursor, onBackgroundClick, popup, children}) {

	const stage = useRef();
	const mapContainer = useRef();

	let [scale, setScale] = useState(1);
	let [mapImage, setMapImage] = useState();

	function addZoom() {
		let oldScale = stage.current.scaleX();
		let newScale = oldScale * 1.3;
		stage.current.scale({x: newScale, y: newScale});
		stage.current.batchDraw();
		setScale(newScale);
	}

	function subZoom() {
		let oldScale = stage.current.scaleX();
		let newScale = oldScale / 1.3;
		if (newScale < 0.5) newScale = 0.5;
		stage.current.scale({x: newScale, y: newScale});
		stage.current.batchDraw();
		setScale(newScale);
	}

	function resetZoom() {
		stage.current.scale({x: 1, y: 1});
		stage.current.position({x: 0, y: 0});
		stage.current.batchDraw();
		setScale(1);
	}

	function onWheel({evt}) {
		evt.preventDefault();

		let oldScale = stage.current.scaleX();

		let newScale = evt.deltaY > 0 ? oldScale * 1.1 : oldScale / 1.1;
		if (newScale < 0.5) newScale = 0.5;
		stage.current.scale({x: newScale, y: newScale});

		let mousePointTo = {
			x: stage.current.getPointerPosition().x / oldScale - stage.current.x() / oldScale,
			y: stage.current.getPointerPosition().y / oldScale - stage.current.y() / oldScale
		};
		let newPos = {
			x: -(mousePointTo.x - stage.current.getPointerPosition().x / newScale) * newScale,
			y: -(mousePointTo.y - stage.current.getPointerPosition().y / newScale) * newScale
		};
		stage.current.position(newPos);
		stage.current.batchDraw();
		setScale(newScale);
	}

	useEffect(() => {
		setMapImage("downloading");
		map && download(`${MAPS}/${map}/image`)
			.then(response => setMapImage(URL.createObjectURL(response.data)))
			.catch(e => console.log(e));
	}, [map]);

	let containerWidth = mapContainer.current && mapContainer.current.getBoundingClientRect().width;
	return <Grid container spacing={2} style={{cursor: "url("+ customCursor +"), auto"}}>
		<Grid item sm={8} ref={mapContainer}>
			{
				mapImage && mapImage !== "downloading" &&
				<IndoorMap
					map={map}
					refElem={stage}
					scale={scale}
					mapImage={mapImage}
					containerWidth={containerWidth * 0.95}
					containerHeight={map.width ? containerWidth / map.width * map.height : 600}
					onWheel={onWheel}
					positions={positions}
					gateways={gateways}
					onGatewayClick={e => onGatewayClick(e.currentTarget.attrs, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					beacons={beacons}
					dragBeacon={dragBeacon}
					onDragBeacon = {onDragBeacon}
					onBeaconClick={e => onBeaconClick(e.currentTarget.attrs, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					dragGateway={dragGateway}
					onDragGateway = {onDragGateway}
					poi={poi}
					onPoiClick={e => onPoiClick(e.currentTarget.attrs, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					dragPoi={dragPoi}
					onDragPoi = {onDragPoi}
					events={events}
					dragEvent={dragEvent}
					onDragEvent={onDragEvent}
					resizeEvent={resizeEvent}
					onResizeEvent={onResizeEvent}
					onEventClick={e => onEventClick(e.currentTarget.attrs, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					paths={paths}
					onNodeClick={e => onNodeClick(e, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					dragNode={dragNode}
					originNode={originNode}
					onDragNode={onDragNode}
					onLinkClick={e => onLinkClick(e, stage.current.getPointerPosition().x > containerWidth / 2 ? "left" : "right")}
					onBackgroundClick={onBackgroundClick}
				/>
			}
			{popup}
		</Grid>
		<Grid item sm={4}>
			{
				mapImage && mapImage !== "downloading" &&
				<Grid container direction={"row"} spacing={2}>
					<HoverableIconButton
						onClick={addZoom}
						src={require("../../images/icons/map-zoom-in.svg")}
						hoverSrc={require("../../images/icons/map-zoom-in-over.svg")}
					/>
					<HoverableIconButton
						onClick={subZoom}
						src={require("../../images/icons/map-zoom-out.svg")}
						hoverSrc={require("../../images/icons/map-zoom-out-over.svg")}
					/>
					<HoverableIconButton
						onClick={resetZoom}
						src={require("../../images/icons/map-reset.svg")}
						hoverSrc={require("../../images/icons/map-reset-over.svg")}
					/>
				</Grid>
			}
			{
				mapImage && mapImage !== "downloading" && children
			}
		</Grid>
	</Grid>;


}

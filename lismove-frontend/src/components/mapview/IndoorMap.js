import React,{useContext,useEffect,useRef,useState} from "react";
import {Stage, Layer, Label, Tag, Text} from 'react-konva';
import URLImage from "./URLImage";
import BeaconImage from "./BeaconImage";
import PropTypes from "prop-types";
import UserImage from "./UserImage";
import GatewayImage from "./GatewayImage";
import PoiImage from "./PoiImage";
import PathsLayer from "./PathsLayer";
import { AbilityContext } from '../../services/Can';
import {useSelector} from "react-redux";
import {useHistory,useParams} from "react-router-dom";
import EventsLayer from "./EventsLayer";

export default function IndoorMap({	  map,
	                                  containerWidth, containerHeight,
	                                  refElem,
	                                  mapImage,
	                                  onBackgroundClick,
	                                  onWheel,
	                                  scale,
	                                  beacons, onBeaconClick, dragBeacon, onDragBeacon,
	                                  positions,
	                                  gateways, onGatewayClick, dragGateway, onDragGateway,
									  poi, onPoiClick, dragPoi, onDragPoi,
									  events, onEventClick, onDragEvent, dragEvent, resizeEvent, onResizeEvent,
									  paths, onNodeClick, onLinkClick, dragNode, onDragNode, originNode, originNodeForLinkBetweenFloors
                                  }) {

	const backgroundRef = useRef();
    const eventsLayer = useRef();
	const beaconLayer = useRef();
	const positionLayer = useRef();
	const gatewaysLayer = useRef();
	const poiLayer = useRef();

	let {id} = useParams();
	let [canvas, setCanvas] = useState({});
	let [background, setBackground] = useState({});
	let [selected, setSelected] = useState({});
	let [cursorPosition, setCursorPosition] = useState();
	let history = useHistory();

	let venue = useSelector(store => store.session.venue);
	const ability = useContext(AbilityContext);

	useEffect(() => {
		if (containerWidth && canvas.width !== containerWidth) {
			let newCanvas = Object.assign({}, canvas);
			newCanvas.width = containerWidth;
			newCanvas.height = containerHeight || 600;
			setCanvas(newCanvas);
		}
		setSelected({beacon: parseInt(id)});
		history.replace({pathname: "/maps"});
	}, []);

	const findPositionOnMap = () => {
		let scale = refElem.current.scaleX();
		let {x,y} = refElem.current.getPointerPosition();
		let translate = {
			x:refElem.current.x(),
			y:refElem.current.y()
		};
		return {
			x: (x - translate.x ) / scale / background.ratio,
			y: (y - translate.y ) / scale / background.ratio,
		}
	};

	function setBackgroundImage(backgroundImage) {
		let h = backgroundImage.height;
		let w = backgroundImage.width;
		let ratio = h / w;

		let background = {
			original: {h, w}
		};
		if (h > w) {
			background.height = canvas.height;
			background.width = canvas.height / ratio;
			background.ratio = canvas.height / h;
		} else {
			background.height = ratio * canvas.width;
			background.width = canvas.width;
			background.ratio = canvas.width / w;
		}
		backgroundImage.height = background.height;
		backgroundImage.width = background.width;
		setBackground(background);
		backgroundRef.current && backgroundRef.current.batchDraw();
	}

	function dragBoundFunc(pos) {
		let topLimit = -background.height * 0.9 * scale;
		let bottomLimit = canvas.height * 0.9;
		let leftLimit = -background.width * 0.9 * scale;
		let rightLimit = canvas.width * 0.9;
		let newY, newX;

		if (pos.y < topLimit) newY = topLimit;
		else if (pos.y > bottomLimit) newY = bottomLimit;
		else newY = pos.y;
		if (pos.x < leftLimit) newX = leftLimit;
		else if (pos.x > rightLimit) newX = rightLimit;
		else newX = pos.x;

		return {
			x: newX,
			y: newY
		};
	}

	if (!canvas) return <div/>;

	return (
		<Stage
			ref={refElem}
			width={canvas.width}
			height={canvas.height}
			onMouseMove={() => setCursorPosition(findPositionOnMap(background.ratio))}
			draggable
			dragBoundFunc={pos => dragBoundFunc(pos)}
			onWheel={onWheel}
		>
			<Layer ref={backgroundRef} onClick={() => {
				let newElement = {
					x : findPositionOnMap(background.ratio).x,
					y : findPositionOnMap(background.ratio).y,
					map : map,
				}
				onBackgroundClick(newElement);
				setSelected({});
			}}>
				<URLImage
					src={mapImage}
					onLoad={imageRef => setBackgroundImage(imageRef)}
				/>
			</Layer>
			{background && events && ability.can('read', venue.id + "", 'events') &&
            <Layer ref = {eventsLayer}>
                {events.map(ev =>
                    <EventsLayer
                        event={ev}
                        isSelected={selected.event === ev.id}
                        scale={scale}
                        ratio={background.ratio}
                        onClick={e => {
                            onEventClick(e);
                            selected.event === ev.id ? setSelected({}) : setSelected({event: ev.id});
                        }}
						draggable={dragEvent === ev.id}
						onDragEnd={(xy) => {
							setSelected({});
							onDragEvent(xy, ev)
						}}
						resizable={resizeEvent === ev.id}
						onResizeEnd={(newEvent) => {
							setSelected({});
							onResizeEvent(newEvent);
						}}
						findPositionOnMap={findPositionOnMap}
                    />
                )}
            </Layer>
			}
			{background && beacons && ability.can('read', venue.id + "", 'beacons') &&
			<Layer ref={beaconLayer}>
				{beacons.map(b =>
					<BeaconImage
						selected={selected.beacon === b.id}
						beacon={b}
						key={b.id}
						ratio={background.ratio}
						scale={scale}
						onClick={e => {
							onBeaconClick(e);
							selected.beacon === b.id ? setSelected({}) : setSelected({beacon: b.id});
						}}
						draggable={dragBeacon === b.id}
						onDragEnd={() => {
							setSelected({});
							onDragBeacon(findPositionOnMap(background.ratio), b);
						}}
					/>
				)}
			</Layer>
			}
			{background && gateways && ability.can('read', venue.id + "", 'gateways') &&
			<Layer ref={gatewaysLayer}>
				{gateways.map(g =>
					<GatewayImage
						selected={selected.gateway === g.id}
						gateway={g}
						key={g.id}
						ratio={background.ratio}
						scale={scale}
						onClick={e => {
							onGatewayClick(e);
							selected.gateway === g.id ? setSelected({}) : setSelected({gateway: g.id});
						}}
						draggable={dragGateway === g.id}
						onDragEnd={() => {
							setSelected({});
							onDragGateway(findPositionOnMap(background.ratio), g);
						}}
					/>
				)}
			</Layer>
			}
			{background && poi && ability.can('read', venue.id + "", 'poi') &&
				<Layer ref={poiLayer}>
					{poi.map(p =>
						<PoiImage
							selected={selected.poi === p.id}
							poi={p}
							key={p.id}
							ratio={background.ratio}
							scale={scale}
							onClick={e => {
								onPoiClick(e);
								selected.poi === p.id ? setSelected({}) : setSelected({poi: p.id});
							}}
							draggable={dragPoi === p.id}
							onDragEnd={() => {
								setSelected({});
								onDragPoi(findPositionOnMap(background.ratio), p);
							}}
						/>
					)}
				</Layer>
			}
			{background && paths && ability.can('read', venue.id + "", 'paths') &&
				<PathsLayer
					selectedMap={map}
					paths={paths}
					scale={scale}
					ratio={background.ratio}
					onClickNode={onNodeClick}
					onClickLink={onLinkClick}
					setSelected={setSelected}
					selected={selected}
					dragNode={dragNode}
					onDragNode={onDragNode}
					findPositionOnMap={findPositionOnMap}
					originNode={originNode}
					cursorPositionLink={cursorPosition}
					originNodeForLinkBetweenFloors={originNodeForLinkBetweenFloors}
				/>
			}
			{background && positions &&
			<Layer ref={positionLayer}>
				{positions.map(p =>
					<>
						<UserImage
							user={p}
							key={p.id}
							ratio={background.ratio}
							scale={scale}
						/>
						<Label
							x={p.x * background.ratio}
							y={p.y * background.ratio + 15}
							scale={scale}
						>
							<Tag
								fill='black'
								pointerDirection='up'
								pointerWidth={5}
								pointerHeight={5}
								cornerRadius={4}
								opacity={0.5}
							/>
							<Text
								text={p.beacon}
								fontFamily='Calibri'
								fontSize={14}
								fill='white'
								padding={5}
							/>
						</Label>
					</>
				)}
			</Layer>
			}
		</Stage>
	);
}

IndoorMap.propTypes =
	{
		beacons: PropTypes.array,
		positions: PropTypes.array,
		containerWidth: PropTypes.number,
		mapImage: PropTypes.string.isRequired,
		scale: PropTypes.number,
		refElem: PropTypes.object,
		onBeaconClick: PropTypes.func,
		onBackgroundClick: PropTypes.func
	};


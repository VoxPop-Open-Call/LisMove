import React from "react";
import URLImage from "./URLImage";

const image = require("../../images/icons/on-map-beacon.svg");
const imageSelected = require("../../images/icons/on-map-beacon-over.svg");

export default function BeaconImage({beacon, draggable, onClick, onDragEnd, onDragStart, selected, scale, ratio}) {
	return <URLImage
		src={selected ? imageSelected : image}
		scale={scale}
		imgProps={{
			id: beacon.id,
			x: beacon.x * ratio,
			y: beacon.y * ratio,
			onClick,
			centered: true,
			draggable,
			onDragEnd,
			onDragStart
		}}
	/>;

}

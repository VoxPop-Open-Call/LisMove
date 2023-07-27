import React from "react";
import URLImage from "./URLImage";

const image = require("../../images/icons/on-map-cloud-beacon.svg");
const imageSelected = require("../../images/icons/on-map-cloud-beacon-over.svg");

export default function GatewayImage({gateway, draggable, onClick, onDragEnd, onDragStart, ratio, scale, selected}) {
	return <URLImage
		scale={scale}
		src={selected ? imageSelected : image}
		imgProps={{
			id: gateway.id,
			x: gateway.x * ratio,
			y: gateway.y * ratio,
			centered: true,
			draggable: draggable,
			onClick,
			onDragEnd,
			onDragStart
		}}

	/>;
}

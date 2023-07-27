import React from "react";
import URLImage from "./URLImage";

const image = require("../../images/icons/on-map-user.svg");
const imageSelected = require("../../images/icons/on-map-user-over.svg");

export default function UserImage({user, draggable, onClick, onDragEnd, onDragStart, ratio, scale, selected}) {
	return <URLImage
		scale={scale}
		src={selected ? imageSelected : image}
		imgProps={{
			id: user.id,
			x: user.x * ratio,
			y: user.y * ratio,
			centered: true,
			draggable: draggable,
			onClick,
			onDragEnd,
			onDragStart
		}}

	/>;

}

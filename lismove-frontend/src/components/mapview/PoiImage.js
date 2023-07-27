import React from "react";
import URLImage from "./URLImage";

const image = require("../../images/icons/on-map-poi.svg");
const imageSelected = require("../../images/icons/on-map-poi-over.svg");

export default function GatewayImage({poi, draggable, onClick, onDragEnd, onDragStart, ratio, scale, selected}) {
    return <URLImage
        scale={scale}
        src={selected ? imageSelected : image}
        imgProps={{
            id: poi.id,
            x: poi.x * ratio,
            y: poi.y * ratio,
            centered: true,
            draggable: draggable,
            onClick,
            onDragEnd,
            onDragStart
        }}

    />;
}
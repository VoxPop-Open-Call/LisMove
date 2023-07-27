import React, {useState} from "react";
import IconButton from "@mui/material/IconButton";

export default function HoverableIconButton({src, hoverSrc, onClick}) {
	let [hover, setHover] = useState(false);
	let icon;
	if (hover) {
		icon = hoverSrc;
	} else {
		icon = src;
	}
	return (
        <IconButton
            onMouseOver={() => setHover(true)}
            onMouseOut={() => setHover(false)}
            onClick={onClick}
            size="large">
			<img
				onMouseOver={() => setHover(true)}
				src={icon}
			/>
		</IconButton>
    );
}

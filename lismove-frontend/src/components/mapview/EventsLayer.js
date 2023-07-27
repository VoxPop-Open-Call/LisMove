import React,{useState} from "react";
import {Circle,Transformer} from "react-konva";


export default function EventsLayer({event, ratio, scale, isSelected, onClick, draggable, onDragEnd, resizable, onResizeEnd, findPositionOnMap}){
    let [difference, setDifference] = useState({});

    const shapeRef = React.useRef();
    const trRef = React.useRef();

    React.useEffect(() => {
        if (resizable) {
            // we need to attach transformer manually
            trRef.current.nodes([shapeRef.current]);
            trRef.current.getLayer().batchDraw();
        }
    }, [resizable]);


    return (
        <>
            <Circle
                id={event.id}
                ref={shapeRef}
                x={event.x * ratio}
                y={event.y * ratio}
                radius={event.radius}
                strokeWidth={3 / scale}
                stroke={isSelected ? "rgb(34,183,0)" : "#acff01"}
                fill={isSelected ? "rgb(34,183,0,0.1)" : "rgba(172,255,1,0.1)"}
                onClick={onClick}
                strokeScaleEnabled={false}
                draggable={draggable}
                onDragStart={() => {
                    let xy = findPositionOnMap();
                    setDifference({x: (xy.x - event.x), y: (xy.y - event.y)});
                }}
                onDragEnd={() => {
                    let xy = findPositionOnMap();
                    xy.x = xy.x - difference.x;
                    xy.y = xy.y - difference.y;
                    onDragEnd(xy);
                }}
                onTransformEnd={() => {
                    const node = shapeRef.current;
                    const scaleX = node.scaleX();

                    node.scaleX(1);
                    node.scaleY(1);

                    let width = node.width() * scaleX;

                    let newRadius = width/2;

                    onResizeEnd({
                       ...event,
                        radius: newRadius
                    });
                }}
            />
            {resizable && (
                <Transformer
                    ref={trRef}
                    ignoreStroke={true}
                    centeredScaling={true}
                    rotateEnabled={false}
                    enabledAnchors={['bottom-right','bottom-left','top-right','top-left']}
                    boundBoxFunc = {function (oldBoundBox, newBoundBox) {
                        if(newBoundBox.width < 30 || newBoundBox.width > 300) return oldBoundBox;
                        return newBoundBox
                    }}
                />
            )}
        </>
    );

}
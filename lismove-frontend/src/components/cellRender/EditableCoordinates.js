import React,{useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import HoverableButton from "../layout/HoverableButton";
import EditIcon from "@mui/icons-material/Edit";
import EditCoordinatesModal from "../modals/EditCoordinatesModal";

const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    }
}));

export default function EditableCoordinates({latitude, longitude, onChange}){
    let classes = useStyles();
    let [modal, setModal] = useState(false);

    return (
        <div>
            <HoverableButton
                item={{
                    icon: <EditIcon/>,
                    name: latitude || longitude ? `${latitude || ''} - ${longitude || ''}` : ""
                }}
                onClick={() => setModal(true)}
                classes={{root: classes.root}}
            />
            <EditCoordinatesModal open={modal} onClose={() => setModal(false)} defaultLat={latitude} defaultLng={longitude} onSubmit={onChange}/>
        </div>
    );
}

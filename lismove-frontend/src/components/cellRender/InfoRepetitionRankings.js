import React,{useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import HoverableButton from "../layout/HoverableButton";
import InfoIcon from "@mui/icons-material/Info";
import PreviewRepetitionModal from "../modals/PreviewRepetitionModal";

const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    }
}));

export default function InfoRepetitionRankings ({params}){

    let classes = useStyles();
    let [modal, setModal] = useState(false);

    if(!params.value) return "";
    return (
        <div>
            <HoverableButton
                item={{
                    icon : <InfoIcon/>,
                    name : params.value || ""
                }}
                onClick={() => setModal(true)}
                classes={{root : classes.root}}
            />
            <PreviewRepetitionModal open={!!modal} onClose={() => setModal(false)} values={params.row}/>
        </div>
    );
}
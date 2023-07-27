import Avatar from "@mui/material/Avatar";
import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import HoverableButton from "../layout/HoverableButton";
import EditIcon from "@mui/icons-material/Edit";
import EditAvatarModal from "../modals/EditAvatarModal";


const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    }
}));

export default function RenderCellAvatar({params,saveEdit, folder, label, prefix, infoMessage, warningMessage}) {

    let classes = useStyles();
    let [modal, setModal] = useState(false);


    return (
        <div>
            {
                saveEdit ?
                    <div>
                        <HoverableButton
                            item={{
                                icon: <EditIcon/>,
                                name: params.value ? <Avatar src={params.value}/>  : ""
                            }}
                            onClick={() => setModal(true)}
                            classes={{root: classes.root}}
                        />
                    </div>
                : params.value ? <Avatar src={params.value}/> : ""
            }
            <EditAvatarModal open={modal} onClose={() => setModal(false)}
                             onSubmit={(newValue) => saveEdit(params.id, params.field, newValue)}
                             folder={folder} label={label} prefix={prefix} infoMessage={infoMessage} warningMessage={warningMessage}/>
        </div>
    );

}
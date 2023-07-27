import React, {useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import HoverableButton from "../layout/HoverableButton";
import EditIcon from "@mui/icons-material/Edit";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import BaseModal from "../modals/BaseModal";
import TextInput from "../forms/TextInput";
import MenuItem from "@mui/material/MenuItem";

const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    }
}));

/**
 * mostra un pallino verde o rosso in base al boolean
 * @param params
 * @param inputLabel
 * @param saveEdit {function(id,field,value,row)} Callback function per salvare la modifica
 * @returns {JSX.Element|string}
 * @constructor
 */
export default function RenderBoolean({params, inputLabel, saveEdit}) {
    let classes = useStyles();
    let [modal, setModal] = useState(false);
    let [value, setValue] = useState(params.value);
    let color = (params.value || false).toString() === "true" ? "#43ff00" : "#ff0000";

    function onChange(newValue) {
        setValue(newValue);
    }

    function submit() {
        saveEdit(params.row.id, params.field, value, params.row)
        setModal(false);
    }

    const options = [
        {
            id: "true",
            name: <FiberManualRecordIcon style={{color: "#43ff00"}}/>
        },
        {
            id: "false",
            name: <FiberManualRecordIcon style={{color: "#ff0000"}}/>
        }
    ];

    if (!saveEdit) {
        return params.value != null ? <FiberManualRecordIcon style={{color: color}}/> : ""
    }

    return (
        <div>
            <HoverableButton
                item={{
                    icon: <EditIcon/>,
                    name: params.value != null ? <FiberManualRecordIcon style={{color: color}}/> : ""
                }}
                onClick={() => setModal(true)}
                classes={{root: classes.root}}
            />
            <BaseModal open={modal} onClose={() => setModal(false)} onSave={submit}>
                <TextInput value={value} options={options} label={inputLabel} onTextChange={onChange} select={true}>
                    {options.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                </TextInput>
                <div style={{height: "2rem", width: "15rem"}}/>
            </BaseModal>
        </div>
    );

}

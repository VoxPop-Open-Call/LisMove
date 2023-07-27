import React, {useEffect, useState} from "react";
import BaseModal from "./BaseModal";
import TextInput from "../forms/TextInput";
import Grid from "@mui/material/Grid";
import dayjs from "dayjs";
import InputAdornment from "@mui/material/InputAdornment";
import MenuItem from "@mui/material/MenuItem";
import { Alert } from '@mui/material';

export default function EditFieldModal({open, onClose, onSubmit, defaultValue, title = "Label", type, required, min, max, step, measureUnit, options, infoMessage, warningMessage}) {

	const getDefaultValue = () => {
		if(type === "date") return dayjs(defaultValue).format("YYYY-MM-DD");
		return defaultValue;
	}

	let [value, setValue] = useState(getDefaultValue());
	let [error, setError] = useState(null);

	useEffect(() => {
		if(open) setValue(getDefaultValue());
	}, [open]);

	function onChange(newLabel) {
		setValue(newLabel);
		setError(null);
	}

	function submit() {
		if(required && (value === null || value === undefined)) {
			setError("Valore Obbligatorio")
			return
		}
		onSubmit(value);
		setError(null);
		onClose();
	}

	return <BaseModal open={open} onClose={onClose} onSave={submit}>
		{infoMessage &&
			<Grid item xs={12} style={{marginBottom: "0.5rem"}}>
				<Alert severity="info">{infoMessage}</Alert>
			</Grid>
		}
		{warningMessage &&
			<Grid item xs={12} style={{marginBottom : "0.5rem"}}>
				<Alert severity="warning">{warningMessage}</Alert>
			</Grid>
		}
		<Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}}>
			<Grid item xs={12}>
				<TextInput label={title} value={value} onTextChange={onChange} select={type === "select"}
						   startAdornment={measureUnit && <InputAdornment position="start">{measureUnit}</InputAdornment>}
						   error={error} type={type} required={required} min={min} max={max} step={step}>
					{options && options.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
				</TextInput>
			</Grid>
			<div style={{width: "15rem"}}/>
		</Grid>
	</BaseModal>
}

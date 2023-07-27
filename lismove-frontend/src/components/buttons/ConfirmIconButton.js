import React, {useState} from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import {IconButton} from "@mui/material";

export default function ConfirmIconButton({title, text, onConfirm, cancelText = "Cancella", confirmText = "Conferma", children}) {

	let [open, setOpen] = useState(false);

	let handleClose = () => setOpen(false);
	let handleConfirm = () => {
		setOpen(false)
		onConfirm()
	}

	return <>
		<IconButton onClick={() => setOpen(true)} size="large">
			{children}
		</IconButton>

		<Dialog
			open={open}
			onClose={handleClose}
		>
			<DialogTitle>{title}</DialogTitle>
			{text &&
			<DialogContent>
				<DialogContentText>
					{text}
				</DialogContentText>
			</DialogContent>}
			<DialogActions>
				<Button onClick={handleClose} color="secondary">
					{cancelText}
				</Button>
				<Button onClick={handleConfirm} color="primary" autoFocus>
					{confirmText}
				</Button>
			</DialogActions>
		</Dialog>
	</>;
}

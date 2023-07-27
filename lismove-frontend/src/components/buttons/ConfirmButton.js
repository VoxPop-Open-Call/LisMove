import React, {useState} from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";

export default function ConfirmButton({buttonProps = {}, title, text, onConfirm, cancelText = "Cancel", confirmText = "Confirm", children}) {
	let [open, setOpen] = useState(false);
	let handleClose = () => setOpen(false);
	return <div><Button
		onClick={() => setOpen(true)}
		{...buttonProps}
	>{children}</Button>
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
				<Button onClick={onConfirm} color="primary" autoFocus>
					{confirmText}
				</Button>
			</DialogActions>
		</Dialog>
	</div>;
}

import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import DialogTitle from "@mui/material/DialogTitle";
import Dialog from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import IconButton from "@mui/material/IconButton";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";

const useStyles = makeStyles((theme) => ({
	paper: {
		backgroundColor: theme.palette.primary.dark,
		borderRadius: 0
	},
	root: {
		"&::-webkit-scrollbar": {
			width: 5,
		},
		"&::-webkit-scrollbar-thumb": {
			backgroundColor: theme.palette.secondary.main,
			borderRadius: "10px"
		},
	},
}));

export default function BaseModal({open, onClose, onSave, children, error, iconButton, onClickButton, fullWidth}) {

	const classes = useStyles();

	return (
        <Dialog open={open} onClose={onClose} classes={{paper: classes.paper}} fullWidth={fullWidth} maxWidth={fullWidth && "lg"}>
			<DialogTitle style={{textAlign: "right"}}>
				{
					iconButton &&
					<IconButton onClick={onClickButton} size="large">
						{iconButton}
					</IconButton>
				}
				{
					onSave &&
					<IconButton onClick={onSave} size="large">
						<CheckIcon/>
					</IconButton>
				}
				<IconButton onClick={onClose} size="large">
					<CloseIcon/>
				</IconButton>
			</DialogTitle>
			<DialogContent className={classes.root}>
				{error && <div>{error}</div>}
				{children}
			</DialogContent>
		</Dialog>
    );
}

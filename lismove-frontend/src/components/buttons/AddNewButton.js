import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import HoverableButton from "../layout/HoverableButton";
import AddIcon from '@mui/icons-material/Add';

const useStyles = makeStyles((theme) => ({
	addButton: {
		color: theme.palette.primary.dark,
		marginRight: "2rem"
	},
	addText: {
		fontWeight: 700,
	}
}));

export default function AddNewButton({onClick, text}) {
	const classes = useStyles();
	return <HoverableButton
		item={{
			icon: <AddIcon/>,
			name: text
		}}
		onClick={onClick}
		classes={{root: classes.addButton, text: classes.addText}}
	/>;
}

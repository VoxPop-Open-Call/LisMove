import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import {Link} from "react-router-dom";
import {ListItemAvatar} from "@mui/material";

const useStyles = makeStyles(theme => ({
	root: {
		display: "flex",
		justifyContent: "center",
		padding:theme.spacing(2),
		color:"white",
		'&:hover': {
			color:theme.palette.secondary.main,
			backgroundColor: theme.palette.primary.main
		}
	},
	selected: {
		backgroundColor: theme.palette.secondary.main,
		'&:hover': {
			backgroundColor: theme.palette.primary.main
		}
	},
	imageIcon: {
		color: theme.palette.primary.contrastText
	},
	iconRoot: {
		display: "flex"
	},
	imageText: {
		margin:-10,
		color: theme.palette.primary.contrastText
	}
}));

export default function HoverableListItem({item, open}) {

	let classes = useStyles();

	return (
        <Link to={item.url} style={{textDecoration: "none"}}>
			<ListItem button>
				<ListItemAvatar className={classes.imageIcon}>{item.icon}</ListItemAvatar>
				{
					open && <ListItemText className={classes.imageText}>{item.name}</ListItemText>
				}
			</ListItem>
		</Link>

	);
}

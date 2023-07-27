import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import List from "@mui/material/List";
import HoverableListItem from "./HoverableListItem";
import useMediaQuery from "@mui/material/useMediaQuery";
import {useTheme} from "@mui/material/styles";
import clsx from "clsx";
import IconButton from "@mui/material/IconButton";
import Drawer from "@mui/material/Drawer";

const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
	menuButton: {
		marginRight: 36,
	},
	hide: {
		display: 'none',
	},
	drawer: {
		zIndex: -1,
		backgroundColor: theme.palette.primary.dark,
		overflowX: 'hidden',
		flexShrink: 0,
		whiteSpace: 'nowrap'
	},
	list: {
		backgroundColor: theme.palette.primary.dark,
		overflowX: 'hidden',
	},
	drawerOpen: {
		overflowX: 'hidden',
		[theme.breakpoints.up('md')]: {
			width: drawerWidth,
			transition: theme.transitions.create('width', {
				easing: theme.transitions.easing.sharp,
				duration: theme.transitions.duration.enteringScreen,
			})
		}
	},
	drawerClose: {
		overflowX: 'hidden',
		[theme.breakpoints.up('md')]: {
			transition: theme.transitions.create('width', {
				easing: theme.transitions.easing.sharp,
				duration: theme.transitions.duration.leavingScreen,
			}),
			width: theme.spacing(8),
		}
	},
	toolbar: {
		display: 'flex',
		alignItems: 'center',
		justifyContent: 'flex-start',
		padding: theme.spacing(0, 1),
		// necessary for content to be below app bar
		...theme.mixins.toolbar,
	},
}));

export default function Sidebar({open, setOpen, items}) {
	const theme = useTheme();
	const classes = useStyles();
	const isDesktop = useMediaQuery(theme.breakpoints.up('md'));

	const handleDrawerClose = () => {
		setOpen(false);
	};

	return (
		<Drawer
			variant={isDesktop ? "permanent" : "temporary"}
			anchor={"top"}
			open={open}
			onClose={handleDrawerClose}
			className={clsx(classes.drawer, {
				[classes.drawerOpen]: open,
				[classes.drawerClose]: !open,
			})}
			classes={{
				paper: clsx({
					[classes.drawerOpen]: open,
					[classes.drawerClose]: !open,
				}),
			}}
		>
			<div className={classes.toolbar}>
				<IconButton onClick={handleDrawerClose} size="large">

				</IconButton>
			</div>
			<List className={clsx(classes.list, {
				[classes.drawerOpen]: open,
				[classes.drawerClose]: !open,
			})}>
				{items.map(i => <HoverableListItem item={i} key={i.url} open={open}/>)}
			</List>
		</Drawer>
    );
}

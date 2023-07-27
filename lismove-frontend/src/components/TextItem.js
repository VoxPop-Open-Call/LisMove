import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import clsx from "clsx";
import {Typography} from "@mui/material";

const useStyles = makeStyles(theme => ({
	box: {
		width: "100%",
		height: theme.spacing(4),
		paddingBottom: theme.spacing(1),
		paddingLeft: theme.spacing(1),
		paddingRight: theme.spacing(1),
		margin: theme.spacing(0.5),
		borderBottom: `${theme.palette.secondary.main} 1px solid`,
		color: theme.palette.primary.dark,
		fontWeight: "bold",
		flexGrow: 1,
	},
	label: {
		paddingTop: theme.spacing(2),
		paddingLeft: theme.spacing(1),
		color: theme.palette.text.primary,
	}
}));

export function TextItem({value, label, xs = 6, md = 6, className}) {
	let innerClasses = useStyles();
	return <Grid item xs={xs} md={md}>
		<Grid container alignItems={"flex-end"} style={{display: "flex"}}>

                <Typography variant="caption" className={innerClasses.label}>{label}</Typography>
                <Box xs={xs} md={md} className={clsx(innerClasses.box, className)}>{value}</Box>

		</Grid>
	</Grid>;
}

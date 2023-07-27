import React, {useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import clsx from "clsx";
import  PropTypes from "prop-types";
import Box from "@mui/material/Box";
import AddNewButton from "../buttons/AddNewButton";

const useStyles = makeStyles((theme) => ({
	buttonRow: {
		flexGrow:2,
		marginTop: "auto",
		marginBottom: "auto",
		overflowX: "scroll",
		"-ms-overflow-style": "none",  // IE 10+
		scrollbarWidth: "none",  // Firefox
		"&::-webkit-scrollbar": {
			display: "none" // Safari and Chrome
		},
		"& button": {
			marginRight: "0.5rem"
		}
	},
	button: {
		backgroundColor:theme.palette.primary.light,
		color: theme.palette.primary.dark,
		padding: "0.75rem 1rem",
		verticalAlign: "middle",
		textAlign:"center",
		fontSize: "0.875rem",
		lineHeight: "1.25",
		textTransform: "upperCase",
		fontWeight: 700,
		cursor: "pointer",
		marginRight: "0.3rem",
		marginBottom: "0.3rem",
		minWidth:"5rem",
		'&:hover': {
			backgroundColor:theme.palette.primary.main,
			color: "#FFF"
		}
	},
	selected: {
		backgroundColor:theme.palette.secondary.main,
		color: "#FFF"
	},
	addButton: {
		color: theme.palette.primary.dark,
		marginRight: "2rem"
	},
	addText: {
		fontWeight: 700,
	}
}));

export default function FilterAndSearchBar({addElement, allButton, filters = [], onFilter, onSearch, selected}) {

	const classes = useStyles();
	let [search, setSearch] = useState(null);

	return (
        <Grid container alignItems={"center"}>
			<Grid item>
				<Grid container>
					{
						allButton &&
						<Box
							onClick={() => {
								onFilter && onFilter(null);
							}}
							className={clsx(classes.button, {[classes.selected]:selected === null})}
						>
							All
						</Box>
					}
					{
						filters.map(({id, name}) =>
							<Box
								onClick={() => {
									onFilter && onFilter(id);
								}}
								className={clsx(classes.button, {[classes.selected]:selected === id})}
								key={id}
							>
								{name}
							</Box>
						)
					}
				</Grid>
			</Grid>
			<div style={{flexGrow:1}}/>
			<Grid item>
			{
				addElement && <AddNewButton onClick={addElement}/>
			}
			</Grid>
			<Grid item>
			{ onSearch &&
			<TextField
				margin={"dense"}
				label="Search"
				variant="outlined"
				value={search}
				onChange={({target}) => {
					setSearch(target.value);
					onSearch(target.value);
				}}/>
			}
			</Grid>
		</Grid>
	);
}

FilterAndSearchBar.propTypes = {
	filters: PropTypes.array.isRequired,
}

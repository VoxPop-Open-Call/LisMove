import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import withStyles from '@mui/styles/withStyles';
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import {NativeSelect} from "@mui/material";
import InputBase from "@mui/material/InputBase";

const BootstrapInput = withStyles((theme) => ({
	root: {
		'label + &': {
			marginTop: theme.spacing(0.5),
		},
	},
	input: {
		borderRadius: 4,
		paddingLeft: "1rem",
		position: 'relative',
		backgroundColor: theme.palette.background.paper,
		border: '1px solid #ced4da',
		fontSize: 14,
		transition: theme.transitions.create(['border-color', 'box-shadow']),
		// Use the system font instead of the default Roboto font.
		fontFamily: [
			'-apple-system',
			'BlinkMacSystemFont',
			'"Segoe UI"',
			'Roboto',
			'"Helvetica Neue"',
			'Arial',
			'sans-serif',
			'"Apple Color Emoji"',
			'"Segoe UI Emoji"',
			'"Segoe UI Symbol"',
		].join(','),
		'&:focus': {
			borderRadius: 4,
			borderColor: '#290e08',
			boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
		},
	},
}))(InputBase);

const useStyles = makeStyles((theme) => ({
	root: {
		color: theme.palette.primary.main,
		'& label': {
			color: theme.palette.primary.main,
			fontStyle: "italic",
			'&.MuiInputLabel-shrink': {
				fontStyle: "normal",
			},
			'&.Mui-focused': {
				color: theme.palette.secondary.main,
			},
			'&:hover': {
				color: theme.palette.secondary.main,
			},
		},
		'& .MuiOutlinedInput-root': {
			borderRadius: 0,
			'& fieldset': {
				borderColor: theme.palette.primary.main,
			},
			'&:hover fieldset': {
				borderColor: theme.palette.secondary.main,
			},
			'&.Mui-focused fieldset': {
				borderColor: theme.palette.secondary.main,
				borderWidth: "3px"
			},
		},
	},
	input: {
		minWidth: "11rem",
		height: "3vh",
		color: theme.palette.primary.main,
		'&:focused': {
			borderColor: theme.palette.secondary.main,
		},
		'&:hover': {
			color: theme.palette.secondary.main,
		},
		'&::placeholder': { /* Chrome, Firefox, Opera, Safari 10.1+ */
			color: theme.palette.secondary.main,
			opacity: 1 /* Firefox */
		},
		'&:-ms-input-placeholder': { /* Internet Explorer 10-11 */
			color: theme.palette.secondary.main,
		},
		'&::-ms-input-placeholder': { /* Microsoft Edge */
			color: theme.palette.secondary.main,
		}
	},
	item: {
		color: theme.palette.primary.main,
		'&:hover': {
			color: theme.palette.secondary.main,
		},
	}
}));

export default function SelectInput({label, value, onChange, options, defaultValue, children, rootClass, inputClass, labelColor = "primary"}) {
	let innerClasses = useStyles();
	return (
        <FormControl variant="outlined" className={rootClass || innerClasses.root}>
			<InputLabel id="demo-simple-select-outlined-label" color={labelColor}>{label}</InputLabel>
			<NativeSelect
				id="demo-simple-select-outlined"
				value={value}
				onChange={event => onChange(event.target.value)}
				label={label}
				inputProps={{
					className: inputClass || innerClasses.input
				}}
				input={<BootstrapInput />}
			>
				<option aria-label="None" value="" />
				{children}
			</NativeSelect>
		</FormControl>
    );
}

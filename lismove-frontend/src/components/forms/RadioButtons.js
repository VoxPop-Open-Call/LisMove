import { FormControlLabel, FormLabel, Radio, RadioGroup } from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import FormControl from "@mui/material/FormControl";
import React from "react";

const useStyles = makeStyles(theme => ({
    radioLabel: {
        color: theme.palette.secondary.main,
        '&.Mui-focused' : {
            color : theme.palette.secondary.main,
        }
    },
    radioButton: {
        "&.Mui-checked": {
            color: theme.palette.primary.main
        },
    },
}));

export default function RadioButtons({value, label, onChange, options, row}){

    const classes = useStyles();

    return <FormControl component="fieldset">

            <FormLabel component="legend" className={classes.radioLabel}>{label}</FormLabel>
            <RadioGroup value={value} onChange={(event) => onChange(event.target.value)} row={row}>

                {options.map(option => <FormControlLabel value={option.value}
                                      control={<Radio className={classes.radioButton}/>}
                                      label={option.label}/>
                )}

        </RadioGroup>
    </FormControl>
}
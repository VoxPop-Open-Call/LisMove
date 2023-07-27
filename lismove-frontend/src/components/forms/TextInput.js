import React, {useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import TextField from "@mui/material/TextField";
import InputAdornment from "@mui/material/InputAdornment";
import {Visibility, VisibilityOff} from "@mui/icons-material";

export default function TextInput({label, value, onTextChange, required, error, readOnly, type = "text", step, min, max, noHelperText, startAdornment, helperText, multiline, rows, select,
                                      InputLabelProps, showPasswordIcon, autoComplete = 'new-password', children, color = 'light', InputProps, onClick, autoFocus, className}) {

    const useStyles = makeStyles(theme => ({
        root: {
            '& label': {
                color: color === 'light' ? theme.palette.primary.light : theme.palette.secondary.main,
                fontStyle: "italic",
                '&.MuiInputLabel-shrink': {
                    fontStyle: "normal"
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
                    borderColor: color === 'light' ? theme.palette.primary.light : theme.palette.secondary.main,
                },
                '&:hover fieldset': {
                    borderColor: theme.palette.secondary.main,
                },
                '&.Mui-focused fieldset': {
                    borderColor: theme.palette.secondary.main,
                    borderWidth: "3px"
                },
            },
            "& .Mui-error": {
                color: color === 'light' ? theme.palette.error.light : theme.palette.error.main,
                '& .MuiOutlinedInput-notchedOutline': {
                    borderColor:  color === 'light' ? theme.palette.error.light : theme.palette.error.main,
                }
            },
        },
        input: {
            color: color === 'light' ? theme.palette.primary.light : theme.palette.secondary.main,
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
        colorGrey:{
            color: 'rgba(0, 0, 0, 0.54)'
        }
    }));


    const classes = useStyles();
    let [showPassword, setShowPassword] = useState(false);
    const preventLoseFocus = (event) => {
        event.preventDefault()
    }
    return (
        <TextField
            className={className}
            variant="outlined"
            fullWidth
            classes={{root: classes.root}}
            label={label}
            value={value}
            inputProps={{
                className: classes.input,
                readOnly,
                type: showPassword ? "text" : type,
                step, min, max,
                autoComplete: autoComplete,
            }}
            InputProps={InputProps || {
                endAdornment: (showPasswordIcon &&
                    <InputAdornment position="end">
                        {showPassword ?
                            <Visibility onClick={() => setShowPassword(false)} onMouseUp={preventLoseFocus}
                                        onMouseDown={preventLoseFocus}/> :
                            <VisibilityOff onClick={() => setShowPassword(true)} onMouseUp={preventLoseFocus}
                                           onMouseDown={preventLoseFocus}/>}
                    </InputAdornment>),
                startAdornment
            }}
            onChange={({target}) => onTextChange(target.value)}
            required={required}
            multiline={multiline}
            rows={rows}
            error={error}
            helperText={!noHelperText && (<>{helperText && <span className={classes.colorGrey}>{helperText}</span>} {helperText && <br/>} {error || ''}</>)}
            select={select}
            InputLabelProps={InputLabelProps}
            onClick={onClick}
            autoFocus={autoFocus}
        >
            {children}
        </TextField>
    );
}

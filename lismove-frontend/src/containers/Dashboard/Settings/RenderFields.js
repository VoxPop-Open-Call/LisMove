import { FormControlLabel, Switch } from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import RadioButtons from "../../../components/forms/RadioButtons";
import {CustomTooltip} from "../../../components/forms/CustomTooltip";
import TextInput from "../../../components/forms/TextInput";
import InputAdornment from "@mui/material/InputAdornment";
import React from "react";

const useStyles = makeStyles(theme => ({
    switch: {
        paddingLeft: theme.spacing(2)
    }
}));

export default function RenderFields({fields, values, error, onTextChange}){

    let classes = useStyles();

    return <>
        {
            fields.map(field => {
                if((field.condition1 === undefined || field.condition1 === true) && (field.condition2 === undefined || field.condition2 === true)) {

                    if(field.type === "switch")
                        return <Grid container>
                            <Grid item xs={field.xs}>
                            <FormControlLabel
                                    className={classes.switch}
                                    control={<Switch checked={field.control} onChange={field.onChange || onTextChange(field.name)} name={field.name}
                                                     color="primary"/>}
                                    label={field.label}
                                />
                            </Grid>
                        </Grid>

                    if(field.type === "radio")
                        return <Grid item xs={field.xs}>
                            <RadioButtons label={field.label} value={values[field.name]} row
                                          onChange={field.onChange || onTextChange(field.name)}
                                          options={field.options}/>
                        </Grid>

                    return <CustomTooltip title={field.tooltip}>
                        <Grid item xs={field.xs}>
                            <TextInput label={field.label}
                                       type={field.type} min={field.min} max={field.max} required={field.required} step={field.step}
                                       startAdornment={field.startAdornment && <InputAdornment position="start">{field.startAdornment}</InputAdornment>}
                                       value={values[field.name]} color={"dark"}
                                       onTextChange={field.onChange || onTextChange(field.name)}
                                       error={error[field.name]}
                                       InputLabelProps={{shrink : true}}/>
                        </Grid>
                    </CustomTooltip>
                }
            })
        }
    </>
}
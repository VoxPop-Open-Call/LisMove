import Grid from "@mui/material/Grid";
import makeStyles from '@mui/styles/makeStyles';
import SelectInput from "../forms/SelectInput";
import React from "react";
import BaseModal from "./BaseModal";
import {useGetCities} from "../../services/ContentManager";

const useStyles = makeStyles((theme) => ({
    root: {
        '& .MuiOutlinedInput-root': {
            borderRadius: 0,
            '& fieldset': {
                borderColor: theme.palette.secondary.main,
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
        color: theme.palette.primary.contrastText,
        backgroundColor: theme.palette.primary.dark,
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
    grid: {
        padding: theme.spacing(2)
    }
}));

export default function SelectCitiesModal({open, onClose, onSubmit}){

    let {cities = []} = useGetCities();
    let classes = useStyles();
    const [selectedCity, setSelectedCity] = React.useState("");

    const close = () => {
        setSelectedCity("")
        onClose()
    }

    const save = () => {
        setSelectedCity("")
        onSubmit(selectedCity)
    }

    return (
        <BaseModal open={open} onClose={close} onSave={save}>

            <Grid container justifyContent={"center"} className={classes.grid}>

                <SelectInput
                    value={selectedCity}
                    label="Seleziona Comune"
                    onChange={city => setSelectedCity(city)}
                    labelColor="secondary"
                    inputClass={classes.input}
                    rootClass={classes.root}>

                    {cities.map(o => <option key={o.istatId} value={o.istatId}>{o.city}</option>)}

                </SelectInput>

            </Grid>

        </BaseModal>
    );
}
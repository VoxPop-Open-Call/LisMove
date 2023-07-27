import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import Grid from "@mui/material/Grid";
import TextInput from "../forms/TextInput";
import MenuItem from "@mui/material/MenuItem";
import {sessionType} from "../../constants/sessionType";
import FormControl from "@mui/material/FormControl";
import {FormControlLabel,FormLabel,Radio,RadioGroup} from "@mui/material";
import DatePicker from '@mui/lab/DatePicker';
import dayjs from "dayjs";

const genderOptions = [
    {
        id: "M",
        name: "Maschio"
    },
    {
        id: "F",
        name: "Femmina"
    }
]

const repeatType = {
    NONE: "0",
    MONTH: "1",
    CUSTOM: "2"
}

const useStyles = makeStyles((theme) => ({
    radioLabel: {
        color: theme.palette.primary.light,
        display: 'flex',
        alignContent: "space-between",
        '&.Mui-focused': {
            color: theme.palette.primary.light
        }
    },
    radioButton: {
        color: theme.palette.primary.light
    },
    previewTitle: {
        color: theme.palette.primary.light,
        height: "2.5rem",
        "white-space": "pre-line",
    },
    preview: {
        color: theme.palette.primary.light,
        "white-space": "pre-line",
        maxHeight: "15vh",
        overflow: "auto",
        "&::-webkit-scrollbar": {
            width: 5,
        },
        "&::-webkit-scrollbar-thumb": {
            backgroundColor: theme.palette.secondary.main,
            borderRadius: "10px"
        },
    },
    root: {
        '& label': {
            color: theme.palette.primary.light,
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
                borderColor: theme.palette.primary.light,
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
            color: theme.palette.error.light,
            '& .MuiOutlinedInput-notchedOutline':{
                borderColor: theme.palette.error.light,
            }
        },
    },
    input: {
        color: theme.palette.primary.light,
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
    }
}));

export default function CreateRankModal({open, onClose, onSubmit, usefulValues, rankingFilter}){

    let [values, setValues] = useState({repeatType: repeatType.NONE});
    let [error, setError] = useState({});
    let classes = useStyles();

    const getPreview = () => {
        const error = findErrorInPreview();
        if(error){
            return "Inserire " + error.label + ", " + error.error;
        } else {
            let preview = ""
            if(values.repeatType === repeatType.NONE) preview += "1. " + dayjs(Date.parse(values["startDate"])).format('DD/MM/YYYY') + " - " +  dayjs(Date.parse(values["endDate"])).format('DD/MM/YYYY');
            if(values.repeatType === repeatType.MONTH) {
                let year = values.startMonth.getFullYear();
                let month = values.startMonth.getMonth();
                for(let i = 0; i < values.repeatNum; i++){
                    preview += i+1 + ". " + dayjs(new Date(year, month, 1)).format('DD/MM/YYYY') + " - ";
                    month++;
                    preview += dayjs(new Date(year, month, 0)).format('DD/MM/YYYY') + "\n";
                }
            }
            if(values.repeatType === repeatType.CUSTOM) {
                let startDate = new Date(values["startDate"])
                for(let i = 0; i < values.repeatNum; i++){
                    preview += i+1 + ". " + dayjs(startDate).format('DD/MM/YYYY') + " - ";
                    startDate.setDate(startDate.getDate() + parseInt(values.dayLife) -1);
                    preview += dayjs(startDate).format('DD/MM/YYYY') + "\n"
                    startDate.setDate(startDate.getDate() + 1);
                }
            }
            return preview;
        }
    }

    const findErrorInPreview = () => {
        let convertedValues = {...values};
        convertedValues["startDate"] = Date.parse(values["startDate"])
        convertedValues["endDate"] = Date.parse(values["endDate"])

        if(convertedValues.repeatType === repeatType.NONE) {
            if(!convertedValues.startDate) return {error: "Campo Obbligatorio", field: "startDate", label: "Data inizio"}
            if(!convertedValues.endDate) return {error: "Campo Obbligatorio", field: "endDate", label: "Data fine"}
            if(convertedValues.startDate > convertedValues.endDate) return {error: "Data precedente a data inizio", field: "endDate", label: "Data fine"}
        }

        if(convertedValues.repeatType === repeatType.MONTH) {
            if(!convertedValues.startMonth) return {error: "Campo Obbligatorio", field: "startMonth", label: "Mese inizio"}
            if(!convertedValues.repeatNum) return {error: "Campo Obbligatorio", field: "repeatNum", label: "Numero ripetizioni"}
            if(convertedValues.repeatNum < 1) return {error: "Valore non valido", field: "repeatNum", label: "Numero ripetizioni"}
        }

        if(convertedValues.repeatType === repeatType.CUSTOM) {
            if(!convertedValues.startDate) return {error: "Campo Obbligatorio", field: "startDate", label: "Data inizio"}
            if(!convertedValues.dayLife) return {error: "Campo Obbligatorio", field: "dayLife", label: "Durata in giorni"}
            if(!convertedValues.repeatNum) return {error: "Campo Obbligatorio", field: "repeatNum", label: "Numero ripetizioni"}
            if(convertedValues.dayLife < 1) return {error: "Valore non valido", field: "dayLife", label: "Numero ripetizioni"}
            if(convertedValues.repeatNum < 1) return {error: "Valore non valido", field: "repeatNum", label: "Numero ripetizioni"}
        }

        return false;
    }

    const save = () => {

        let convertedValues = {...values};
        convertedValues["startDate"] = Date.parse(values["startDate"])
        convertedValues["endDate"] = Date.parse(values["endDate"])

        if(!convertedValues.title) {
            setError({title : "Campo obbligatorio"});
            return;
        }
        const error = findErrorInPreview();
        if(error) {
            setError({[error.field]: error.error})
            return;
        }
        if(convertedValues.value === null || convertedValues.value === undefined) {
            setError({value : "Campo obbligatorio"});
            return;
        }
        if(convertedValues.filter !== null && (convertedValues.filter === 0 || convertedValues.filter === 2) && (convertedValues.filterValue === null || convertedValues.filterValue === undefined)) {
            setError({filterValue : "Campo obbligatorio"});
            return;
        }
        if(convertedValues.filter !== null && convertedValues.filter === 1) {
            if(!convertedValues.minAge){
                setError({minAge : "Campo obbligatorio"});
                return;
            }
            if(convertedValues.minAge < 1){
                setError({minAge : "Valore non valido"});
                return;
            }
            if(!convertedValues.maxAge){
                setError({maxAge : "Campo obbligatorio"});
                return;
            }
            if(convertedValues.maxAge < convertedValues.minAge){
                setError({maxAge : "Età inferiore al minimo"});
                return;
            }
            convertedValues.filterValue = convertedValues.minAge + " - " + convertedValues.maxAge
            delete convertedValues.minAge
            delete convertedValues.maxAge
        }
        if(convertedValues.filter === '') delete convertedValues.filter
        if(convertedValues.repeatType === repeatType.MONTH) {
            let startMontJs = dayjs(convertedValues.startMonth);//.year(year).month(month)
            convertedValues.startDate = startMontJs.startOf("month").valueOf()//  Date.parse(new Date(year, month, 1))
            convertedValues.endDate = startMontJs.endOf("month").valueOf() // Date.parse(new Date(year, month +1, 0))
            delete convertedValues.startMonth
        }
        if(convertedValues.repeatType === repeatType.CUSTOM){
            let endDate = new Date(values["startDate"])
            convertedValues.endDate =  dayjs(endDate).add(parseInt(convertedValues.dayLife) -1, 'day').valueOf()
            delete convertedValues.dayLife;
        }
        onSubmit(convertedValues)
        close()
    }

    const close = () => {
        setValues({repeatType: repeatType.NONE})
        setError({});
        onClose()
    }

    const onTextChange = (value, name) => {
        let newValues = {...values};
        if(name === "filter") delete newValues.filterValue
        if(name === "repeatType") {
            if((values[name] === repeatType.NONE && value === repeatType.MONTH) || (values[name] === repeatType.MONTH && value === repeatType.NONE)){
                delete newValues.startDate
                delete newValues.repeatNum
            }
            if((values[name] === repeatType.NONE && value === repeatType.CUSTOM) || (values[name] === repeatType.CUSTOM && value === repeatType.NONE)){
                delete newValues.repeatNum
            }
            if((values[name] === repeatType.CUSTOM && value === repeatType.MONTH) || (values[name] === repeatType.MONTH && value === repeatType.CUSTOM)){
                delete newValues.startDate
            }
            delete newValues.endDate
            delete newValues.startMonth
            delete newValues.dayLife
            if(value === repeatType.MONTH) newValues.startMonth = new Date();
        }
        newValues[name] = value;
        setError({});
        setValues(newValues);
    }

    return (
        <BaseModal open={open} onClose={close} onSave={save}>
            <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1rem"}} justifyContent={"center"}>
                <Grid item xs={12}>
                    <TextInput label={"Titolo"} value={values.title}
                               type="string" required
                               onTextChange={(value) => {onTextChange(value, "title")}}
                               error={error.title}/>
                </Grid>
                <Grid item xs={12}>
                    <FormControl component="fieldset">
                        <FormLabel component="legend" className={classes.radioLabel}>Ripetizione</FormLabel>
                        <RadioGroup row value={values.repeatType} onChange={(event) => onTextChange(event.target.value, "repeatType")} className={classes.radioLabel}>
                            <FormControlLabel value={repeatType.NONE} control={<Radio className={classes.radioButton}/>} label="Nessuna" />
                            <FormControlLabel value={repeatType.MONTH} control={<Radio className={classes.radioButton}/>} label="Mensile" />
                            <FormControlLabel value={repeatType.CUSTOM} control={<Radio className={classes.radioButton}/>} label="Custom" />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                {
                    (values.repeatType === repeatType.NONE || values.repeatType === repeatType.CUSTOM) && <Grid item xs={12}>
                        <TextInput required label={"Data inizio"} value={values.startDate}
                                   type="date"
                                   InputLabelProps={{
                                       shrink : true,
                                   }}
                                   onTextChange={(value) => {
                                       onTextChange(value,"startDate")
                                   }}
                                   error={error.startDate}/>
                    </Grid>
                }
                {
                    values.repeatType === repeatType.NONE && <Grid item xs={12}>
                        <TextInput required label={"Data fine"} value={values.endDate}
                                   type="date" min={values.startDate}
                                   InputLabelProps={{
                                       shrink : true,
                                   }}
                                   onTextChange={(value) => {
                                       onTextChange(value,"endDate")
                                   }}
                                   error={error.endDate}/>
                    </Grid>
                }
                {values.repeatType === repeatType.MONTH && <Grid item xs={12}>
                        <DatePicker
                            fullWidth
                            inputVariant="outlined"
                            classes={{root: classes.root}}
                            inputProps={{className: classes.input}}
                            views={["year", "month"]}
                            label="Mese inizio"
                            value={values.startMonth}
                            onChange={(value) => onTextChange(value, "startMonth")}
                        />
                    </Grid>
                }
                {
                    values.repeatType === repeatType.CUSTOM && <Grid item xs={12}>
                        <TextInput required label={"Durata in giorni"} value={values.dayLife}
                                   type="number"
                                   min={1} step={1}
                                   onTextChange={(value) => {onTextChange(value, "dayLife")}}
                                   error={error.dayLife}/>
                    </Grid>
                }
                {
                    (values.repeatType === repeatType.MONTH || values.repeatType === repeatType.CUSTOM) && <Grid item xs={12}>
                        <TextInput required label={"Numero ripetizioni"} value={values.repeatNum}
                                   type="number"
                                   min={1} step={1}
                                   onTextChange={(value) => {onTextChange(value, "repeatNum")}}
                                   error={error.repeatNum}/>
                    </Grid>
                }
                <Grid item xs={12}>
                    <TextInput required label={"Valore"} value={values.value}
                               options={usefulValues} select
                               onTextChange={(value) => {onTextChange(value, "value")}}
                               error={error.value}>
                        {usefulValues.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                    </TextInput>
                </Grid>
                <Grid item xs={12}>
                    <TextInput label={"Categoria filtro"} value={values.filter}
                               options={rankingFilter} select
                               onTextChange={(value) => {onTextChange(value, "filter")}}
                               error={error.filter}>
                        <MenuItem key='' value=''>
                            <br/>
                        </MenuItem>
                        {rankingFilter.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                    </TextInput>
                </Grid>
                {
                    values.filter !== null && values.filter === 0 &&
                    <Grid item xs={12}>
                        <TextInput required label={"Valore filtro"} value={values.filterValue}
                                   options={sessionType} select
                                   onTextChange={(value) => {
                                       onTextChange(value,"filterValue")
                                   }}
                                   error={error.filterValue}>
                            {sessionType.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                        </TextInput>
                    </Grid>
                }
                {
                    values.filter !== null && values.filter === 1 &&
                    <>
                        <Grid item xs={6}>
                            <TextInput required label={"Età minima"} value={values.minAge}
                                       type="number"
                                       min={1} step={1}
                                       onTextChange={(value) => {onTextChange(value, "minAge")}}
                                       error={error.minAge}/>
                        </Grid>
                        <Grid item xs={6}>
                            <TextInput required label={"Età massima"} value={values.maxAge}
                                       type="number"
                                       min={1} step={1}
                                       onTextChange={(value) => {onTextChange(value, "maxAge")}}
                                       error={error.maxAge}/>
                        </Grid>
                    </>
                }
                {
                    values.filter !== null && values.filter === 2 &&
                    <Grid item xs={12}>
                        <TextInput required label={"Valore filtro"} value={values.filterValue}
                                   options={genderOptions} select
                                   onTextChange={(value) => {
                                       onTextChange(value,"filterValue")
                                   }}
                                   error={error.filterValue}>
                            {genderOptions.map(o => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
                        </TextInput>
                    </Grid>
                }
                <Grid item xs={8} className={classes.previewTitle}>
                    Anteprima Classifiche:
                </Grid>
                <Grid item xs={8} className={classes.preview}>
                    {getPreview()}
                </Grid>
            </Grid>
        </BaseModal>
    );
}

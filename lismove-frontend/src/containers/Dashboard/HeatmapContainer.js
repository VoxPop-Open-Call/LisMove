import React,{useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {FormControlLabel,Grid,Slider,Switch,Typography} from "@mui/material";
import Heatmap from "./Heatmap";
import StartIconButton from "../../components/buttons/StartIconButton";
import SaveIcon from "@mui/icons-material/Save";
import Box from "@mui/material/Box";
import TextInput from "../../components/forms/TextInput";
import dayjs from "dayjs";

const useStyles = makeStyles(theme => ({
    sliderText: {
        paddingRight: "0.8rem",
    },
    secondColumn: {
        paddingTop: "0.33rem"
    }
}));

export default function HeatmapContainer(){

    let classes = useStyles();
    const yesterday = dayjs(new Date(new Date().setDate(new Date().getDate()-1))).format("YYYY-MM-DD");
    const today = dayjs(new Date()).format("YYYY-MM-DD");
    let [filters, setFilters] = useState({
        isHomeWorkPath: false,
        minAge: 18,
        maxAge: 30,
        minTime: "10:00",
        maxTime: "13:00",
        minDate: yesterday,
        maxDate: yesterday
    });

    let [savedFilters, setSavedFilters] = useState({...filters})

    return (
        <Grid container>
            <Grid container direction="row">
                <Grid item md={5} xs={12}>
                    <Grid container direction="column" alignItems={"center"} justifyContent={"center"}>
                        <Grid item>
                            <FormControlLabel
                                control={
                                    <Switch checked={filters.isHomeWorkPath} color="primary"
                                            onChange={evt => setFilters({...filters, isHomeWorkPath : evt.target.checked})}/>
                                }
                                label={"Solo percorsi casa -> scuola/lavoro"}
                            />
                        </Grid>
                        <Grid container direction="row" spacing={4} style={{margin : 0,width : "100%",marginBottom : "0.5rem"}}>
                            <Grid item xs={6}>
                                <TextInput required label={"Inizio periodo"} value={filters.minDate} type="date" color="dark" max={today}
                                           onTextChange={value => {
                                               if(filters.maxDate < value.trim()) setFilters({...filters, maxDate : value.trim(), minDate : value.trim()})
                                               else setFilters({...filters, minDate : value.trim()})
                                           }}/>
                            </Grid>
                            <Grid item xs={6}>
                                <TextInput required label={"Fine periodo"} value={filters.maxDate} type="date" color="dark" max={today}
                                           onTextChange={value => {
                                               if(value.trim() < filters.minDate) setFilters({...filters, minDate : value.trim(), maxDate : value.trim()})
                                               else setFilters({...filters, maxDate : value.trim()})
                                           }}/>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item md={5} xs={12} className={classes.secondColumn}>
                    <Grid container direction="column" alignItems={"center"} justifyContent={"center"}>
                        <Grid item>
                            <Grid container justifyContent={"center"}  alignItems={"center"} direction={"row"}>
                                <Grid item>
                                    <Typography gutterBottom className={classes.sliderText}>Età</Typography>
                                </Grid>
                                <Grid>
                                    <Box sx={{ width: 300 }}>
                                        <Slider
                                            value={[filters.minAge, filters.maxAge]}
                                            onChange={(event ,newValues) => setFilters({...filters, minAge: newValues[0], maxAge: newValues[1]})}
                                            valueLabelDisplay="auto" max={110}
                                        />
                                    </Box>
                                </Grid>
                            </Grid>
                        </Grid>
                        <Grid container direction="row" spacing={4} style={{margin : 0,width : "100%",marginBottom : "0.5rem"}}>
                            <Grid item xs={6}>
                                <TextInput required label={"Inizio fascia oraria"} value={filters.minTime} type="time" color="dark"
                                           onTextChange={value => {
                                               if(filters.maxTime < value.trim()) setFilters({...filters, maxTime : value.trim(), minTime : value.trim()})
                                               else setFilters({...filters, minTime : value.trim()})
                                           }}/>
                            </Grid>
                            <Grid item xs={6}>
                                <TextInput required label={"Fine fascia oraria"} value={filters.maxTime} type="time" color="dark"
                                           onTextChange={value => {
                                               if(value.trim() < filters.minTime) setFilters({...filters, minTime : value.trim(), maxTime : value.trim()})
                                               else setFilters({...filters, maxTime : value.trim()})
                                           }}/>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid container md={2} xs={12} alignItems={"center"} justifyContent={"center"} style={{margin: 0,width : "100%",marginBottom : "1.5rem"}}>
                    <Grid item>
                        <StartIconButton onClick={() => setSavedFilters({...filters})} title="Salva" startIcon={<SaveIcon/>}
                                         disabled={JSON.stringify(filters) === JSON.stringify(savedFilters)}/>
                    </Grid>
                </Grid>
            </Grid>
            <Heatmap filters={savedFilters}/>
        </Grid>
    );
}
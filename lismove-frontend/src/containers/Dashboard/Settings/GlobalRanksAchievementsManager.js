import {globalRanksAchievementsManagerFields} from "./settingsFields";
import RenderFields from "./RenderFields";
import React from "react";
import Grid from "@mui/material/Grid";


export default function GlobalRanksAchievementsManager({values, setValues}){

    let fields = globalRanksAchievementsManagerFields(values, setValues)

    return <Grid container spacing={4} style={{margin : 0,width : "100%",marginBottom : "0.5rem"}}>

        <RenderFields fields={fields} values={values}/>

    </Grid>
}
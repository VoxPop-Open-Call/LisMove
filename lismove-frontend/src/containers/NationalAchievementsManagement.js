import {Paper} from "@mui/material";
import React from "react";
import AchievementsManager from "./AchievementsManager";
import {useGetNationalAchievements} from "../services/ContentManager";


export default function NationalAchievementsManagement(){

    let {achievements = []} = useGetNationalAchievements();

    return <Paper style={{padding: "2rem"}}>
        <AchievementsManager achievements={achievements} national/>
    </Paper>
}
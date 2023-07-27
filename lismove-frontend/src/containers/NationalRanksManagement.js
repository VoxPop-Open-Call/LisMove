import {useGetNationalRanks} from "../services/ContentManager";
import {Paper} from "@mui/material";
import React from "react";
import RanksManager from "./RanksManager";


export default function NationalRanksManagement(){

    let {ranks = []} = useGetNationalRanks();

    return <Paper style={{padding: "2rem"}}>
        <RanksManager ranks={ranks} national/>
    </Paper>
}
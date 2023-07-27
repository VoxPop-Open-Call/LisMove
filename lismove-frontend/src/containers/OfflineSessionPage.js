import React from "react";
import {get, OFFLINE_SESSIONS, } from "../services/Client";
import {Paper} from "@mui/material";
import {useQuery} from "react-query";
import NTMXGrid, {timestampTypeToDate, timestampTypeToTime} from "../components/NTMXGrid";

export function OfflineSessionPage(){
	const {status, data, error} = useQuery(OFFLINE_SESSIONS, () => get(OFFLINE_SESSIONS));

	function getValue(params, field){
		if(!params.row[field]) return ""
		return params.row[field]
	}

	const defaultColumns = [
		{
			headerName: 'Session UUID',
			field: 'id',
			width: 310,
			hide:true
		},
		{
			headerName: 'User UID',
			field: 'user',
			width: 220,
			hide: true
		},
		{
			headerName: 'Email',
			field: 'email',
			width: 220,
			hide: true
		},
		{
			headerName: 'Username',
			field: 'username',
			width: 170
		},
		{
			headerName: 'Sensore',
			field: 'sensor',
			width: 170,
			hide:true
		},
		{
			headerName: 'Data Inizio',
			field: 'startTimeDate',
			width: 180,
			valueGetter: params => getValue(params, "startTime"),
			...timestampTypeToDate
		},
		{
			headerName: 'Ora Inizio',
			field: 'startTime',
			width: 180,
			...timestampTypeToTime
		},
		{
			headerName: 'Data Fine',
			field: 'endTimeDate',
			width: 180,
			valueGetter: params => getValue(params, "endTime"),
			...timestampTypeToDate
		},
		{
			headerName: 'Ora Fine',
			field: 'endTime',
			width: 280,
			...timestampTypeToTime
			//renderCell: (params) => <RenderCell params = {params} saveEdit = {saveEdit} inputType="time"/>
		},
		{
			headerName: 'Start revs',
			field: 'startRevs',
			width: 180,
			type: "number",
			headerAlign: 'left',
			align: "left",
			hide: true
		},
		{
			headerName: 'End revs',
			field: 'endRevs',
			width: 280,
			type: "number",
			headerAlign: 'left',
			align: "left",
			hide: true
		},
		{
			headerName: 'Distanza',
			field: 'distance',
			width: 280,
			type: "number",
			headerAlign: 'left',
			align: "left",
		}
	];

	return <Paper style={{padding: "2rem"}}>
		<NTMXGrid
			columns={defaultColumns}
			rows={data || []}
			title="Sessioni Offline"
			getRowId={(row) => data && row.id}
		/>
	</Paper>
}

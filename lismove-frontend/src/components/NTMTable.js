import React from "react"
import MaterialTable from "material-table";
import {useTheme} from "@mui/material";
import NTMToolbar from "./NTMToolbar";
import Paper from "@mui/material/Paper";

export default function NTMTable({columns, data, title, actions, options={}}) {
	const theme = useTheme();
	return (
		<MaterialTable
			title={title || " "}
			columns={columns}
			data={data}
			options={{
				actionsColumnIndex: -1,
				headerStyle: {
					backgroundColor: theme.palette.primary.main,
					color:"#FFF",
					textTransform: "uppercase",
					padding: "0.85rem"
				},
				rowStyle: {
					color:"#000",
					textAlign: "center",
				},
				...options
			}}
			components={{
				Toolbar: props => <NTMToolbar {...props} />,
				Container: props => <Paper {...props} style={{padding:"2rem"}} />
			}}
			actions={actions}
			localization={{
				pagination: {
					labelDisplayedRows: '{from}-{to} of {count}'
				},
				toolbar: {
					nRowsSelected: '{0} row(s) selected'
				},
				header: {
					actions: ''
				},
				body: {
					emptyDataSourceMessage: 'No records to display',
					filterRow: {
						filterTooltip: 'Filter'
					}
				}
			}}
		/>
	)
}

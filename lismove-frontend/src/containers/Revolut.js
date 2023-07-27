import {useEffect, useState} from "react";
import {useSnackbar} from "notistack";
import {get, getErrorMessage, post, USERS} from "../services/Client";
import Grid from "@mui/material/Grid";
import {CircularProgress, Paper} from "@mui/material";
import Button from "@mui/material/Button";
import NTMTable from "../components/NTMTable";

export function Revolut(){

	let [counterparties, setCounterparties] = useState([]);
	let [loading, setLoading] = useState(false);
	const {enqueueSnackbar} = useSnackbar();

	useEffect(getCounterparties, [])

	function getCounterparties() {
		get("revolut/counterparties").then(data => setCounterparties(data.map(c => {
			return {
				...c.accounts[0],
				id:c.id
			}
		})))
	}

	function upload(file, type) {
		let formData = new FormData();

		formData.append("csvfile", file);
		formData.append("type", type)
		setLoading(true)

		post("revolut/massive", {body:formData, header:{"Content-Type": "multipart/form-data"}}).then(response => {
			enqueueSnackbar("Finished", {variant: "success"})
			const blob = new Blob([response.data], {type: response.data.type});
			const url = window.URL.createObjectURL(blob);
			const link = document.createElement('a');
			link.href = url;
			let fileName = 'esito.csv';
			link.setAttribute('download', fileName);
			document.body.appendChild(link);
			link.click();
			link.remove();
			window.URL.revokeObjectURL(url);
		})
			.catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
			.finally(() => setLoading(false));

	}

	return <Paper>
		<Grid container>
			<Grid item xs={12}>
				<NTMTable
					columns={[
						{
							title:"ID",
							field: "id"
						},
						{
							title:"Nome",
							field: "name"
						},
						{
							title:"IBAN",
							field: "iban"
						},
						{
							title:"BIC",
							field: "bic"
						},
						{
							title:"Email",
							field: "email"
						},
					]}
					data={counterparties}
				/>
			</Grid>
			<Grid item container
			      direction="row"
			      justifyContent="space-evenly"
			      alignItems="center"
				style={{margin:"1rem"}}
			>
				{
					loading ?
						<CircularProgress/> :
						<>
							<div>
								<input
									accept="text/csv"
									style={{display:"none"}}
									id="counterparties-file"
									type="file"
									onChange={event => upload(event.target.files[0], "c")}
								/>
								<label htmlFor="counterparties-file">
									<Button variant="contained" color="primary" component="span">
										Carica controparti
									</Button>
								</label>
							</div>
							<div>
								<input
									accept="text/csv"
									style={{display:"none"}}
									id="payments-file"
									type="file"
									onChange={event => upload(event.target.files[0], "p")}
								/>
								<label htmlFor="payments-file">
									<Button variant="contained" color="primary" component="span">
										Carica pagamenti
									</Button>
								</label>
							</div>
						</>
				}

			</Grid>
		</Grid>
	</Paper>
}

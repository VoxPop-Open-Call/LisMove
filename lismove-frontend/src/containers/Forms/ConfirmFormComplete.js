import makeStyles from '@mui/styles/makeStyles';
import {Container, Typography} from "@mui/material";
import {useLocation, useParams} from "react-router-dom";
import React, {useEffect, useMemo, useState} from "react";
import {get, getErrorMessage, getUrl, post, USERS} from "../../services/Client";
import axios from "axios";
import CssBaseline from "@mui/material/CssBaseline";
import logo from "../../images/splash.png"

const useStyles = makeStyles((theme) => ({
	paper: {
		marginTop: theme.spacing(8),
		display: 'flex',
		flexDirection: 'column',
		alignItems: 'center',
	},
	avatar: {
		margin: theme.spacing(1),
	},
	form: {
		width: '100%', // Fix IE 11 issue.
		marginTop: theme.spacing(1),
	},
	submit: {
		margin: theme.spacing(3, 0, 2),
	},
	error: {
		display: 'flex',
		flexDirection: 'column',
		alignItems: 'center',
		color: theme.palette.primary.main
	},
	input: {
		'& :-webkit-autofill': {
			'transition-delay': '9999s'
		},
	},
	bold: {
		fontWeight:"bold",
		marginTop:"1em"
	}
}))

export default function ConfirmFormComplete() {
	const classes = useStyles();
	const [message, setMessage] = useState()
	const [award, setAward] = useState()
	const [description, setDescription] = useState()
	const {search} = useLocation()
	let {aid} = useParams();
	const query= useMemo(() => {
		return new URLSearchParams(search);
	}, [search])

	useEffect(()=> {
		console.log("UseEffect")
		let email = query.get("e")
		axios.get(getUrl(`${USERS}/${email}/profile`)).then(({data}) => {
			setMessage(`Grazie, ${data.firstName} ${data.lastName} per aver partecipato`)
			axios.post(getUrl(`awards/custom-users?uid=${data.uid}&aid=${aid}`))
				.then(({data}) => {
					if(data.name) {
						setAward(data.name);
						setDescription(data.description);
					}
					else setAward("Sembra che tu abbia già riscattato il premio")
				})
				.catch(e => setAward("Sembra che tu abbia già riscattato il premio"));
		}).catch(e => {
			setMessage("Sembra che abbia utilizzato una mail non presente nei nostri sistemi")
		})
	}, [query])

	return (
        <Container component="main" maxWidth="xs">
			<CssBaseline />
			<div className={classes.paper}>
				<img className={classes.avatar} alt={"home"} src={logo}/>
				<Typography component="h1" variant="h5">
					Questionario completato
				</Typography>
				<Typography component={'h3'} variant="h6" align={'center'}>
					{message}
				</Typography>
				<Typography component={'h5'} variant="h6" align={'center'} className={classes.bold}>
					<b>{award}</b>
				</Typography>
				<Typography component={'h5'} variant="h6" align={'center'} >
					{description}
				</Typography>
			</div>
		</Container>
    );
}

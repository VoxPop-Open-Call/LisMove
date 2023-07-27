import React, {useState} from 'react';
import makeStyles from '@mui/styles/makeStyles';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Box from '@mui/material/Box';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import InputAdornment from "@mui/material/InputAdornment";
import {Visibility,VisibilityOff} from "@mui/icons-material";
import IconButton from "@mui/material/IconButton";

function Copyright() {
	return (
		<Typography variant="body2" color="textSecondary" align="center">
			{'Copyright © '}
			<Link color="inherit" href="https://nextome.net/">
				Nextome
			</Link>{' '}
			{new Date().getFullYear()}
			{'.'}
		</Typography>
	);
}

const useStyles = makeStyles((theme) => ({
	paper: {
		marginTop: theme.spacing(8),
		display: 'flex',
		flexDirection: 'column',
		alignItems: 'center',
	},
	avatar: {
		margin: theme.spacing(1),
		backgroundColor: theme.palette.secondary.main,
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
}));

export function LoginPage({ onSubmit, error }) {
	const [username, setUsername] = useState("");
	const [password, setPassword] = useState("");
	const classes = useStyles();
	let [showPassword, setShowPassword] = useState(false);

	const preventLoseFocus = (event) => {
		event.preventDefault()
	}

	const onKeyPress = (ev) => {
		if (ev.key === 'Enter') {
			onSubmit(username, password);
			ev.preventDefault();
		}
	}

	return (
        <Container component="main" maxWidth="xs">
			<CssBaseline />
			<div className={classes.paper}>
				<Avatar className={classes.avatar}>
					<LockOutlinedIcon />
				</Avatar>
				<Typography component="h1" variant="h5">
					Effettua l'accesso
				</Typography>
				<TextField
					variant="outlined"
					className={classes.input}
					margin="normal"
					required
					fullWidth
					id="email"
					label="Indirizzo Email"
					name="email"
					autoComplete="email"
					autoFocus
					onChange={event => {
						setUsername(event.target.value);
					}}
					value={username}
					onKeyPress={onKeyPress}
				/>
				<TextField
					variant="outlined"
					className={classes.input}
					margin="normal"
					required
					fullWidth
					name="password"
					label="Password"
					type={showPassword ? "text" : "password"}
					id="password"
					autoComplete="current-password"
					onChange={event => {
						setPassword(event.target.value);
					}}
					InputProps={{
						endAdornment: (
							<InputAdornment position="end">
								<IconButton
                                    aria-label="toggle password visibility"
                                    onClick={() => setShowPassword(!showPassword)}
                                    onMouseDown={preventLoseFocus}
                                    onMouseUp={preventLoseFocus}
                                    edge="end"
                                    size="large">
									{showPassword ? <Visibility /> : <VisibilityOff />}
								</IconButton>
							</InputAdornment>)
					}}
					value={password}
					onKeyPress={onKeyPress}
				/>
				<Button
					fullWidth
					variant="contained"
					color="primary"
					className={classes.submit}
					onClick={() => {
						onSubmit(username, password);
					}}
				>
					Entra
				</Button>
			</div>
			<Box mt={4}>
				<div className={classes.error}>{error}</div>
			</Box>
			<Box mt={8}>
				<Copyright />
			</Box>
		</Container>
	);
}

export function LogoutView({ onClick }) {
	return (
		<div>
			<span>You are logged in</span>
			<button onClick={onClick}>Logout</button>
		</div>
	);
}

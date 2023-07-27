import { Typography} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        maxHeight: '100vh'
    },
    content: {
        flexGrow: 1,
        padding: theme.spacing(3),
    },
}));

export default function ConfirmEmail() {
    const classes = useStyles();
    return (
        <div className={classes.root}>
            <main className={classes.content}>
                <Typography variant={'h4'}>Conferma email</Typography>
                <br/>
                <Typography variant={'h6'}>
                    Grazie per esserti registrato! Ti abbiamo inviato una mail con i dati di accesso alla piattaforma.
                </Typography>
                <Typography>
                    Una volta confermata l'email potrai accedere cliccando <a href="/">qui</a>
                </Typography>
            </main>
        </div>
    );
}
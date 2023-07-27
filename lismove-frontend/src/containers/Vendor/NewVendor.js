import makeStyles from '@mui/styles/makeStyles';
import React, {useState} from "react";
import {
    Button, Grid, Typography,
} from "@mui/material";
import TextInput from "../../components/forms/TextInput";
import {Link, useHistory, useParams} from "react-router-dom";
import {firebaseAuth} from "../../firebase";
import {useQueryClient} from "react-query";
import {useSnackbar} from "notistack";
import {post, VENDORS} from "../../services/Client";
import VendorData from "./VendorData";
import {isEmpty} from "../../services/helper";
import {CREATION_ERROR_VENDOR, ERROR_FORM, MANDATORY, PASSWORD_NOT_MATCH} from "../../constants/errorMessages";
import {validateVendorData} from "../../services/ValidationFormManager";
import CircularLoading from "../../components/CircularLoading";


const useStyles = makeStyles((theme) => ({
    root:{
        minHeight: '100vh',
        padding: theme.spacing(3),
    },
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    marginBottom: {
        marginBottom: theme.spacing(2)
    },
    formControl: {
        width: '100%'
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
    title:{
        width: '100%',
        textAlign:'center',
        marginBottom:theme.spacing(3),
    }
}));

export default function NewVendor({}) {
    const classes = useStyles();

    let history = useHistory();
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    const [data, setData] = useState({});
    const [error, setError] = useState({});
    const [loading, setLoading] = useState(false);

    const handleOnChange = (text, field) => {
        setData({...data, [field]: text});
    };

    return (
        <div className={classes.root}>
            <Grid item xs={12} className={classes.title}>
                <Typography variant={'h5'}>Registrati come Esercente</Typography>
            </Grid>
            <Grid container
                  direction="row"
                  alignItems="center"
                  className={classes.marginBottom}
            >
                {/*--- nome  --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Nome"}
                        type="text"
                        color={'primary'}
                        value={data.firstName || ''}
                        onTextChange={(text) => handleOnChange(text, 'firstName')}
                        error={error.firstName || ''}
                    />
                </Grid>

                {/*--- cognome  --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Cognome"}
                        type="text"
                        color={'primary'}
                        value={data.lastName || ''}
                        onTextChange={(text) => handleOnChange(text, 'lastName')}
                        error={error.lastName || ''}
                    />
                </Grid>

                {/*--- email  --*/}
                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Email"}
                        type="text"
                        color={'primary'}
                        value={data.email || ''}
                        onTextChange={(text) => handleOnChange(text, 'email')}
                        error={error.email || ''}
                    />
                </Grid>

                {/*--- password  --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Password"}
                        type="password"
                        color={'primary'}
                        value={data.newPassword || ''}
                        onTextChange={(text) => handleOnChange(text, 'newPassword')}
                        error={error.newPassword || ''}
                    />
                </Grid>
                {/*--- ripeti password  --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Ripeti Password"}
                        type="password"
                        color={'primary'}
                        value={data.repeatPassword || ''}
                        onTextChange={(text) => handleOnChange(text, 'repeatPassword')}
                        error={error.repeatPassword || ''}
                    />
                </Grid>
            </Grid>

            <Grid item xs={12} className={classes.title}>
                <Typography variant={'caption'}>Inserisci i tuoi dati legali. Successivamente potrai inserire le informazioni della tua attività</Typography>
            </Grid>

            <VendorData handleOnChange={handleOnChange} vendor={data} errors={error}/>

            {/*--spacing--*/}
            <Grid container className={classes.marginBottom} style={{width:'100%'}}/>

            <Grid container
                  justifyContent={'center'}
                  direction={'row'}
            >
                <Grid item xs={12} md={3}>
                    <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        onClick={() => {
                            setLoading(true);
                            let newData = {...data};

                            //--------controllo-----------
                            let valid = true;
                            let newErrors = {}
                            if(isEmpty(newData.firstName)){
                                newErrors.firstName = MANDATORY;
                                valid = false;
                            }
                            if(isEmpty(newData.lastName)){
                                newErrors.lastName = MANDATORY;
                                valid = false;
                            }
                            if(isEmpty(newData.email))
                            {
                                newErrors.email= MANDATORY;
                                valid = false;
                            }
                            if (isEmpty(newData.newPassword)) {
                                newErrors.newPassword= MANDATORY;
                                valid = false;
                            }
                            if (newData.newPassword !== newData.repeatPassword) {
                                newErrors.repeatPassword= PASSWORD_NOT_MATCH;
                                valid = false;
                            }
                            if(isEmpty(newData.lastName)){
                                newErrors.lastName = MANDATORY;
                                valid = false;
                            }
                            let [errorsData, isValidData] = validateVendorData(newData);

                            setError({...newErrors,...errorsData});
                            if(!valid || !isValidData)
                            {
                                setLoading(false);
                                enqueueSnackbar(ERROR_FORM, {variant: "error"});
                                return;
                            }
                            //----- -------------

                            firebaseAuth.createUserWithEmailAndPassword(newData.email, newData.newPassword).then(() => {
                                //cancella password prima di mandare la richiesta;
                                newData.newPassword = null;
                                newData.repeatPassword = null;

                                newData.visible = true;
                                newData.uid = firebaseAuth.currentUser.uid;

                                post(VENDORS, {body: newData,})
                                    .then(() => {
                                        //---mandare mail di conferma---
                                        firebaseAuth.currentUser.sendEmailVerification();
                                        enqueueSnackbar("User Saved", {variant: "success"});
                                        history.push('/confirmEmail');
                                    })
                                    .catch(() => {
                                        firebaseAuth.currentUser.delete().then(() => {
                                            enqueueSnackbar(ERROR_FORM, {variant: "error"});
                                        })
                                    })
                                    .finally(async () => {
                                        await queryClient.invalidateQueries([VENDORS, {vendor:firebaseAuth.currentUser.uid}]);
                                        setLoading(false);
                                    });
                            }).catch((e) =>{
                                let appendMessage = '';
                                if(e.code === 'auth/email-already-in-use')//se un errore include _exists allora vuol dire che il vendor e' gia' stato creato
                                    appendMessage = ': email già in uso';
                                enqueueSnackbar(CREATION_ERROR_VENDOR + appendMessage, {variant: "error"});
                                setLoading(false);
                            })

                        }}
                    >
                        Registrati
                    </Button>
                    {loading && <CircularLoading/> }
                </Grid>
            </Grid>
        </div>
    );
}
import makeStyles from '@mui/styles/makeStyles';
import React, {useEffect, useState} from "react";
import {
    Button, FormControlLabel,
    Grid, Switch,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import TextInput from "../../components/forms/TextInput";
import {useHistory, useParams} from "react-router-dom";
import {firebaseAuth} from "../../firebase";
import {useQueryClient} from "react-query";
import {useSnackbar} from "notistack";
import firebase from "firebase";
import {put, VENDORS} from "../../services/Client";
import VendorData from "./VendorData";
import {isEmpty} from "../../services/helper";
import {useGetVendors} from "../../services/ContentManager";
import {validateVendorData} from "../../services/ValidationFormManager";
import {
    ERROR_FORM,
    INCORRECT_PASSWORD,
    MANDATORY,
    NEW_EMAIL_ERROR,
    PASSWORD_NOT_MATCH
} from "../../constants/errorMessages";

const useStyles = makeStyles((theme) => ({
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
}));

export default function VendorProfile({}) {
    const classes = useStyles();

    let history = useHistory();
    let queryClient = useQueryClient();
    const {enqueueSnackbar} = useSnackbar();
    let user = firebaseAuth.currentUser;

    const {uid} = useParams();

    let {vendors, status} = useGetVendors(uid);

    const [data, setData] = useState({});
    const [editPassword, setEditPassword] = useState(false);
    const [error, setError] = useState({});
    useEffect(() => {
        setData({...vendors, email: user.email});
    }, [vendors]);

    const handleOnChange = (text, field) => {
        setData({...data, [field]: text});
    };

    return (
        (<div>
            <Grid container justifyContent="space-between" className={classes.marginBottom}>
                <Button
                    onClick={() => history.goBack()}
                    className={classes.backButton}
                >
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
            </Grid>
            <Grid container
                  direction="row"
                  alignItems="center"
                  className={classes.marginBottom}
            >
                {/*--- email  --*/}
                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Email"}
                        type="text"
                        color={'primary'}
                        value={data.email || ''}
                        onTextChange={(text) => handleOnChange(text, 'email')}
                        error={error.email}
                    />
                </Grid>

                {/*--- password  --*/}
                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Password Attuale"}
                        type="password"
                        color={'primary'}
                        value={data.currentPassword || ''}
                        onTextChange={(text) => handleOnChange(text, 'currentPassword')}
                        error={error.currentPassword}
                    />
                </Grid>

                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={editPassword}
                                onChange={(e) => setEditPassword(e.target.checked)}
                                name="editPassword"
                                color="secondary"
                            />
                        }
                        label="Modifica password"
                    />
                </Grid>

                {/*password*/}
                {
                    editPassword &&
                    <>
                        {/*--- nuova password  --*/}
                        <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                            <TextInput
                                label={"Nuova password"}
                                type="password"
                                color={'primary'}
                                value={data.newPassword || ''}
                                onTextChange={(text) => handleOnChange(text, 'newPassword')}
                                error={error.newPassword}
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
                                error={error.repeatPassword}
                            />
                        </Grid></>
                }
            </Grid>
            {/*--spacing--*/}
            <Grid container className={classes.marginBottom} style={{width: '100%'}}/>
            <VendorData handleOnChange={handleOnChange} vendor={data} errors={error}/>
            {/*--spacing--*/}
            <Grid container className={classes.marginBottom} style={{width: '100%'}}/>
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
                            setError({});
                            let newData = {...data};

                            //---controllo-----
                            if (isEmpty(newData.email)) {
                                setError({email: MANDATORY});
                                return;
                            }

                            if (isEmpty(newData.currentPassword)) {
                                setError({currentPassword: MANDATORY});
                                return;
                            }

                            if (editPassword) {
                                if (newData.newPassword !== newData.repeatPassword) {
                                    setError({
                                        repeatPassword: PASSWORD_NOT_MATCH,
                                    });
                                    return;
                                }
                            }

                            let [errorsData, isValidData] = validateVendorData(newData);

                            setError({...errorsData});
                            if(!isValidData)
                            {
                                enqueueSnackbar(ERROR_FORM, {variant: "error"});
                                return;
                            }

                            const currentEmail = user.email;

                            //autentica l'utente per sicurezza
                            const credential = firebase.auth.EmailAuthProvider.credential(user.email, newData.currentPassword);
                            firebaseAuth.currentUser.reauthenticateWithCredential(credential).then(async () => {
                                //todo: testarea sync
                                try {
                                    //---modifica l'email solo se e' cambiata
                                    if (newData.email !== currentEmail) {
                                        await firebaseAuth.currentUser.updateEmail(newData.email);
                                    }

                                    //---modifica passwrd
                                    if (editPassword && newData.newPassword) {
                                        await firebaseAuth.currentUser.updatePassword(newData.newPassword);
                                    }
                                } catch (e) {
                                    enqueueSnackbar(ERROR_FORM, {variant: "error"});
                                    return;
                                }

                                //cancella password prima di mandare la richiesta;
                                delete newData.newPassword;
                                delete newData.repeatPassword;
                                delete newData.currentPassword;

                                try {
                                    await put(VENDORS, {body: newData, elem: user.uid});
                                } catch (e) {
                                    //riporta la mail allo stato precedente
                                    await firebaseAuth.currentUser.updateEmail(currentEmail)
                                    enqueueSnackbar(NEW_EMAIL_ERROR, {variant: "error"});
                                    return;
                                } finally {
                                    await queryClient.invalidateQueries([VENDORS, {vendor: firebaseAuth.currentUser.uid}]);
                                }

                                enqueueSnackbar("Dati utente salvati", {variant: "success"});
                                history.goBack();

                            }).catch(() => {
                                setError({currentPassword: INCORRECT_PASSWORD});
                            });
                        }}
                    >
                        Salva
                    </Button>
                </Grid>
            </Grid>
        </div>)
    );
}
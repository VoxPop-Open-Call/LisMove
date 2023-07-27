import makeStyles from '@mui/styles/makeStyles';
import React, {useState} from "react";
import CircularLoading from "../../../components/CircularLoading";
import {
    Button,
    Checkbox,
    FormControl,
    FormControlLabel,
    FormGroup,
    FormLabel,
    Grid,
    Radio, RadioGroup, Typography
} from "@mui/material";
import {Album, Edit} from "@mui/icons-material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import TextInput from "../../../components/forms/TextInput";
import NTMImageInput from "../../../components/NTMImageInput";
import {Link} from "react-router-dom";
import EditCoordinatesModal from "../../../components/modals/EditCoordinatesModal";
import {removeImagesFirebase} from "../../../services/FirebaseManager";
import {COORDINATES, post, put, SHOPS, VENDORS} from "../../../services/Client";
import {useSnackbar} from "notistack";
import {useGetCategories} from "../../../services/ContentManager";
import {isEmpty, isValidHttpUrl, isValidPhone} from "../../../services/helper";
import {INVALID_PHONE, INVALID_URL, MANDATORY} from "../../../constants/errorMessages";

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
    title: {
        textAlign: 'center',
        marginBottom: theme.spacing(3),
    },
    error: {
        color: theme.palette.error.main
    }
}));

/**
 * mostra i dettagli di uno shop, se shop e' null allora si sta facendo una new, se defShop e' vuoto allora e' il primo shop ad essere creato
 * @param vendor
 * @param isAdmin
 * @param shop
 * @param goBack
 * @param defShop
 * @param isFirstShop se settato imposta lo shop creato come primo shop (default)
 * @returns {JSX.Element}
 * @constructor
 */
export default function ShopData({vendor, isAdmin, shop, goBack, defShop, isFirstShop = false}) {
    const classes = useStyles();
    const {enqueueSnackbar} = useSnackbar();

    let uid = vendor.uid;

    //seleziona tutte le categories possibili (si presuppone che le categories nel negozio siano presenti come array di integer)
    let {categories: allCategories, status} = useGetCategories();
    const [data, setData] = useState({...shop, logo: null, images: null});//se e' il primo shop allora primary deve essere settato a true, verra cosi considerato come shop di default
    let [error, setError] = useState({});
    const [showcases, setShowCases] = useState({logo: shop.logo, images: shop.images || []});//Oggetto con immagini e logo

    const [openMap, setOpenMap] = useState(false);

    //attendi che siano caricate le categories
    if (status === 'loading')
        return <CircularLoading/>

    /**
     * gestisci il cambio di tab
     * @param text
     * @param field
     */
    const handleOnChange = (text, field) => {
        if (!isAdmin)
            setData((data) => ({...data, [field]: text}));
    };

    const handleOnChangeCheckbox = (e, category) => {
        let newData = {...data};
        newData.categories = newData.categories.slice();
        //se selezionato aggiungi la categoria alla lista
        if (e.target.checked) {
            //se giá esiste non fare nulla
            if (!newData.categories.find((s) => s === category.id))
                newData.categories.push(category.id);
        } else
            //se non selezionato rimuovi dalla lista
            newData.categories = newData.categories.filter((s) => s !== category.id);

        setData(newData);
    };

    function handleEditCoordinates() {
        setError((error) => ({...error, address: null}));
        if (isEmpty(data.address)) {
            setError((error) => ({...error, address: MANDATORY}));
            return;
        }
        //ricerca le cooridnate in base all'address solo se l'address e' stato cambiato
        if (!data.latitude || !data.longitude) {
            post(VENDORS + '/' + COORDINATES, {body: data.address}).then((coordinates) => {
                setData((data) => ({...data, latitude: coordinates.lat, longitude: coordinates.lng}));
                setOpenMap(true);
            });
        } else {
            setOpenMap(true);
        }
    }

    const save = () => {
        let newShop = {...data, ...showcases};
        newShop.address = newShop.isEcommerce ? null : newShop.address;
        newShop.isPrimary = isFirstShop || null;//setta isPrimary se é il primo negozio
        newShop.id = shop.id;

        //--------controlli----------------------
        let valid = true;
        setError({});
        if (isEmpty(newShop.name)) {
            setError((error) => ({...error, name: MANDATORY}));
            valid = false;
        }
        if (isEmpty(newShop.website)) {
            setError((error) => ({...error, website: MANDATORY}));
            valid = false;
        } else if (!isValidHttpUrl(newShop.website)) {
            setError((error) => ({...error, website: INVALID_URL}));
            valid = false;
        }
        if (isEmpty(newShop.claim)) {
            setError((error) => ({...error, claim: MANDATORY}));
            valid = false;
        }
        if (isEmpty(newShop.description)) {
            setError((error) => ({...error, description: MANDATORY}));
            valid = false;
        }
        if (!newShop.images || newShop.images.length === 0) {
            setError((error) => ({...error, images: MANDATORY}));
            valid = false;
        }
        if (isEmpty(newShop.logo)) {
            setError((error) => ({...error, logo: MANDATORY}));
            valid = false;
        }
        if (!newShop.isEcommerce && isEmpty(newShop.address)) {
            setError((error) => ({...error, address: MANDATORY}));
            valid = false;
        }
        if (isEmpty(newShop.phone)) {
            setError((error) => ({...error, phone: MANDATORY}));
            valid = false;
        } else if (!isValidPhone(newShop.phone)) {
            setError((error) => ({...error, phone: INVALID_PHONE}));
            valid = false;
        }
        if (isEmpty(newShop.categories)) {
            setError((error) => ({...error, categories: MANDATORY}));
            valid = false;
        }

        if (!valid)
            return;
        //-----------

        (newShop.id ? put : post)(VENDORS + '/' + uid + '/' + SHOPS, {
            body: newShop,
            elem: newShop.id || ''
        })
            .then(() => goBack())
            .catch(() => enqueueSnackbar("Errore nel salvataggio dei dati", {variant: "error"}));
    }


    return (
        (<div>
            {
                !isFirstShop &&
                <Grid container justifyContent="space-between" className={classes.marginBottom}>
                    <Button
                        onClick={(e) => {
                            //todo: se elimino un immagine, e torno indietro l'immagine verra' persa
                            removeImagesFirebase(data.addedImages)//rimuovi le immagini aggiunte che non verranno salvare
                            goBack(e);
                        }}
                        className={classes.backButton}
                    >
                        <ArrowBackIcon className={classes.backButtonIcon}/>
                        Torna indietro
                    </Button>
                </Grid>
            }
            {/*----primo shop -----*/}
            {
                isFirstShop &&
                <Grid item xs={12} className={classes.title}>
                    <Typography variant={'h5'}>Crea il tuo primo negozio</Typography>
                    <Typography variant={'caption'}> potrà essere usato come modello per i prossimi negozi. <br/> I
                        valori inseriti potranno essere modificati in seguito </Typography>
                </Grid>
            }
            {/*valori di default*/}
            {
                (defShop && !isAdmin) &&
                <Grid container justifyContent="space-between" className={classes.marginBottom}>
                    <Grid item>
                        <Button
                            onClick={(e) => {
                                //imposta data come defShop
                                setData({...defShop, addedImages: [...defShop.images, defShop.logo]});
                                setShowCases({images: defShop.images, logo: defShop.logo});
                            }}
                            className={classes.backButton}
                        >
                            <Album className={classes.backButtonIcon}/>
                            Imposta valori predefiniti
                        </Button>
                    </Grid>
                </Grid>
            }
            <Grid container
                  direction="row"
                  alignItems="center"
            >
                {/*--- nome negozio --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Nome Negozio"}
                        type="text"
                        color={'primary'}
                        helperText="Questo è il nome che appare agli utenti"
                        value={data.name || ''}
                        onTextChange={(text) => handleOnChange(text, 'name')}
                        error={error.name}
                    />
                </Grid>

                {/*--- categoria ---*/}
                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <FormControl className={classes.formControl}>
                        <FormLabel component="legend">Categoria negozio</FormLabel>
                        <FormGroup>
                            {
                                allCategories.map(category =>
                                    <FormControlLabel
                                        key={category.id}
                                        control={
                                            <Checkbox
                                                checked={!!data.categories.find(c => c === category.id)}
                                                onChange={(e) => handleOnChangeCheckbox(e, category)}
                                                name={category.name}
                                                disabled={isAdmin}
                                            />
                                        }
                                        label={category.name}
                                    />
                                )
                            }
                        </FormGroup>
                    </FormControl>
                    {error.categories &&
                    <Typography variant={'caption'} className={classes.error}>{error.categories}</Typography>}
                </Grid>

                {/*--- indirizzo web --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Indirizzo web"}
                        type="text"
                        color={'primary'}
                        value={data.website || ''}
                        onTextChange={(text) => handleOnChange(text, 'website')}
                        error={error.website}
                        disabled={isAdmin}
                    />
                </Grid>

                {/*--- claim --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Claim"}
                        helperText="Inserisci un tuo slogan"
                        type="text"
                        color={'primary'}
                        value={data.claim || ''}
                        onTextChange={(text) => handleOnChange(text, 'claim')}
                        error={error.claim}
                        disabled={isAdmin}
                    />
                </Grid>

                {/*--- descrizione --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Descrizione"}
                        type="text"
                        color={'primary'}
                        value={data.description || ''}
                        onTextChange={(text) => handleOnChange(text, 'description')}
                        error={error.description}
                        disabled={isAdmin}
                    />
                </Grid>

                {/*--- telefono --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Telefono"}
                        type="text"
                        color={'primary'}
                        value={data.phone || ''}
                        onTextChange={(text) => handleOnChange(text, 'phone')}
                        error={error.phone}
                        disabled={isAdmin}
                    />
                </Grid>

                {/*--- is Ecommerce --*/}
                <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <FormControl component="fieldset" className={classes.formControl}>
                        <RadioGroup aria-label="Negozio fisico o E-Commerce" name="tipoNegozio"
                                    value={data.isEcommerce ? 'ecommerce' : 'fisico' || ''}
                                    onChange={(e) => handleOnChange(e.target.value === 'ecommerce', 'isEcommerce')}>
                            <FormControlLabel value='ecommerce' control={<Radio disabled={isAdmin}/>}
                                              label="E-commerce"/>
                            <FormControlLabel value='fisico' control={<Radio disabled={isAdmin}/>}
                                              label="Negozio Fisico"/>
                        </RadioGroup>
                    </FormControl>
                </Grid>

                {
                    //mostro i dati del comune solo se non àe un ecommerce
                    !data.isEcommerce &&
                    (
                        <>
                            {/*/!*--- comune --*!/*/}
                            {/*<Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>*/}
                            {/*    <TextInput*/}
                            {/*        label={"Comune del negozio"}*/}
                            {/*        type="text"*/}
                            {/*        color={'primary'}*/}
                            {/*        value={data.town || ''}*/}
                            {/*        onTextChange={(text) => handleOnChange(text, 'town')}*/}
                            {/*    />*/}
                            {/*</Grid>*/}

                            {/*--- indirizzo --*/}
                            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                                <TextInput
                                    label={"Indirizzo"}
                                    type="text"
                                    color={'primary'}
                                    value={data.address || ''}
                                    onTextChange={(text) => {
                                        setData((data) => {
                                            return {...data, latitude: null, longitude: null}
                                        });
                                        handleOnChange(text, 'address');
                                    }}
                                    helperText={
                                        !isAdmin &&
                                        <Link onClick={handleEditCoordinates} to={'#'}>
                                            <Edit/> Modifica Posizione
                                        </Link>
                                    }
                                    error={error.address}
                                    disabled={isAdmin}
                                />
                            </Grid>
                        </>
                    )
                }

                {/*logo*/}
                <NTMImageInput
                    title={'Logo'}
                    images={showcases.logo}
                    isSingleImage={true}
                    prefix={shop.id}
                    folder={'shops/logos'}
                    onDeleteImage={(img, index) => {
                        if (defShop && !defShop.logo === img) {//se l'immagine che sto eliminando è uguale a quella del def shop vuol dire che ho inserito i dati di default e quindi non devo eliminare la foto da firebase
                            removeImagesFirebase([img])
                        }
                        setShowCases((showcases) => {
                            return {...showcases, logo: null};
                        });
                    }}
                    onAddImage={(e) => {
                        setData((data) => {
                            let addedImages = [...(data.addedImages || []), e]//inserisci le immagini aggiunte in un array che verrá utilizzato dopo per salvarle/eliminarle da firebase
                            return {...data, addedImages};
                        });
                        setShowCases((showcase) => {
                            return {...showcase, logo: e}
                        });
                    }}
                    error={error.logo}
                    disabled={isAdmin}
                />

                {/*images*/}
                <NTMImageInput
                    title={'Immagini del negozio'}
                    images={showcases.images || []}
                    isSingleImage={false}
                    prefix={shop.id}
                    folder={'shops/images'}
                    onDeleteImage={(img, index) => {
                        debugger
                        if (defShop && !defShop.images.find(i => i === img)) {//se l'immagine che sto eliminando è uguale a quella del def shop vuol dire che ho inserito i dati di default e quindi non devo eliminare la foto da firebase
                            removeImagesFirebase([img])
                        }
                        setShowCases(showcases => {
                            let images = showcases.images.filter((img, i) => i !== index);
                            return {...showcases, images};
                        });
                    }}
                    onAddImage={(e) => {
                        setData((data) => {
                            let addedImages = [...(data.addedImages || []), e]//inserisci le immagini aggiunte in un array che verrá utilizzato dopo per salvarle/eliminarle
                            return {...data, addedImages};
                        });
                        setShowCases((showcase) => {
                            let images = [...(showcase.images || []), e];
                            return {...showcase, images}
                        });
                    }}
                    error={error.images}
                    disabled={isAdmin}
                />

            </Grid>
            {
                !isAdmin &&
                <Grid container
                      justifyContent={'center'}
                      direction={'row'}
                >
                    <Grid item xs={12} md={3}>
                        <Button
                            fullWidth
                            variant="contained"
                            color="primary"
                            onClick={save}
                        >
                            Salva
                        </Button>
                    </Grid>
                </Grid>
            }
            <EditCoordinatesModal
                onClose={() => setOpenMap(false)}
                open={openMap}
                defaultLat={data.latitude}
                defaultLng={data.longitude}
                onSubmit={(lat, lng) => setData((data) => ({
                        ...data,
                        latitude: lat,
                        longitude: lng
                    })
                )}
            />
        </div>)
    );
}
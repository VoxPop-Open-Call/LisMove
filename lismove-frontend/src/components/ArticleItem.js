import React, {useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {isEmpty, stringToUnix, unixToString} from "../services/helper";
import {ERROR_FORM, MANDATORY} from "../constants/errorMessages";
import {Button, FormControlLabel, Grid, Switch} from "@mui/material";
import {Delete} from "@mui/icons-material";
import NTMExpansionPanel from "./NTMExpansionPanel";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import TextInput from "./forms/TextInput";
import {TextItem} from "./TextItem";
import dayjs from "dayjs";
import NTMImageInput from "./NTMImageInput";
import {useSnackbar} from "notistack";
import {MULT_VALUE_TO_POINT} from "../constants/articles";
import {removeImagesFirebase} from "../services/FirebaseManager";

const useStyles = makeStyles((theme) => ({
    marginBottom: {
        marginBottom: theme.spacing(2)
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
    textItem: {
        color: "white",
        borderColor: theme.palette.primary.light,
        height: "100%",
    },
}));

export default function ArticleItem({article, onSave, onDelete, isNew = false, disabled=false}) {
    const classes = useStyles();
    const {enqueueSnackbar} = useSnackbar();

    const [data, setData] = useState({...article});
    const [edited, setEdited] = useState(article.edited);
    const [expanded, setExpanded] = useState(isNew);
    const [errors, setErrors] = useState({});

    //all'inizio isExpire é undefined, quindi mostro la data di scadenza solo se esiste
    let isExpire = data.isExpire === undefined ? !!data.expirationDate : data.isExpire;//indica se l'articolo ha una data di scadenza
    let isNumbered = data.isNumbered === undefined ? !!data.numberArticles : data.isNumbered; //indica se l'articolo ha un numero massimo di elementi
    data.expirationDate = dayjs(new Date());

    const handleSetExpanded = (expanded) => {
        if(!isNew){//se è un nuovo articolo deve rimanere sempre aperto
            setExpanded(expanded);
        }
    }

    const handleOnChange = (text, field) => {
        if(!disabled){
            setEdited(true);
            setData(data => ({...data, [field]: text}));
        }
    };

    const handleDeleteImage = (img, index) => {
        removeImagesFirebase([img])//delete image from firebase
        setEdited(true);
        setData(data => {
            let newData = {...data};
            newData.image = null;
            return newData;
        });
    }

    const handleAddImage = (e) => {
        setEdited(true);
        setData((data) => {
            let newData = {...data};
            newData.image = e;
            return newData;
        });
    }

    const handleOnDelete = (e) => {
        e.stopPropagation();
        onDelete(article.id);
        setEdited(false);
    }

    const handleOnSave = () => {
        let newArticle = {...data};
        if (!isExpire) {
            newArticle.expirationDate = null;
        }
        if (!isNumbered) {
            newArticle.numberArticles = null;
        }

        //----- --- controllo -----
        let valid = true;
        let errors = {};
        if (isEmpty(data.title)) {
            errors.title = MANDATORY
            valid = false;
        }
        if (isEmpty(data.description)) {
            errors.description = MANDATORY;
            valid = false;
        }
        if (isEmpty(data.points)) {
            errors.points = MANDATORY;
            valid = false;
        }
        if (isExpire && isEmpty(data.expirationDate)) {
            errors.expirationDate = MANDATORY;
            valid = false;
        }
        if (isNumbered && isEmpty(data.numberArticles)) {
            errors.numberArticles = MANDATORY;
            valid = false;
        }
        if (isEmpty(data.image)) {
            errors.image = MANDATORY;
            valid = false;
        }

        setErrors({...errors});
        if (!valid) {
            enqueueSnackbar(ERROR_FORM, {variant: "error"});
            return;
        }
        //--------

        if(!isNew){
            setExpanded(false);
        }

        setEdited(false);

        onSave(newArticle);
    }

    function Title({children}) {
        return (
            <Grid>
                <Grid container direction={'row'} justifyContent={'center'} alignContent={'center'}
                      alignItems={'center'}>
                    <Grid item>
                        {children}
                    </Grid>
                    <Grid style={{flexGrow: 1}}>

                    </Grid>
                    <Grid>
                        {
                            !disabled &&
                            <Delete
                                onClick={(e) => {
                                    handleOnDelete(e);
                                }}
                            />
                        }
                    </Grid>
                </Grid>
            </Grid>
        );
    }

    return (
        <Grid item xs={12} md={6} key={article.id}
              className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
            <NTMExpansionPanel
                id={article.id}
                title={<Title >{(data.title || '') + (edited ? '*' : '')}</Title>}
                icon={data.image ?
                    <img src={data.image} alt="article image"
                         style={{verticalAlign: 'middle', width: '2em'}}/>
                    :
                    <ShoppingCartIcon style={{verticalAlign: 'middle'}}/>}
                expanded={expanded}
                setExpanded={handleSetExpanded}
            >
                <Grid container
                      direction="row"
                      alignItems="baseline"
                >
                    {/*--- titolo --*/}
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <TextInput
                            label={"Titolo"}
                            type="text"
                            value={data.title || ''}
                            onTextChange={(text) => handleOnChange(text, 'title')}
                            error={errors.title}
                            disabled={disabled}
                        />
                    </Grid>
                    {/*--- descrizione --*/}
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <TextInput
                            label={"Descrizione"}
                            type="text"
                            value={data.description || ''}
                            onTextChange={(text) => handleOnChange(text, 'description')}
                            error={errors.description}
                            disabled={disabled}
                        />
                    </Grid>
                    {/*--- valore --*/}
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <TextInput
                            label={"Valore in punti"}
                            type="number"
                            value={data.points || ''}
                            onTextChange={(text) => handleOnChange(text, 'points')}
                            error={errors.points}
                            disabled={disabled}
                        />
                        <TextItem value={(data.points || 0) / MULT_VALUE_TO_POINT + '€'}
                                  className={classes.textItem}
                                  label={"Valore in euro"} xs={12}/>
                    </Grid>
                    {/*--- data scadenza --*/}
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isExpire}
                                    onChange={(e) => handleOnChange(e.target.checked, 'isExpire')}
                                    name="isExpire"
                                    color="secondary"
                                />
                            }
                            label="Ha una data di scadenza"
                        />
                        {
                            isExpire &&
                            <TextInput
                                label={"Data di scadenza"}
                                type="date"
                                value={unixToString(data.expirationDate)}
                                onTextChange={(text) => handleOnChange(stringToUnix(text), 'expirationDate')}
                                error={errors.expirationDate}
                                disabled={disabled}
                            />
                        }
                    </Grid>
                    {/*--- numero elementi --*/}
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isNumbered}//all' inizio isExpire é undefined, quindi mostro la data di scadenza solo se esiste
                                    onChange={(e) => handleOnChange(e.target.checked, 'isNumbered')}
                                    name="isNumbered"
                                    color="secondary"
                                />
                            }
                            label="Ha una Numero massimo di elementi"
                        />
                        {
                            isNumbered &&
                            <TextInput
                                label={"Numero di elementi"}
                                type="number"
                                value={data.numberArticles || ''}
                                onTextChange={(text) => handleOnChange(text, 'numberArticles')}
                                error={errors.numberArticles}
                                disabled={disabled}
                            />
                        }
                    </Grid>

                    <Grid item xs={12}>
                        {/*immagine*/}
                        <NTMImageInput
                            title={'Immagine del articolo'}
                            images={data.image || ''}
                            isSingleImage={true}
                            prefix={article.id}
                            folder={'articles/images'}
                            classes={{slider: {width: '100%'}}}
                            onDeleteImage={handleDeleteImage}
                            onAddImage={handleAddImage}
                            error={errors.image}
                            disabled={disabled}
                        />
                    </Grid>
                    {
                        !disabled &&
                        <Grid container
                              justifyContent={'center'}
                              direction={'row'}
                        >
                            <Grid item xs={12} md={3}>
                                <Button
                                    fullWidth
                                    variant="contained"
                                    color="secondary"
                                    onClick={() => {
                                        handleOnSave();
                                    }}
                                >
                                    Salva
                                </Button>
                            </Grid>
                        </Grid>
                    }

                </Grid>
            </NTMExpansionPanel>
        </Grid>
    )
}
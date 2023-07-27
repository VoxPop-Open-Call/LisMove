import {Button, FormControlLabel, Grid, Switch, Typography} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import React, {useEffect, useState} from "react";
import TextInput from "../../../components/forms/TextInput";
import CircularLoading from "../../../components/CircularLoading";
import {useGetDummyArticles} from "../../../services/GetDummyData";
import NTMExpansionPanel from "../../../components/NTMExpansionPanel";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import {TextItem} from "../../../components/TextItem";
import FilterAndSearchBar from "../../../components/layout/FilterAndSearchBar";
import dayjs from "dayjs";
import NTMImageInput from "../../../components/NTMImageInput";
import StorefrontIcon from "@mui/icons-material/Storefront";
import {Delete} from "@mui/icons-material";
import {isEmpty, stringToUnix, unixToString} from "../../../services/helper";
import {useGetArticles} from "../../../services/ContentManager";
import {ARTICLES, deleteElem, post, put, SHOPS, VENDORS} from "../../../services/Client";
import {firebaseAuth} from "../../../firebase";
import {useSnackbar} from "notistack";
import {removeImagesFirebase} from "../../../services/FirebaseManager";
import {ERROR_FORM, MANDATORY} from "../../../constants/errorMessages";
import {useQueryClient} from "react-query";
import ArticleItem from "../../../components/ArticleItem";


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
    spacing: {
        width: '100%',
        height: '4em'
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
}));

export default function Article({isAdmin, shop, goBack}) {
    const classes = useStyles();
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    let defaultArticle = {
        edited: true
    };

    let {articles, status} = useGetArticles(shop.id);
    const [toCreate, setToCreate] = useState(false);

    const [search, setSearch] = useState('');

    //attendi che siano caricati
    if (status === 'loading')
        return <CircularLoading/>

    function handleNew() {
        setToCreate(true);
    }

    function save(values) {
        //se e' nuovo allora non avra' un id quindi sara' una post
        (values.id ? put : post)(SHOPS + '/' + shop.id + '/' + ARTICLES, {
            body: values,
            elem: values.id || ''
        })
            .then((saved) => {
                queryClient.invalidateQueries([ARTICLES, {shop: shop.id}]).then(() => setToCreate(false));
                enqueueSnackbar("Articolo salvato", {variant: 'success'});
            })
            .catch(() => enqueueSnackbar("Errore nel salvataggio dei dati", {variant: "error"}));
    }

    function deleteArticle(id) {
        if (id) {
            deleteElem(SHOPS + '/' + shop.id + '/' + ARTICLES, {elem: id})
                .then(() => {
                    queryClient.invalidateQueries(ARTICLES).then(() => setToCreate(false));
                })
                .catch((e) => console.log(e));
        } else {
            setToCreate(false);
        }
    }

    return (
        (<div>
            <Grid container justifyContent="space-between" className={classes.marginBottom}>
                <Button
                    onClick={(e) => {
                        goBack(e);
                    }}
                    className={classes.backButton}
                >
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
            </Grid>
            <Grid container
                  direction="row"
                  alignItems="baseline"
            >
                {
                    articles.length || toCreate ?
                        <>
                            {/*--ricerca con action add ---*/}
                            {
                                !isAdmin &&
                                <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                                    <FilterAndSearchBar
                                        onSearch={(search) => {
                                            setSearch(search)
                                        }}
                                        addElement={handleNew}
                                    />
                                </Grid>
                            }
                            {
                                toCreate &&
                                <ArticleItem disabled={isAdmin} article={{...defaultArticle}} onDelete={deleteArticle}
                                             onSave={save} isNew/>
                            }
                            {
                                articles.map((article) =>
                                    (
                                        (!search || (article.title || '').toLowerCase().includes(search.toLowerCase())) && //ricerca
                                        <ArticleItem disabled={isAdmin} article={article} onDelete={() => deleteArticle(article.id)}
                                                     onSave={save}/>
                                    )
                                )
                            }

                        </>
                        :
                        (
                            isAdmin ?
                                <Typography variant={'h6'}>Non sono presenti articoli</Typography>
                                :
                                <Grid style={{textAlign: 'center'}} item xs={12}>
                                    <Typography>Non hai ancora creato nessun articolo</Typography>
                                    <br/>
                                    <Button
                                        variant="contained"
                                        color={'primary'}
                                        onClick={handleNew}
                                    >
                                        Crea un articolo
                                    </Button>
                                </Grid>
                        )

                }
            </Grid>
        </div>)
    );
}
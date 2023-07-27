import React, {useContext, useEffect, useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {
    BottomNavigation, BottomNavigationAction,
    Button,
    ClickAwayListener,
    Grid,
    Hidden,
    IconButton,
    Paper,
    Tooltip,
    Typography
} from "@mui/material";
import TextInput from "../../components/forms/TextInput";
import InfoIcon from "@mui/icons-material/Info";
import StorefrontIcon from '@mui/icons-material/Storefront';
import DeleteIcon from "@mui/icons-material/Delete";
import FilterAndSearchBar from "../../components/layout/FilterAndSearchBar";
import WebAssetIcon from '@mui/icons-material/WebAsset';
import QrCodeScannerIcon from '@mui/icons-material/CameraAlt';
import ScanQrModal from "../../components/modals/ScanQrModal";
import {
    COORDINATES,
    COUPON,
    COUPONS,
    deleteElem,
    getErrorMessage,
    post,
    put,
    SHOPS,
    VENDORS
} from "../../services/Client";
import {firebaseAuth} from "../../firebase";
import {useGetShops} from "../../services/ContentManager";
import useGetDummyInsertions from "../../services/GetDummyData";
import CircularLoading from "../../components/CircularLoading";
import {useHistory, useParams} from "react-router-dom";
import {DeleteForever} from "@mui/icons-material";
import ArticleModal from "../../components/modals/ArticleModal";
import CouponRedeem from "../../components/modals/CouponRedeem";
import {isEmpty} from "../../services/helper";
import {MANDATORY} from "../../constants/errorMessages";
import {useSnackbar} from "notistack";
import {roles} from "../../services/ability";
import {UserContext} from "../App";
import {INSERTION_ROUTE, INSERTION_ROUTE_ADMIN, SHOP_ROUTE, SHOP_ROUTE_ADMIN} from "../../constants/vendors";
import {removeImagesFirebase} from "../../services/FirebaseManager";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import {setRouteUid} from "../../services/VendorManager";

const useStyles = makeStyles(theme => ({
    couponContainer: {
        backgroundColor: theme.palette.primary.light,
    },
    bold: {
        fontWeight: 'bold'
    },
    marginBottom: {
        marginBottom: theme.spacing(2)
    },
    textCenter: {
        textAlign: 'center'
    },
    padding: {
        padding: theme.spacing(3)
    },
    marginRight: {
        marginRight: theme.spacing(2)
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
    paddingSmall: {
        padding: theme.spacing(1)
    },
    center: {
        position: 'fixed',
        top: 'calc(50vh - 20px)',
        left: 'calc(50vw - 20px)'
    },
    bottomNavigation: {
        width: '100%',
        position: "fixed",
        bottom: 0,
        left: 0,
        zIndex: 100
    },
    spacing: {
        width: '100%',
        height: '5em'
    },
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },

}));

export default function Vendor({vendor,isAdmin}) {
    const classes = useStyles();
    let history = useHistory();
    const {enqueueSnackbar} = useSnackbar();
    const loggedUser = useContext(UserContext);

    let uid = vendor.uid

    let {shops, status: statusShops} = useGetShops(uid);
    //todo:dummy data
    //------------------------------------
    let {insertions, status: statusInsertions} = useGetDummyInsertions(uid);

    const [coupon, setCoupon] = useState();
    const [errorCoupon, setErrorCoupon] = useState();
    const [openInfo, setOpenInfo] = useState(false);

    let [shopsList, setShopsList] = useState([]);
    useEffect(() => {
        setShopsList(shops);
    }, [statusShops]);

    function handleDeleteShop(shop) {
        deleteElem(VENDORS + '/' + uid + '/' + SHOPS, {elem: shop.id})
            .then(() => {
                setShopsList(shopsList.filter(s => s.id !== shop.id));
                enqueueSnackbar('Negozio eliminato', {variant:"success"});
            })
            .catch((e) => {
                enqueueSnackbar('Negozio non eliminato, il negozio non deve contenre articoli per poter essere eliminato', {variant:"error"});
                console.log(e)
            })
    }

    let [insertionList, setInsertionList] = useState([]);
    useEffect(() => {
        setInsertionList(insertions);
    }, [statusInsertions]);

    function handleDeleteInsertion(insertion) {
        //todo: elimina insertion da db
        setInsertionList(insertionList.filter(s => s.id !== insertion.id));
    }

    let def = localStorage.getItem('navigationValue') || 'shops';
    def = 'shops';//todo:inserzioni
    const [navigationValue, setNavigationValue] = useState(def);
    const handleChangeNavigation = (event, newValue) => {
        localStorage.setItem('navigationValue', newValue)
        setNavigationValue(newValue);
    };

    const [openQr, setOpenQr] = useState(false);

    function handleOpenQr() {
        setOpenQr(true);
    }

    const [openArticle, setOpenArticle] = useState(false);
    const [couponArticle, setCouponArticle] = useState({});
    const [openCouponData, setOpenCouponData] = useState(false);
    const [couponData, setCouponData] = useState({});

    function handleConsumeCoupon() {
        setErrorCoupon('');
        if (isEmpty(coupon)) {//controllo che coupon sia pieno
            setErrorCoupon(MANDATORY);
            return
        }
        post(VENDORS + '/' + uid + '/' + COUPONS, {body: {code:coupon}})
            .then((res) => {
                let data = res.data;
                if (data.article) {//se è presente un articolo allora mostro la pagina dell' articolo
                    setCouponArticle(data.article);
                    setOpenArticle(true);
                } else {
                    setCouponData(data);
                    setOpenCouponData(true);
                }
            })
            .catch(e => {
                if(e.response.data.status >= 500){
                    setErrorCoupon(()=>'Errore interno');
                }else{
                    //mostro l'errore
                    setErrorCoupon(() => e.response.data.message);
                }
            });
    }

    const onShopClick = (shop) => {
        history.push((isAdmin ? setRouteUid(SHOP_ROUTE_ADMIN, uid) : setRouteUid(SHOP_ROUTE, uid)) + (shop.id || ''));
    };
    const onInsertionClick = (insertion) => {
        history.push((isAdmin ? setRouteUid(INSERTION_ROUTE_ADMIN,uid) : setRouteUid(INSERTION_ROUTE, uid) ) + (insertion.id || ''));
    };
    const handleSearchInsertion = (search) => {
        setInsertionList(insertions.filter(s => !search || s.title.toLowerCase().includes(search.toLowerCase())))
    }
    const handleSearchShops = (search) => {
        setShopsList(shops.filter(s => !search || s.name.toLowerCase().includes(search.toLowerCase())))
    }

    /**
     * mostra un elemento cliccabile con logo testo e pulsante di elimina
     * @param item
     * @param onCLick
     * @param logo
     * @param text
     * @param onDelete
     * @returns {JSX.Element}
     * @constructor
     */
    function ClickableItem({item, onCLick, logo, text, onDelete}) {
        const classes = useStyles();

        return (
            <Grid item
                  md={6}
                  xs={12}
                  className={[classes.marginBottom, classes.paddingMdUp].join(' ')}
            >
                {
                    <Paper
                        className={classes.paddingSmall}
                        style={{cursor: 'pointer'}}
                    >
                        <Grid container
                              direction="row"
                              alignItems="center"
                        >
                            <Grid item xs={2} style={{textAlign: 'center'}} onClick={() => {
                                onCLick(item)
                            }}>
                                {
                                    logo ?
                                        <img src={logo} alt="icon"
                                             style={{verticalAlign: 'middle', width: '100%'}}/>
                                        :
                                        <StorefrontIcon style={{verticalAlign: 'middle'}}/>
                                }
                            </Grid>
                            <Grid item xs={8} onClick={() => {
                                onCLick(item)
                            }}>
                                <Typography noWrap variant={'h6'} style={{textAlign: 'center'}}>
                                    {text}
                                </Typography>
                            </Grid>
                            {
                                //gli admin non possono modificare
                                !isAdmin &&
                                <Grid item xs={2}>
                                    {
                                        onDelete ?
                                            <IconButton onClick={() => onDelete(item)} size="large">
                                                <DeleteIcon/>
                                            </IconButton>
                                            :
                                            <Tooltip disableInteractive title="Non si può eliminare il primo negozio creato">
                                                <IconButton size="large">
                                                    <DeleteForever style={{opacity: 0.5}}/>
                                                </IconButton>
                                            </Tooltip>
                                    }
                                </Grid>
                            }
                        </Grid>
                    </Paper>
                }

            </Grid>
        );
    }

    //attendi che siano caricati i vendors
    if (statusShops === 'loading' || statusInsertions === 'loading')
        return <CircularLoading/>

    let tooltipText = 'se un utente viene nel tuo negozio con un codice coupon, inseriscilo nel campo sottostante per convalidarlo';

    return (
        <div>
            {isAdmin &&
            <Grid container justifyContent="space-between" className={classes.marginBottom}>
                <Button
                    onClick={(e) => {history.push('/vendors')}}
                    className={classes.backButton}
                >
                    <ArrowBackIcon className={classes.backButtonIcon}/>
                    Torna indietro
                </Button>
            </Grid>}
            <Grid>
                {
                    //gli admin non possono riscattare i codici
                    (vendor.enableCoupon && !isAdmin ) &&

                    <Grid container
                          direction="row"
                          justifyContent='center'
                          alignItems="center"
                          className={classes.marginBottom}
                    >
                        {/*-- text field con il coupon --*/}
                        <Paper className={[classes.couponContainer, classes.padding].join(' ')}>
                            <Grid container
                                  direction="row"
                                  justifyContent='center'
                                  alignItems="center"
                            >
                                <Grid item xs={12} className={classes.marginBottom}>
                                    <Grid container
                                          direction="row"
                                          justifyContent='center'
                                          alignItems="baseline"
                                    >
                                        <Typography className={classes.bold + ' ' + classes.textCenter}
                                                    variant={'h5'}>
                                            Convalida Coupon
                                        </Typography>

                                        {/*info tooltip icon*/}
                                        <Hidden mdUp>
                                            <ClickAwayListener onClickAway={() => setOpenInfo(false)}>
                                                <Tooltip disableInteractive
                                                    title={
                                                        <Typography variant={'caption'}>
                                                            {
                                                                tooltipText
                                                            }
                                                        </Typography>
                                                    }
                                                    PopperProps={{
                                                        disablePortal: true,
                                                    }}
                                                    disableFocusListener
                                                    disableHoverListener
                                                    disableTouchListener
                                                    onClose={() => setOpenInfo(false)}
                                                    open={openInfo}
                                                >

                                                    <IconButton onClick={() => setOpenInfo(true)} size="large">
                                                        <InfoIcon/>
                                                    </IconButton>
                                                </Tooltip>
                                            </ClickAwayListener>
                                        </Hidden>
                                    </Grid>
                                    {/*---- se é md o lg mostra il testo completo sopra il pulsante -----*/}
                                    <Hidden mdDown>
                                        <Grid>
                                            <Typography variant={'caption'}>
                                                {
                                                    tooltipText
                                                }
                                            </Typography>
                                        </Grid>
                                    </Hidden>
                                </Grid>
                                <Grid item xs={12} md={9} className={classes.paddingMdUp}>
                                    <TextInput
                                        label={"Inserisci il coupon"}
                                        type="text"
                                        onTextChange={(value) => {
                                            setCoupon(value);
                                            setErrorCoupon(null);
                                        }}
                                        color={'primary'}
                                        InputProps={{
                                            endAdornment: <IconButton onClick={handleOpenQr} size="large"><QrCodeScannerIcon/></IconButton>
                                        }}
                                        value={coupon || ''}
                                        error={errorCoupon}
                                    />
                                </Grid>
                                {/*-- in sm gli elementi sono in verticale quindi aggiungo un mergine sotto --*/}
                                <Hidden mdUp>
                                    <Grid item xs={12} className={classes.marginBottom}/>
                                </Hidden>
                                <Grid item xs={12} md={3} className={classes.paddingMdUp}>
                                    <Button
                                        fullWidth
                                        variant="contained"
                                        color="primary"
                                        onClick={handleConsumeCoupon}
                                    >
                                        Valida
                                    </Button>
                                </Grid>
                            </Grid>
                        </Paper>
                    </Grid>
                }

                {/*--  lista delle inserzioni  --*/}
                {
                    navigationValue === 'insertion' &&
                    (
                        insertions.length > 0 ?
                            <>
                                {/*--ricerca con action add ---*/}
                                <Grid className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                                    {
                                        !isAdmin && <FilterAndSearchBar
                                            onSearch={handleSearchInsertion}
                                            addElement={() => {
                                                onInsertionClick({})
                                            }}
                                            filters={[]}
                                        />
                                    }
                                </Grid>
                                <Grid container
                                      direction="row"
                                      justifyContent='flex-start'
                                      alignItems="center"
                                >
                                    {
                                        insertionList.map((i) => {
                                            return <ClickableItem key={i.id} item={i} logo={i.image}
                                                                  onCLick={onInsertionClick}
                                                                  text={i.title} onDelete={handleDeleteInsertion}/>
                                        })
                                    }
                                </Grid>
                            </>
                            :
                            <Grid style={{textAlign: 'center'}}>
                                <Typography>Non hai ancora creato nessuna inserzione</Typography>
                                <br/>
                                <Button
                                    variant="contained"
                                    color={'primary'}
                                    onCLick={() => onInsertionClick({})}
                                >
                                    Crea un inserzione
                                </Button>
                            </Grid>
                    )
                }

                {/*--   lista di negozi cliccabili  --*/}
                {
                    navigationValue === 'shops' &&
                    (
                        shops.length > 0 ?
                            <>
                                {/*--ricerca con action add ---*/}
                                <Grid className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                                    {!isAdmin &&
                                    <FilterAndSearchBar
                                        onSearch={handleSearchShops}
                                        addElement={() => {
                                            onShopClick({});
                                        }}
                                    />
                                    }
                                </Grid>
                                <Grid container
                                      direction="row"
                                      justifyContent='flex-start'
                                      alignItems="center"
                                >
                                    {
                                        shopsList.map((s) => {
                                            return <ClickableItem key={s.id} item={s} logo={s.logo}
                                                                  onCLick={onShopClick}
                                                                  text={s.name}
                                                                  onDelete={!s.isPrimary && handleDeleteShop}
                                            />
                                        })
                                    }
                                </Grid>
                            </>
                            :
                            //nessun negozio inserito
                            <Grid style={{textAlign: 'center'}}>
                                <Typography>Non hai ancora creato nessun negozio</Typography>
                                <br/>
                                <Button
                                    variant="contained"
                                    color={'primary'}
                                    onClick={() => onShopClick({})}
                                >
                                    Crea un negozio
                                </Button>
                            </Grid>
                    )
                }

                <Grid className={classes.spacing}>
                </Grid>
                {/* navigation*/}
                <Grid container>
                    {/*todo:inserzioni*/}
                    {/*<BottomNavigation value={navigationValue} onChange={handleChangeNavigation}*/}
                    {/*                  className={classes.bottomNavigation}*/}
                    {/*                  showLabels>*/}
                    {/*    <BottomNavigationAction label="Negozi" value="shops" icon={<StorefrontIcon/>}/>*/}
                    {/*    <BottomNavigationAction label="Inserzioni" value="insertion" icon={<WebAssetIcon/>}/>*/}
                    {/*</BottomNavigation>*/}
                </Grid>
            </Grid>
            <ScanQrModal open={openQr} onClose={() => setOpenQr(false)} onScan={(text) => setCoupon(text)}
                         onError={(e) => setErrorCoupon(e.message)}/>
            <ArticleModal open={openArticle} onClose={() => setOpenArticle(false)} article={couponArticle}/>
            <CouponRedeem open={openCouponData} onClose={() => setOpenCouponData(false)} coupon={couponData}/>
        </div>
    );
}

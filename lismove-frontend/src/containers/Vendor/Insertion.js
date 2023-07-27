import makeStyles from '@mui/styles/makeStyles';
import React, {useContext, useEffect, useState} from "react";
import {
    Button, FormControl,
    Grid, InputLabel, MenuItem, Select,
} from "@mui/material";
import TextInput from "../../components/forms/TextInput";
import SelectInput from "../../components/forms/SelectInput";
import useGetDummyInsertions, {useGetDummyShops} from "../../services/GetDummyData";
import CircularLoading from "../../components/CircularLoading";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import NTMSelect from "../../components/NTMSelect";
import {Redirect, Route, useHistory, useParams} from "react-router-dom";
import NTMImageInput from "../../components/NTMImageInput";
import {firebaseAuth, firebaseStorage} from "../../firebase";
import {removeImagesFirebase} from "../../services/FirebaseManager";
import {useGetCities, useGetShops} from "../../services/ContentManager";
import {roles} from "../../services/ability";
import {UserContext} from "../App";
import {INSERTIONS} from "../../services/Client";
import {VENDOR_ROUTE, VENDOR_ROUTE_ADMIN} from "../../constants/vendors";
import {useQueryClient} from "react-query";
import {setRouteUid} from "../../services/VendorManager";

const useStyles = makeStyles((theme) => ({
    backButton: {
        color: theme.palette.primary.main,
    },
    backButtonIcon: {
        margin: theme.spacing(1)
    },
    marginBottom: {
        marginBottom: theme.spacing(3)
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

//todo:disabilitare modifiche da parte di admin
export default function Insertion({vendor, isAdmin}) {
    const classes = useStyles();
    let uid = vendor.uid;
    let queryClient = useQueryClient();
    let history = useHistory()();

    let {cities} = useGetCities();
    let {shops, status} = useGetShops(uid);
    //todo:dummy data
    //------------------------------------
    let {insertions, status: statusInsertions} = useGetDummyInsertions(uid);
    //-----------------------------------

    let insertionDef = {cities:[], shops:[]};//parametri di default

    //la lista e' ereditata da VendorRoutes l'id e' dato dall'url
    const {id} = useParams();
    const [data, setData] = useState(insertionDef);

    useEffect(()=>{
        if(!data.isUpdated){
            if(statusInsertions === 'success'){
                if(id){
                    setData({... insertions.find(s => s.id + '' === id),  isUpdated: true });
                }else{
                    setData({... insertionDef, isUpdated: true});
                }
            }else if(statusInsertions === 'error'){
                setData(null);
            }
        }
    },[insertions]);

    // useEffect(() => {
    //     setData(insertion);
    // }, [insertion]);

    const handleOnChange = (text, field) => {
        setData((data)=>({...data, [field]: text}));
    };

    //attendi che siano caricate le categories
    if (status === 'loading' || statusInsertions === "loading")
        return <CircularLoading/>

    if(!data){
        return <Redirect exact to="/vendor"/>
    }

    const goBack = async () => {
        await queryClient.invalidateQueries(INSERTIONS);
        history.push(isAdmin ? setRouteUid(VENDOR_ROUTE_ADMIN, uid) : VENDOR_ROUTE);
    }

    return (
        (<div>
            <Grid container justifyContent="space-between" className={classes.marginBottom}>
                <Button
                    onClick={(e) => {
                        //rimuovi le immagini nuove che non verranno salvate
                        removeImagesFirebase(data.addedImages);
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
                {/*--- titolo --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Titolo"}
                        type="text"
                        value={data.title || ''}
                        onTextChange={(text) => handleOnChange(text, 'title')}
                        color={'primary'}
                    />
                </Grid>
                {/*--- testo --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Testo inserzione"}
                        type="text"
                        value={data.text || ''}
                        onTextChange={(text) => handleOnChange(text, 'text')}
                        color={'primary'}
                    />
                </Grid>
                {/*--- link --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextInput
                        label={"Link Inserzione"}
                        type="text"
                        value={data.link || ''}
                        onTextChange={(text) => handleOnChange(text, 'link')}
                        color={'primary'}
                    />
                </Grid>
                {/*--  Negozio associato  --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>

                    <NTMSelect
                        value={data.shops[0] || ''}
                        onChange={(value) => setData({...data, shops: [value]})}
                        label="Negozio associto"
                        items={shops.map(s => ({value: s.id, text: s.name}))}
                    >
                    </NTMSelect>
                </Grid>

                {/*--  Comuni  --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>

                    <NTMSelect
                        value={data.cities}
                        onChange={(value) => setData((data) => {
                            let newData = {...data};
                            newData.cities.push(value);
                            return newData;
                        })}
                        onDelete={(value) => setData((data) => ({...data, cities: data.cities.filter((c)=> value !== c)}))}
                        label="Comuni di visualizzazione"
                        items={cities.map(s => ({value: s.istatId, text: s.city}))}
                    >
                    </NTMSelect>
                </Grid>

                <Grid item xs={12}>
                    {/*immagine*/}
                    <NTMImageInput
                        title={'Immagine'}
                        images={data.image}
                        isSingleImage={true}
                        prefix={data.id}
                        folder={'insertions/images'}
                        onDeleteImage={(img, index) => {
                            removeImagesFirebase([img])//delete image from firebase
                            setData(data => {
                                let deletedImages = [...(data.deletedImages || []), img];//inserisci le immagini da eliminare se si salva il form
                                return {...data, deletedImages, image: null};
                            });
                        }}
                        onAddImage={(e) => {
                            setData((data) => {
                                return {
                                    ...data,
                                    image: e,
                                    addedImages: [...(data.addedImages || []), e]//inserisci le immagini aggiunte in un array che verrá utilizzato dopo per salvarle/eliminarle
                                };
                            });
                        }}
                    />
                </Grid>

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
                                let newData = {...data};
                                newData.cities = newData.cities || [];
                                newData.shops = newData.shops || [];

                                //todo:save on db

                                //save images on cloud
                                removeImagesFirebase(data.deletedImages);
                                goBack();
                            }}
                        >
                            Salva
                        </Button>
                    </Grid>
                </Grid>

            </Grid>
        </div>)
    );
}
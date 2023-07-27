import React, {useContext, useEffect, useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {
    BottomNavigation,
    BottomNavigationAction, Button, Checkbox,
    FormControl,
    FormControlLabel,
    FormGroup,
    FormLabel,
    Grid, IconButton, List, ListItem, Radio, RadioGroup, Typography
} from "@mui/material";
import EditIcon from '@mui/icons-material/Edit';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import TextInput from "../../../components/forms/TextInput";
import {useGetDummyCategories, useGetDummyShops} from "../../../services/GetDummyData";
import CircularLoading from "../../../components/CircularLoading";
import FileInput from "../../../components/forms/FileInput";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import HoverableIconButton from "../../../components/hoverables/HoverableIconButton";
import {Delete} from "@mui/icons-material";
import {firebaseAuth, firebaseStorage} from "../../../firebase";
import ShopData from "./ShopData";
import Article from "./Article";
import {Redirect, useHistory, useParams} from "react-router-dom";
import {useGetShops, useGetVendors} from "../../../services/ContentManager";
import {roles} from "../../../services/ability";
import {UserContext} from "../../App";
import {SHOPS} from "../../../services/Client";
import {VENDOR_ROUTE, VENDOR_ROUTE_ADMIN} from "../../../constants/vendors";
import {useQueryClient} from "react-query";
import {setRouteUid} from "../../../services/VendorManager";

const useStyles = makeStyles(theme => ({
    bottomNavigation: {
        width: '100%',
        position: "fixed",
        bottom: 0,
        left: 0,
        zIndex: 100
    },
    spacing: {
        width: '100%',
        height: '4em'
    },
}));

export default function Shop({vendor, isAdmin}) {
    let uid = vendor.uid;
    let queryClient = useQueryClient();
    let history = useHistory();

    let {shops, status} = useGetShops(uid);

    const classes = useStyles();

    const {id} = useParams();
    let defShop = null;

    //se non c'é un id allora é una new
    let shop = {images: [], categories: []};//valori di default
    if (id) {
        shop = shops.find(s => s.id + '' === id);
    } else {
        //il primary shop e' utilizzato come valore di default per gli shop nuovi
        defShop = shops.find(s => s.isPrimary === true)
    }
    const [value, setValue] = React.useState('shop');

    if (!shop) {
        return <Redirect exact to="/vendor"/>
    }

    const handleChangeNavigation = (event, newValue) => {
        setValue(newValue);
    };

    const goBack = async () => {
        await queryClient.invalidateQueries([SHOPS,{vendor:uid}]);
        history.push(isAdmin ? setRouteUid(VENDOR_ROUTE_ADMIN,uid) : VENDOR_ROUTE);
    }

    //attendi che siano caricati i vendors
    if (status === 'loading')
        return <CircularLoading/>

    return (
        (<div>
            {value === 'shop' && <ShopData vendor={vendor} isAdmin={isAdmin} shop={shop} goBack={goBack} defShop={defShop}/>}
            {value === 'article' && <Article isAdmin={isAdmin} shop={shop} goBack={goBack}/>}
            <Grid className={classes.spacing}>
            </Grid>
            {/*buttom navigation*/}
            <Grid container>
                <BottomNavigation value={value} onChange={handleChangeNavigation} className={classes.bottomNavigation}
                                  showLabels>
                    <BottomNavigationAction label="Negozio" value="shop" icon={<EditIcon/>}/>
                    <BottomNavigationAction label="Articoli" value="article" icon={<ShoppingCartIcon/>}/>
                </BottomNavigation>
            </Grid>
        </div>)
    );
}
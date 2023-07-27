import React, {useContext, useState} from "react";
import Vendor from "./Vendor";
import Shop from "./shop/Shop";
import {firebaseAuth} from "../../firebase";
import {Route, useHistory, useParams, Switch, Redirect} from "react-router-dom";
import useGetDummyInsertions, {useGetDummyShops, useGetDummyVendor} from "../../services/GetDummyData";
import CircularLoading from "../../components/CircularLoading";
import Insertion from "./Insertion";
import ShopData from "./shop/ShopData";
import {useGetShops, useGetVendors} from "../../services/ContentManager";
import {useQueryClient} from "react-query";
import {INSERTIONS, SHOPS} from "../../services/Client";
import {roles} from "../../services/ability";
import {UserContext} from "../App";
import {
    INSERTION_ROUTE,
    INSERTION_ROUTE_ADMIN,
    SHOP_ROUTE,
    SHOP_ROUTE_ADMIN,
    VENDOR_ROUTE,
    VENDOR_ROUTE_ADMIN
} from "../../constants/vendors";
import {Typography} from "@mui/material";

export default function VendorRoutes() {
    let history = useHistory();
    const loggedUser = useContext(UserContext);
    let isAdmin = loggedUser.userType === roles.ADMIN;

    let uid = firebaseAuth.currentUser.uid;

    //---se l'utente è un admin allora prendo l'uid dai parametri dell'url
    const {uid : paramUid} = useParams();
    if(isAdmin && paramUid){
        uid = paramUid;
    }

    let {vendors, status} = useGetVendors(uid);
    let {shops, status: statusShops} = useGetShops(uid);
    let queryClient = useQueryClient();

    //attendi che siano caricati i vendors
    if (status === 'loading' || statusShops === 'loading' || !uid)
        return <CircularLoading/>

    if(isAdmin && shops.length === 0)
        return <Typography variant={'h5'}> Non sono presenti negozi </Typography>

    if(!vendors)
        history.push('/404');

    //usa la history per capire se viene selezionato uno shop o un insertion
    return (
        //se non ci sono shop allora vuol dire che il vendor e' stato appena creato quindi mostro la pagina di creazione dello shop
        shops.length === 0?
            <ShopData shop={{categories:[]}} goBack={() => {
                history.push(VENDOR_ROUTE);
            }} isFirstShop/>
            :
            <Switch>
                <Route path={isAdmin ? SHOP_ROUTE_ADMIN + ':id?' : SHOP_ROUTE + ':id?'}>
                    <Shop
                        vendor={vendors}
                        isAdmin={isAdmin}
                    />
                </Route>
                <Route path={isAdmin ? INSERTION_ROUTE_ADMIN + ':id?' : INSERTION_ROUTE + ':id?'}>
                    <Insertion
                        vendor={vendors}
                        isAdmin={isAdmin}
                    />
                </Route>
                <Route path={isAdmin ? VENDOR_ROUTE_ADMIN : VENDOR_ROUTE}>
                    <Vendor
                        vendor={vendors}
                        isAdmin={isAdmin}
                    />
                </Route>
            </Switch>
    );
}
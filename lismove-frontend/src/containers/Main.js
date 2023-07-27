import React, {useContext} from 'react';
import makeStyles from '@mui/styles/makeStyles';
import Sidebar from "../components/layout/Sidebar";
import {setMenu} from "../services/MenuManager";
import {CssBaseline} from "@mui/material";
import Header from "../components/layout/Header";
import {Redirect, Route, Switch} from "react-router-dom";
import {UserManagement} from "./UserManagement";
import ProfilePage from "./Users/ProfilePage";
import {SessionManagement} from "./SessionManagement";
import SessionInfo from "./SessionManagement/SessionInfo";
import Page404 from "../components/pages/Page404";
import {OrganizationsList} from "./OrganizationsList";
import Dashboard from "./Dashboard";
import {Revolut} from "./Revolut";
import {OfflineSessionPage} from "./OfflineSessionPage";
import {resources} from "../services/ability";
import NationalRanksManagement from "./NationalRanksManagement";
import {AbilityContext} from "../services/Can";
import NationalAchievementsManagement from "./NationalAchievementsManagement";
import {UserContext} from "./App";
import DrinkingFountains from "./DrinkingFountains";
import VendorRoutes from "./Vendor/VendorRoutes";
import CustomAwards from "./CustomAwards";
import VendorManagement from "./VendorManagement";
import CouponManagement from './CouponManagement';

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        minHeight: '100vh'
    },
    toolbar: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
    },
    content: {
        flexGrow: 1,
        padding: theme.spacing(3),
    },
}));

export default function Main() {

    const ability = useContext(AbilityContext);
    const [open, setOpen] = React.useState(false);
    const classes = useStyles();
    const loggedUser = useContext(UserContext);

    let menu = setMenu(ability);

    return (
        <div className={classes.root}>
            <CssBaseline/>
            <Header menuOpen={open} toggleMenu={setOpen}/>
            <Sidebar open={open} setOpen={setOpen} items={menu}/>

            <main className={classes.content}>
                <div className={classes.toolbar}/>
                <Switch>

                    <Route path={"/dashboard/:id/:filter"}>
                        <Dashboard/>
                    </Route>
                    <Redirect from="/dashboard/:id" to="/dashboard/:id/details"/>
                    <Route path={"/dashboard"}>
                        <OrganizationsList/>
                    </Route>
                    <Route path={"/sessions/:id"}>
                        <SessionInfo/>
                    </Route>
                    <Route path={"/profile/:uid/:filter"}>
                        <ProfilePage/>
                    </Route>
                    <Redirect from="/profile/:uid" to="/profile/:uid/sensors"/>
                    <Redirect from="/profile" to={"/profile/" + loggedUser.uid + "/sensors"}/>
                    <Route path={"/sessions"}>
                        <SessionManagement/>
                    </Route>
                    <Route path={"/customAwards"}>
                        <CustomAwards/>
                    </Route>
                    <Route path={"/nationalRank"}>
                        {ability.can('read', resources.NATIONALRANKS) ?
                            <NationalRanksManagement/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/nationalAchievements"}>
                        {ability.can('read', resources.NATIONALACHIEVEMENTS) ?
                            <NationalAchievementsManagement/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/drinkingFountains"}>
                        {ability.can('read', resources.DRINKINGFOUNTAINS) ?
                            <DrinkingFountains/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/users"}>
                        <UserManagement/>
                    </Route>
                    <Route path={"/revolut"}>
                        {ability.can('read', resources.REVOLUT) ?
                            <Revolut/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/debug"}>
                        {ability.can('read', resources.DEBUG) ?
                            <OfflineSessionPage/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/vendors/:uid"}>
                        {ability.can('read', resources.VENDOR) ?
                            <VendorRoutes/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Route path={"/vendors"}>
                        {ability.can('read', resources.VENDOR) ?
                            <VendorManagement/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>

                    <Route path={"/coupons"}>
                        {ability.can('read', resources.COUPONS) ?
                            <CouponManagement/> :
                            <Redirect exact from="/" to="/dashboard"/>}
                    </Route>
                    <Redirect exact from="/" to="/dashboard"/>
                    <Route path={"/"}>
                        <Page404/>
                    </Route>

                </Switch>
            </main>
        </div>
    );
}

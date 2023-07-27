import React,{useContext} from 'react';
import makeStyles from '@mui/styles/makeStyles';
import {CssBaseline} from "@mui/material";
import Header from "../components/layout/Header";
import {Redirect,Route,Switch} from "react-router-dom";
import Page404 from "../components/pages/Page404";
import {resources} from "../services/ability";
import {AbilityContext} from "../services/Can";
import {UserContext} from "./App";
import VendorRoutes from "./Vendor/VendorRoutes";
import VendorProfile from "./Vendor/VendorProfile";

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

export default function MainVendor() {

    const ability = useContext(AbilityContext);
    const [open, setOpen] = React.useState(false);
    const classes = useStyles();
    const loggedUser = useContext(UserContext);

    return (
        <div className={classes.root}>
            <CssBaseline/>
            <Header menuOpen={open} toggleMenu={setOpen} noMenu />

            <main className={classes.content}>
                <div className={classes.toolbar}/>
                <Switch>
                    <Route path={"/vendor"}>
                        {ability.can('read', resources.VENDOR) ?
                            <VendorRoutes /> :
                            <Redirect exact to="/unutorized"/>
                        }
                    </Route>
                    <Route path={"/profile/:uid"}>
                        {ability.can('read', resources.VENDOR_PROFILE) ?
                            <VendorProfile /> :
                            <Redirect exact to="/unutorized"/>
                        }
                    </Route>
                    <Redirect exact from="/" to="/vendor"/>
                    <Route path={"/"}>
                        <Page404/>
                    </Route>

                </Switch>
            </main>
        </div>
    );
}

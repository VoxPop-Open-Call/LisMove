import React, {useContext, useEffect, useState} from 'react';
import {
    BrowserRouter as Router,
    Route,
    Redirect,Switch
} from "react-router-dom";

import Main from "./Main";
import {firebaseAuth,getErrorMessage} from "../firebase";
import {LoginPage} from "../components/pages/LoginPage";
import {USERS, get} from "../services/Client";
import {AbilityContext} from "../services/Can";
import ability, {roles} from "../services/ability";
import MainVendor from "./MainVendor";
import NewVendor from "./Vendor/NewVendor";
import ConfirmEmail from "../components/pages/ConfirmEmail";
import {useGetCities} from "../services/ContentManager";
import ConfirmFormComplete from "./Forms/ConfirmFormComplete";

export const UserContext = React.createContext({});

function checkUser (user, setUser, setError){
    if(user && user.uid){
        get(USERS ,{elem: user.uid}).then(user => {
            if(user.userType === "LISMOVER" || (user.userType === "VENDOR" && !firebaseAuth.currentUser.emailVerified)){
                setUser({isLogged: false, ready: true})
                setError("Utente non abilitato")
                firebaseAuth.signOut()
            } else {
                setUser({...user, isLogged: true, ready: true});
            }
        });
    } else setUser({isLogged: false, ready: true})
}

function App() {
    let [user, setUser]= useState({ready: false})
    let [error, setError] = useState(null)
    let {cities = []} = useGetCities();

    useEffect(() => firebaseAuth.onAuthStateChanged(user => {
        checkUser(user, setUser, setError);
    }),[])

    function onLogin(username, password) {
        firebaseAuth.signInWithEmailAndPassword(username,password).then(user => checkUser(user, setUser, setError)).catch(e => setError(getErrorMessage(e)))
    }

    if(!user.ready) return <div/>

    return (
    <Router>
        <UserContext.Provider value={user}>
            <Switch>
                <UnloggedRoute path="/login" setError={setError} redirect>
                    <LoginPage onSubmit={onLogin}
                               error={error}/>
                </UnloggedRoute>
                <UnloggedRoute path="/newVendor" setError={setError} redirect>
                    <NewVendor/>
                </UnloggedRoute>
                <UnloggedRoute path="/confirmEmail" setError={setError} redirect>
                    <ConfirmEmail/>
                </UnloggedRoute>
                <UnloggedRoute path="/confirmFormComplete/:aid" setError={setError}>
                    <ConfirmFormComplete/>
                </UnloggedRoute>
                <AbilityContext.Provider value={ability(user)}>
                    <PrivateRoute path="/">
                        {
                            user.userType === roles.VENDOR?
                                <MainVendor/>
                                :
                                <Main/>
                        }
                    </PrivateRoute>
                </AbilityContext.Provider>
            </Switch>
</UserContext.Provider>
    </Router>
    );
}

// A wrapper for <Route> that redirects to the login
// screen if you're not yet authenticated.
function PrivateRoute({ children, ...rest }) {
    let user = useContext(UserContext) || {};

    return (
        <Route
            {...rest}
            render={({ location }) =>
                user.isLogged ? (
                    children
                ) : (
                    <Redirect
                        to={{
                            pathname: "/login",
                            state: { from: location }
                          }}
                    />
                    )
            }
        />
  );
}

function UnloggedRoute({redirect, children, setError, ...rest }) {
    const user = useContext(UserContext) || {};

    return (
        <Route
            {...rest}
            render={() =>
                user.isLogged && redirect ? (
                    <Redirect
                        to={{
                            pathname: "/"
                        }}
                    />
                ) : (
                    children
                )
            }
        />
    );
}

export default App;

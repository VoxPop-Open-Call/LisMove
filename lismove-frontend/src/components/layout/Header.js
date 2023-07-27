import React, {useContext} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {Link} from "react-router-dom";
import clsx from "clsx";
import Toolbar from "@mui/material/Toolbar";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import AppBar from "@mui/material/AppBar";
import HoverableButton from "./HoverableButton";
import PersonIcon from '@mui/icons-material/Person';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import logo from "../../images/lismove.gif"
import {firebaseAuth} from "../../firebase";
import {useGetProfileUser} from "../../services/ContentManager";
import HoverableProfileButton from "./HoverableProfileButton";
import {UserContext} from "../../containers/App";
import {Hidden} from "@mui/material";

const drawerWidth = 240;

const useStyles = makeStyles((theme) => ({
    appBar: {
        height: "4rem",
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
    },
    appBarShift: {
        marginLeft: drawerWidth,
        // width: `calc(100% - ${drawerWidth}px)`,
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(
            ['width', 'margin'], {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.enteringScreen,
            }),
    },
    menuButton: {
        [theme.breakpoints.up('md')]: {
            marginRight: theme.spacing(4),
        }
    },
    hide: {
        display: 'none',
    },
    toolbar: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
    },
    logo: {
        height: '7rem',
        [theme.breakpoints.down('xs')]: {
            width: '100%',
            height: 'unset',
            maxWidth: 200
        }
    },
    iconLogout: {
        color: "white",
        display: "flex",
        justifyContent: "center",
        [theme.breakpoints.down('xs')]: {
            padding: theme.spacing(2) + 'px 0 ' + theme.spacing(2) + 'px ' + theme.spacing(2) + 'px ',
        },
        padding: theme.spacing(2),
        '&:hover': {
            color: theme.palette.primary.main
        }
    }
}));

export default function Header({menuOpen, toggleMenu, noMenu = false}) {
    const classes = useStyles();
    const userData = useContext(UserContext);
    let {user = {}} = useGetProfileUser(userData.uid);

    return (
        <AppBar
            position="fixed"
            color="secondary"
            className={clsx(classes.appBar, {
                [classes.appBarShift]: menuOpen,
            })}
        >
            <Toolbar>
                {
                    !noMenu &&
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        onClick={() => toggleMenu(!menuOpen)}
                        edge="start"
                        className={clsx(classes.menuButton)}
                        size="large">
                        <MenuIcon/>
                    </IconButton>

                }
                <Link to="/">
                    <img className={classes.logo} alt={"home"} src={logo}/>
                </Link>
                <div style={{flexGrow: 1}}/>
                <Link to={"/profile/" + user.uid} style={{textDecoration: "none"}}>
                    {
                        <HoverableProfileButton
                            item={{
                                name: `${user.username}`,
                                icon: <PersonIcon/>,
                                avatarUrl: user.avatarUrl
                            }}
                        />}
                </Link>
                <HoverableButton
                    item={{
                        name: <Hidden smDown> LogOut </Hidden>,
                        icon: <ExitToAppIcon/>
                    }}
                    onClick={() => firebaseAuth.signOut()}
                    classes={{root: classes.iconLogout}}
                />
            </Toolbar>
        </AppBar>
    );
}

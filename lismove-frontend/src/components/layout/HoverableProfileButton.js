import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import Icon from "@mui/material/Icon";
import clsx from "clsx";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import {Avatar, Hidden} from "@mui/material";

const useStyles = makeStyles(theme => ({
    root: {
        color:"white",
        display: "flex",
        justifyContent: "center",
        padding:theme.spacing(2),
        '&:hover': {
            color: theme.palette.primary.main
        }
    },
    imageIcon: {
        height: '100%'
    },
    iconRoot: {
        display: "flex"
    },
    imageText: {
        margin:0,
        paddingLeft: "1rem",
    }
}));

export default function HoverableProfileButton({item, onClick, classes={}}) {

    let innerClasses = useStyles();

    return (
        <Button
            className={classes.root || innerClasses.root}
            onClick={onClick}
        >
            {item.avatarUrl ?
                <Avatar src={item.avatarUrl} alt="userImage"/>
                :
                <Icon classes={{root : innerClasses.iconRoot}}>
                    {item.icon}
                </Icon>}
            <Hidden smDown>
                <Typography className={clsx(innerClasses.imageText,classes.text)}>{item.name}</Typography>
            </Hidden>
        </Button>
    );
}
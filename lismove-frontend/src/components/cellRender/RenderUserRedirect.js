import makeStyles from '@mui/styles/makeStyles';
import React from "react";
import HoverableButton from "../layout/HoverableButton";
import InfoIcon from "@mui/icons-material/Info";
import {useGetProfileUser} from "../../services/ContentManager";
import Link from "@mui/material/Link";

const useStyles = makeStyles(theme => ({
    root: {
        color: theme.palette.primary.main,
        '&:hover': {
            color: theme.palette.secondary.main
        }
    }
}));

export default function RenderUserRedirect({value, url, showUsername}){

    let classes = useStyles();
    let {user = {}} = useGetProfileUser(url);

    const displayingValue = showUsername ? user.username : (value || "")

    if(!value) return "";
    return (
        <div>
            <Link href={"/profile/" + url} target="_blank" underline='none'>
                <HoverableButton
                    item={{
                        icon : <InfoIcon/>,
                        name : displayingValue
                    }}
                    classes={{root : classes.root}}
                />
            </Link>
        </div>
    );
}

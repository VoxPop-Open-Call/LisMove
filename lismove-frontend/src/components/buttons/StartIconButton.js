import Button from "@mui/material/Button";
import makeStyles from '@mui/styles/makeStyles';
import React from "react";
import clsx from "clsx";

const useStyles = makeStyles(theme => ({
    button: {
        margin: theme.spacing(1),
    },
}));

export default function StartIconButton({startIcon, onClick, title, href, target, disabled, size, className}){

    let classes = useStyles();


    return (
        <Button
            variant="outlined"
            color="primary"
            startIcon={startIcon}
            onClick={onClick}
            className={clsx(classes.button, className)}
            href={href}
            target={target}
            disabled={disabled}
            size={size}
        >
            {title}
        </Button>
    );
}
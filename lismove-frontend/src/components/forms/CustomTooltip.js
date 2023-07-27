import withStyles from '@mui/styles/withStyles';
import {Tooltip} from "@mui/material";
import React from "react";

export const CustomTooltip = withStyles(theme => ({
    arrow: {
        color: theme.palette.primary.contrastText,
    },
    tooltip: {
        backgroundColor: theme.palette.primary.contrastText,
        color: theme.palette.text.primary,
    },
    tooltipPlacementTop: {
        marginBottom: "-0.6rem"
    },
}))(({ classes, title, children }) => (
    <Tooltip placement="top" classes={classes} title={title}>
        {children}
    </Tooltip>
));
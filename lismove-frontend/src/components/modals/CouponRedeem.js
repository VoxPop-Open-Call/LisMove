import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import React from "react";
import {Grid} from "@mui/material";
import {TextItem} from "../TextItem";
import {unixToString} from "../../services/helper";

const useStyles = makeStyles((theme) => ({
    marginBottom: {
        marginBottom: theme.spacing(2)
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
    textItem: {
        color: "white",
        borderColor: theme.palette.primary.light,
        height: "100%",
    },
}));

export default function CouponRedeem({open, onClose, coupon}) {
    const classes = useStyles();
    return (
        <BaseModal open={open} onClose={onClose}>
            <Grid container>
                {/*--- titolo --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Titolo"}
                              value={coupon.title || ''}
                    />
                </Grid>
                {/*--- valore --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Valore"}
                              value={(coupon.value || '') + '€' }
                    />
                </Grid>

                {/*--- data emissione --*/}

                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Data di emissione"}
                              value={unixToString(coupon.emissionDate) || ''}
                    />
                </Grid>

            </Grid>
        </BaseModal>
    );
}
import {Grid} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import TextInput from "../../components/forms/TextInput";
import React from "react";

const useStyles = makeStyles((theme) => ({
    marginBottom: {
        marginBottom: theme.spacing(2)
    },
    formControl: {
        width: '100%'
    },
    paddingMdUp: {
        [theme.breakpoints.up('md')]: {
            paddingLeft: theme.spacing(3),
            paddingRight: theme.spacing(3)
        },
    },
}));

/**
 * mostra i campi del vendor
 * @param handleOnChange call back function prende in input (testo, campo)
 * @param vendor
 * @param errors
 * @returns {JSX.Element}
 * @constructor
 */
export default function VendorData({handleOnChange, vendor, errors= {}}){
    const classes = useStyles();
    return(
        <Grid container
              direction="row"
              alignItems="center"
              className={classes.marginBottom}
        >
            {/*--- ragione sociale --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"Ragione Sociale"}
                    type="text"
                    color={'primary'}
                    value={vendor.businessName || ''}
                    onTextChange={(text) => handleOnChange(text, 'businessName')}
                    error={errors.businessName || ''}
                />
            </Grid>

            {/*--- sede legale --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"Sede legale"}
                    type="text"
                    helperText={'Indirizzo completo di città della sede legale'}
                    color={'primary'}
                    value={vendor.address || ''}
                    onTextChange={(text) => handleOnChange(text, 'address')}
                    error={errors.address || ''}
                />
            </Grid>

            {/*--- telefono --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"Telefono"}
                    type="text"
                    color={'primary'}
                    value={vendor.phone || ''}
                    onTextChange={(text) => handleOnChange(text, 'phone')}
                    error={errors.phone || ''}
                />
            </Grid>

            {/*--- p Iva --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"Partita IVA"}
                    type="text"
                    color={'primary'}
                    value={vendor.vatNumber || ''}
                    onTextChange={(text) => handleOnChange(text, 'vatNumber')}
                    error={errors.vatNumber || ''}
                />
            </Grid>

            {/*--- iban --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"IBAN"}
                    type="text"
                    color={'primary'}
                    value={vendor.iban || ''}
                    onTextChange={(text) => handleOnChange(text, 'iban')}
                    error={errors.iban}
                />
            </Grid>

            {/*--- BIC --*/}
            <Grid item xs={12} md={6} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <TextInput
                    label={"BIC"}
                    type="text"
                    color={'primary'}
                    value={vendor.bic || ''}
                    onTextChange={(text) => handleOnChange(text, 'bic')}
                    error={errors.bic}
                />
            </Grid>

        </Grid>
    );
}
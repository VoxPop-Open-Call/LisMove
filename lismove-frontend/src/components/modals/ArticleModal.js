import BaseModal from "./BaseModal";
import makeStyles from '@mui/styles/makeStyles';
import React from "react";
import {Grid} from "@mui/material";
import TextInput from "../forms/TextInput";
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

export default function ArticleModal({open, onClose, article}) {
    const classes = useStyles();
    return (
        <BaseModal open={open} onClose={onClose}>
            <Grid container>
                {/*--- titolo --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Titolo"}
                              value={article.title || ''}
                    />
                </Grid>
                {/*--- descrizione --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Descrizione"}
                              value={article.description || ''}
                    />
                </Grid>
                {/*--- valore --*/}
                <Grid item xs={12} md={6}
                      className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                    <TextItem xs={12} className={classes.textItem}
                              label={"Valore in punti"}
                              value={article.points || ''}
                    />
                    <TextItem xs={12} className={classes.textItem} value={(article.points || 0) / 100 + '€'}
                              label={"Valore in euro"}/>
                </Grid>
                {/*--- data scadenza --*/}
                {
                    article.expirationDate &&
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <TextItem xs={12} className={classes.textItem}
                                  label={"Data di scadenza"}
                                  value={unixToString(article.expirationDate) || ''}
                        />
                    </Grid>
                }
                {/*--- numero elementi --*/}
                {
                    article.numberArticles &&
                    <Grid item xs={12} md={6}
                          className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                        <TextInput
                            label={"Numero di elementi"}
                            value={article.numberArticles || ''}
                        />
                    </Grid>
                }

                <Grid item xs={12}>
                    {/*immagine*/}
                    <img src={article.image || ''} alt="article image" width={'100%'}/>
                </Grid>
            </Grid>
        </BaseModal>
    );
}
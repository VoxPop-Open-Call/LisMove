import {Grid, IconButton, Typography} from "@mui/material";
import {Delete} from "@mui/icons-material";
import FileInput from "./forms/FileInput";
import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import {removeImagesFirebase} from "../services/FirebaseManager";


/**
 * Mostra un slider con delle immagini dove e' inoltre possibile aggiungerne di nuove (verranno automaticamente caricate su firebase)
 * @param images lista di immagini, o immmagine singola(in questo caso settare single image a true)
 * @param title
 * @param prefix
 * @param folder
 * @param onDeleteImage(img,index) callback function quando viene eliminata un immagine, non aggiorna automaticamente la lista delle immagini
 * @param onAddImage(e) callback function quando viene aggiunta un immagine, non aggiorna automaticamente la lista delle immagini
 * @param isSingleImage
 * @param classes
 * @param className
 * @param error
 * @param disabled
 * @returns {JSX.Element}
 * @constructor
 */
export default function NTMImageInput({
                                          images,
                                          title,
                                          prefix,
                                          folder,
                                          onDeleteImage,
                                          onAddImage,
                                          isSingleImage = false,
                                          classes = {},
                                          className,
                                          error = null,
                                          disabled = false
                                      }) {

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
        imageItem: {
            width: '10rem',
            height: '10rem',
            '& .filepond--panel.filepond--panel-root': {
                borderRadius: 0,
                border: '1px solid ' + (error ? theme.palette.error.main : theme.palette.secondary.dark)
            },
            '& .filepond--root, .filepond--root .filepond--drop-label': {
                width: '10rem',
                height: '10rem',
            },
            margin: [0, theme.spacing(2), theme.spacing(2), 0].join('px '),
            backgroundPosition: 'center',
            backgroundRepeat: 'no-repeat',
            backgroundSize: 'cover',
        },
        slider: {
            overflowX: 'auto',
            //considera il padding per la dimensione
            width: 'calc(100vw - 48px)',
            [theme.breakpoints.up('md')]: {
                width: 'calc(100vw - 177px)',
            }
        },
        deleteButton: {
            // color:theme.palette.primary.contrastText,
            marginLeft: '7rem',
            marginTop: 0,
            backgroundColor: theme.palette.primary.light,
            borderRadius: 0,
            '&:hover': {
                opacity: 1,
                backgroundColor: "rgba(232,222,222,0.9)",
            }
        },
        error: {
            color: theme.palette.error.main
        },
        ...classes //sovrascrivi le classi esistenti
    }));

    classes = useStyles();

    if (isSingleImage)
        images = images ? [images] : []//se l'immagine é singola la trasformo in un array

    return (
        <Grid container direction={'row'} className={className}>
            <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <Typography variant={'h6'}>{title}</Typography>
            </Grid>
            <Grid item xs={12} className={[classes.marginBottom, classes.paddingMdUp].join(' ')}>
                <div className={classes.slider}>
                    <Grid container direction={'row'} wrap={'nowrap'} style={{width: 'max-content'}}>
                        {
                            (!disabled && (!isSingleImage || images.length === 0)) &&
                            <div className={classes.imageItem}>
                                <FileInput
                                    folder={folder}
                                    prefix={prefix}
                                    onRequestSave={onAddImage}
                                    allowMultiple={!isSingleImage}
                                    maxFiles={isSingleImage ? 1 : 2}
                                    showLoadedImages={false}
                                />
                            </div>
                        }
                        {
                            images && images.map(
                                (img, index) => (
                                    <Grid item
                                          key={index}
                                          style={{
                                              backgroundImage: 'url(' + img + ')',
                                          }}
                                          className={classes.imageItem}
                                    >
                                        {
                                            !disabled &&
                                            <IconButton
                                                className={classes.deleteButton}
                                                onClick={() => {
                                                    onDeleteImage(img, index);
                                                }}
                                                size="large">
                                                <Delete/>
                                            </IconButton>
                                        }
                                    </Grid>
                                )
                            )
                        }
                    </Grid>
                </div>
                {error && <Typography variant={'caption'} className={classes.error}>{error}</Typography>}
            </Grid>
        </Grid>
    );
}
import React from "react";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import ButtonBase from "@mui/material/ButtonBase";

const useStyles = makeStyles((theme) => ({
    language: {
        textAlign: "center",
        width: "3rem",
        color: theme.palette.primary.main,
    },
    languageFilled: {
        textAlign: "center",
        width: "3rem",
        color: theme.palette.secondary.main,
    },
    divisor: {
        borderColor: theme.palette.primary.main,
        borderLeft: "0.2rem solid"
    },
    divisorFilled: {
        borderColor: theme.palette.secondary.main,
        borderLeft: "0.2rem solid"
    }
}));

export default function LanguageInput({selectedLanguage,setLanguage, filledLanguages}){
    const classes = useStyles();

    return (
        <Grid  item xs={12}>
            <span className={filledLanguages.find((e) => e === 'it') ? classes.divisorFilled : classes.divisor}/>
            <ButtonBase disableTouchRipple className={filledLanguages.find((e) => e === 'it') ? classes.languageFilled : classes.language} style={(selectedLanguage === 'it') ? {textDecoration: "underline"} : {}}
                        onClick={() => setLanguage('it')}>
                IT
            </ButtonBase>
            <span className={filledLanguages.find((e) => e === 'it' || e === 'en') ? classes.divisorFilled : classes.divisor}/>
            <ButtonBase disableTouchRipple className={filledLanguages.find((e) => e === 'en') ? classes.languageFilled : classes.language} style={(selectedLanguage === 'en') ? {textDecoration: "underline"} : {}}
                        onClick={() => setLanguage("en")}>
                EN
            </ButtonBase>
            <span className={filledLanguages.find((e) => e === 'en' || e === 'fr') ? classes.divisorFilled : classes.divisor}/>
            <ButtonBase disableTouchRipple className={filledLanguages.find((e) => e === 'fr') ? classes.languageFilled : classes.language} style={selectedLanguage === "fr" ? {textDecoration: "underline"} : {}}
                        onClick={() => setLanguage("fr")}>
                FR
            </ButtonBase>
            <span className={filledLanguages.find((e) => e === 'fr' || e === 'de') ? classes.divisorFilled : classes.divisor}/>
            <ButtonBase disableTouchRipple className={filledLanguages.find((e) => e === 'de') ? classes.languageFilled : classes.language} style={selectedLanguage === "de" ? {textDecoration: "underline"} : {}}
                        onClick={() => setLanguage("de")}>
                DE
            </ButtonBase>
            <span className={filledLanguages.find((e) => e === 'de') ? classes.divisorFilled : classes.divisor}/>
        </Grid>
    );
}
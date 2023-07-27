import {useParams} from "react-router-dom";
import makeStyles from '@mui/styles/makeStyles';
import Grid from "@mui/material/Grid";
import React,{useEffect,useState} from "react";
import { AccordionDetails, Accordion, AccordionSummary } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Typography from "@mui/material/Typography";
import UrbanPointsSettings from "./UrbanPointsSettings";
import StartIconButton from "../../../components/buttons/StartIconButton";
import SaveIcon from "@mui/icons-material/Save";
import ReplayIcon from '@mui/icons-material/Replay';
import {
    CUSTOMFIELDS,
    deleteElem,
    getErrorMessage,
    ORGANIZATIONS,
    post,put,
    SETTINGS,
} from "../../../services/Client";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import {useGetOrganizationCustomField,useGetOrganizationSettings} from "../../../services/ContentManager";
import Refunds from "./Refunds";
import UrbanPathRefunds from "./UrbanPathRefunds";
import HomeWorkRefunds from "./HomeWorkRefunds";
import CustomFieldManager from "./CustomFieldManager";
import GlobalRanksAchievementsManager from "./GlobalRanksAchievementsManager";

const useStyles = makeStyles(theme => ({
    container: {
        padding: "2rem",
        backgroundColor: theme.palette.primary.light,
        width: "auto"
    },
    header: {
        fontWeight: "bold",
    },
    expansionPanel: {
        margin: theme.spacing(1),
    },
    expanded: {
        border: "3px solid" + theme.palette.primary.dark,
    },
}));

const replacer = (key, value) => {          //funzione usata per il confronto degli oggetti con JSON.stringify
    if (typeof value === 'boolean') return String(value);
    return value;
}

function Pill ({pill, index}) {

    let classes = useStyles();
    let [expanded,setExpanded] = useState(false);

    return (
        <Grid item lg={6} xs={12} key={index}>
            <Accordion
                TransitionProps={{unmountOnExit : true}}
                classes={{root : classes.expansionPanel,expanded : classes.expanded}}
                square
                onChange={(e,expanded) => setExpanded(expanded)}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon color={expanded ? "secondary" : "primary"}/>}
                    aria-controls="panel1a-content"
                    id={pill.id}
                >
                    <Grid alignItems={"center"} container direction="row">
                        <Typography color={"textPrimary"} className={classes.header}>{pill.title}</Typography>
                    </Grid>
                </AccordionSummary>
                <AccordionDetails>
                    {
                        pill.component
                    }
                </AccordionDetails>
            </Accordion>
        </Grid>
    );
}

export default function Settings(){

    let classes = useStyles();
    let {id} = useParams();
    let {settings = []} = useGetOrganizationSettings(id);
    let [values, setValues] = useState({});
    let [originalSettings, setOriginalSettings] = useState({});
    let [errors, setErrors] = useState({});
    let {customField = []} = useGetOrganizationCustomField(id);
    let [editedCustomField, setEditedCustomField] = useState([]);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    useEffect(() => {
        if(settings){
            let newValues = {}
            settings.map(setting => {
                newValues[setting.organizationSetting] = setting.value
            })
            if(newValues["homeWorkPointsTolerance"]) newValues["homeWorkPointsTolerance"] *= 1000
            if(newValues["homeWorkPathTolerancePerc"]) newValues["homeWorkPathTolerancePerc"] *= 100
            setValues(newValues)
            setOriginalSettings(newValues)
        }
    }, [settings])

    useEffect(() => {
        if(customField) setEditedCustomField(JSON.parse(JSON.stringify(customField)))
    }, [customField])

    const pills = [
        {
            title: "Punti Iniziativa",
            component: <UrbanPointsSettings values={values} setValues={setValues} onError={(value) => setErrors({...errors, urbanPointsSettingsErrors : value})}/>
        },
        {
            title: "Rimborso",
            component: <Refunds values={values} setValues={setValues} onError={(value) => setErrors({...errors, refundsErrors : value})}/>
        },
        {
            title: "Rimborso Casa -> Scuola/Lavoro",
            component: <HomeWorkRefunds values={values} setValues={setValues} onError={(value) => setErrors({...errors, homeWorkRefundsErrors : value})}/>
        },
        {
            title: "Rimborso tragitto urbano",
            component: <UrbanPathRefunds values={values} setValues={setValues} onError={(value) => setErrors({...errors, urbanPathRefundsErrors : value})}/>
        },
        {
            title: "Valori jolly categorie",
            component: <CustomFieldManager settings={values} setSettings={setValues} customField={editedCustomField} setCustomField={setEditedCustomField}/>
        },
        {
            title: "Classifiche e coppe globali",
            component: <GlobalRanksAchievementsManager values={values} setValues={setValues}/>
        }
    ];

    const checkErrors = () => {
        return !!(errors.urbanPointsSettingsErrors || errors.refundsErrors || errors.homeWorkRefundsErrors || errors.urbanPathRefundsErrors);
    }

    const save = () => {

        let showedError = false;
        let showedSaved = false;

        enqueueSnackbar("Saving...",{variant : "info"});

        if(JSON.stringify(originalSettings, replacer) !== JSON.stringify(values, replacer)) {
            let settings = [];

            Object.entries(values).forEach(setting => {
                if(setting[0] === "homeWorkPointsTolerance") settings.push({organizationSetting : setting[0],value : setting[1]/1000});
                else if(setting[0] === "homeWorkPathTolerancePerc") settings.push({organizationSetting : setting[0],value : setting[1]/100});
                else settings.push({organizationSetting : setting[0],value : setting[1]});
            })

            post(ORGANIZATIONS + "/" + id + "/" + SETTINGS,{body : settings})
                .then(() => {
                    enqueueSnackbar("Saved",{variant : "success"})
                    showedSaved = true
                })
                .catch(e => {
                    enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                    showedError = true
                })
                .finally(() => queryClient.invalidateQueries([ORGANIZATIONS,{id : id},SETTINGS]));
        }

        if(JSON.stringify(customField) !== JSON.stringify(editedCustomField)) {

            for(let i = 0; i < 3; i++){
                if(!customField[i] && !!editedCustomField[i]) {
                    post(ORGANIZATIONS+"/"+id+"/"+CUSTOMFIELDS, {body: editedCustomField[i]})
                        .then(() => {
                            !showedSaved && enqueueSnackbar("Saved",{variant : "success"})
                            showedSaved = true
                        })
                        .catch(e => {
                            !showedError && enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                            showedError = true
                        })
                        .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, CUSTOMFIELDS]));
                }
                else if(!!customField[i] && !editedCustomField[i]) {
                    deleteElem(ORGANIZATIONS+"/"+id+"/"+CUSTOMFIELDS+"/"+customField[i].id)
                        .then(() => {
                            !showedSaved && enqueueSnackbar("Saved",{variant : "success"})
                            showedSaved = true
                        })
                        .catch(e => {
                            !showedError && enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                            showedError = true
                        })
                        .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, CUSTOMFIELDS]));
                }
                else if(JSON.stringify(customField[i]) !== JSON.stringify(editedCustomField[i])) {
                    put(ORGANIZATIONS+"/"+id+"/"+CUSTOMFIELDS+"/"+customField[i].id, {body: editedCustomField[i]})
                        .then(() => {
                            !showedSaved && enqueueSnackbar("Saved",{variant : "success"})
                            showedSaved = true
                        })
                        .catch(e => {
                            !showedError && enqueueSnackbar(getErrorMessage(e),{variant : "error"})
                            showedError = true
                        })
                        .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, CUSTOMFIELDS]));
                }
            }

        }

    }

    return <Grid container justifyContent="space-between">

        <Grid container justifyContent={"space-between"}>
            <Grid>
                <StartIconButton
                    onClick={() => {
                        setValues({...originalSettings})
                        setEditedCustomField(JSON.parse(JSON.stringify(customField)))
                    }}
                    size="large" title="Ripristina" startIcon={<ReplayIcon/>}
                    disabled={JSON.stringify(originalSettings, replacer) === JSON.stringify(values, replacer) && JSON.stringify(customField) === JSON.stringify(editedCustomField)}/>
            </Grid>
            <Grid>
                <StartIconButton
                    onClick={save} size="large" title="Salva"
                    startIcon={<SaveIcon/>}
                    disabled={(JSON.stringify(originalSettings, replacer) === JSON.stringify(values, replacer) && JSON.stringify(customField) === JSON.stringify(editedCustomField)) || checkErrors()}/>
            </Grid>

        </Grid>

        <Grid container xs={12} className={classes.container}>

            {pills.map((pill, index) => <Pill pill={pill} index={index}/>)}

        </Grid>
    </Grid>
}
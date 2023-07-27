import Grid from "@mui/material/Grid";
import makeStyles from '@mui/styles/makeStyles';
import React,{useState} from "react";
import {useSnackbar} from "notistack";
import IconButton from "@mui/material/IconButton";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import EditIcon from "@mui/icons-material/Edit";
import InputBase from "@mui/material/InputBase";
import FileInput from "../../components/forms/FileInput";
import {getErrorMessage,ORGANIZATIONS,put} from "../../services/Client";
import {useQueryClient} from "react-query";
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import StartIconButton from "../../components/buttons/StartIconButton";
import {useGetOrganization} from "../../services/ContentManager";
import {useParams} from "react-router-dom";

const useStyles = makeStyles(theme => ({
    logo: {
        maxWidth: "80vw",
        maxHeight: "25vh"
    },
    title: {
        fontWeight: "bold",
        color: theme.palette.primary.main,
        fontSize: 35
    },
    text: {
        paddingBottom: theme.spacing(1),
        fontWeight: "bold",
        color: theme.palette.text.primary
    },
    detail: {
        padding: theme.spacing(2),
    },
    textFieldTitle: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        border: `${theme.palette.secondary.main} 1px solid`,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        width: "50vw",
        flexGrow: 1
    },
    textFieldRegulation: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        border: `${theme.palette.secondary.main} 1px solid`,
        width: "70vw",
        height: "6rem",
        flexGrow: 1
    },
}));

function TitleDetail({value}){
    let classes = useStyles();

    return value ? <Grid item className={classes.detail}>
        <div className={classes.text}>Titolo</div>
        <div className={classes.title}>{value}</div>
    </Grid> : <div/>
}

function ImageDetail({title, src}){
    let classes = useStyles();

    return src ? <Grid item className={classes.detail}>
        <div className={classes.text}>{title}</div>
        <img className={classes.logo} src={src}/>
    </Grid> : <div/>
}

function EditableFileDetail ({title, folder, prefix, onRequestSave, fileType}){
    let classes = useStyles();

    return <Grid item className={classes.detail}>
        <div className={classes.text}>{title}</div>
        <div style={{width: "13rem"}}>
            <FileInput folder={folder} prefix={prefix} onRequestSave={onRequestSave} acceptedFileTypes={fileType}/>
        </div>
    </Grid>
}

function RegulationDetail({value}){
    let classes = useStyles();

    return value ? <Grid item className={classes.detail}>
        <div className={classes.text}>Regolamento</div>
        <div>{value}</div>
    </Grid> : <div/>
}

export default function Details(){

    let [isEditable, setIsEditable] = useState(false);
    let [editOrganization, setEditOrganization] = useState({});
    let {id} = useParams();
    let {organization} = useGetOrganization(id);
    let classes = useStyles();
    let queryClient = useQueryClient();
    const {enqueueSnackbar} = useSnackbar();

    const edit = () => {
        setEditOrganization({...organization});
        setIsEditable(true);
    }

    const onChange = (prop) => (event) => {
        setEditOrganization({ ...editOrganization, [prop]: event.target.value });
    }

    const saveEdit = () => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(ORGANIZATIONS, {body: editOrganization, elem: organization.id})
            .then(() => {
                enqueueSnackbar("Saved",{variant : "success"})
                setIsEditable(false)
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(ORGANIZATIONS, {id: organization.id}));
    }

    return (
        <div>
                <Grid item xs={12} style={{display: "flex", justifyContent: "flex-end"}} >
                    {isEditable ?
                        <Grid>
                            <IconButton onClick={saveEdit} size="large">
                                <CheckIcon/>
                            </IconButton>
                            <IconButton onClick={() => setIsEditable(false)} size="large">
                                <CloseIcon/>
                            </IconButton>
                        </Grid>
                        :
                        <IconButton onClick={edit} size="large">
                            <EditIcon/>
                        </IconButton>
                    }
                </Grid>
            {isEditable ?
                <Grid item className={classes.detail}>
                    <div className={classes.text}>Titolo</div>
                    <InputBase
                        className={classes.textFieldTitle}
                        value={editOrganization["title"]}
                        onChange={onChange("title")}
                        placeholder={"Titolo"}/>
                </Grid>
            :
            <TitleDetail value={organization.title}/>}

            <Grid container direction={isEditable ? "row" : "column"}>

                {isEditable ?
                    <EditableFileDetail title="Logo Iniziativa" folder="organizations/logos" prefix={organization.id}
                                         onRequestSave={(e) => setEditOrganization({ ...editOrganization, "initiativeLogo": e })}/>
                    :
                    <ImageDetail title="Logo Iniziativa" src={organization.initiativeLogo}/>}

                {isEditable ?
                    <EditableFileDetail title="Logo Comune Notifica" folder="organizations/logos" prefix={organization.id}
                                         onRequestSave={(e) => setEditOrganization({ ...editOrganization, "notificationLogo": e })}/>
                    :
                    <ImageDetail title="Logo Comune Notifica" src={organization.notificationLogo}/>}

                {isEditable ?
                    <EditableFileDetail title="Pagina dei termini e condizioni" folder="organizations/termsConditions" prefix={organization.id}
                                        onRequestSave={(e) => setEditOrganization({ ...editOrganization, "termsConditions": e })}
                                        fileType={['application/doc','application/pdf','.docx']}/>
                    :
                    organization.termsConditions ? <Grid item className={classes.detail}>
                        <div className={classes.text}>Pagina dei termini e condizioni</div>
                        <StartIconButton title="Apri Pdf" startIcon={<PictureAsPdfIcon/>} href={organization.termsConditions} target = "_blank"/>
                    </Grid> : <div/>
                }

            </Grid>

            {isEditable ?
                <Grid item className={classes.detail}>
                    <div className={classes.text}>Regolamento</div>
                    <InputBase
                        multiline
                        rows={5}
                        className={classes.textFieldRegulation}
                        value={editOrganization["regulation"]}
                        onChange={onChange("regulation")}
                        placeholder={"Regolamento"}
                    />
                </Grid>
                :
                <RegulationDetail value={organization.regulation}/>}

        </div>
    );
}

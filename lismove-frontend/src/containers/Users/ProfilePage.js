import React,{useContext,useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import Paper from "@mui/material/Paper";
import Grid from "@mui/material/Grid";
import IconButton from "@mui/material/IconButton";
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import userImage from "../../images/top_bar-profilo-over.svg";
import InputBase from "@mui/material/InputBase";
import Box from "@mui/material/Box";
import {useSnackbar} from "notistack";
import {USERS} from "../../services/Client";
import {getErrorMessage, put} from "../../services/Client";
import {useGetCities,useGetOrganizations,useGetProfileUser} from "../../services/ContentManager";
import LockIcon from '@mui/icons-material/Lock';
import Button from "@mui/material/Button";
import {useQueryClient} from "react-query";
import {Avatar} from "@mui/material";
import ChangeCredientalsModal from "../../components/modals/ChangeCredentialsModal";
import FileInput from "../../components/forms/FileInput";
import dayjs from "dayjs";
import {useHistory,useParams} from "react-router-dom";
import {UserContext} from "../App";
import FilterAndSearchBar from "../../components/layout/FilterAndSearchBar";
import {UserAchievements, UserEnrollments, UserSensors, UserSmartphones} from "./ComponentsProfileContainer";


const useStyles = makeStyles(theme => ({
    page: {
        padding: theme.spacing(2)
    },
    container: {
        marginBottom: theme.spacing(2)
    },
    box: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        borderBottom: `${theme.palette.secondary.main} 1px solid`,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        flexGrow: 1,
    },
    textField: {
        padding: theme.spacing(1),
        margin: theme.spacing(1),
        border: `${theme.palette.secondary.main} 1px solid`,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        flexGrow: 1
    },
    userImage: {
        height: "8rem",
        width: "8rem",
        color: theme.palette.primary.main
    },
    editUserImage: {
        color: "#fff",
        backgroundColor: theme.palette.primary.dark,
    },
    filterBar: {
        padding: theme.spacing(4),
        paddingBottom: 0
    },
}));

const form = [
    {
        name: "firstName",
        label: "Nome"
    },
    {
        name: "lastName",
        label: "Cognome"
    },
    {
        name: "username",
        label: "Username"
    },
    {
        name: "email",
        label: "E-Mail",
        notEditable: true
    },
    {
        name: "phoneNumber",
        label: "Numero di telefono"
    },
    {
        name: "birthDate",
        label: "Compleanno",
        type: "date"
    },
    {
        name: "gender",
        label: "Sesso"
    },
    {
        name: "iban",
        label: "IBAN"
    }
]

export default function ProfilePage (){

    let classes = useStyles();
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    const {uid, filter} = useParams();
    let {user = {}} = useGetProfileUser(uid);
    let {cities = []} = useGetCities();
    let {organizations = []} = useGetOrganizations();
    const loggedUser = useContext(UserContext);
    const isUserLogged = uid === loggedUser.uid;
    let [isEditable, setIsEditable] = useState(false);
    let [isEditingCredentials, setIsEditingCredentials] = useState(false);
    let [editUser, setEditUser] = useState({});
    let history = useHistory();

    const workAddresses = user && user["workAddresses"] && user["workAddresses"].slice().sort((a , b) => b.latitude - a.latitude)

    const filters = [
        {
            id: 0,
            filter: "sensors",
            name: "Sensori",
            component: <UserSensors uid={uid}/>
        },
        {
            id: 1,
            filter: "enrollments",
            name: "Iniziative",
            component: <UserEnrollments uid={uid}/>
        },
        {
            id: 2,
            filter: "achievements",
            name: "Coppe",
            component: <UserAchievements uid={uid}/>
        },
        {
            id: 3,
            filter: "smartphones",
            name: "Telefoni",
            component: <UserSmartphones uid={uid}/>
        },
    ]

    const onFilter = (filterId) => {
        history.push(filters.find(f => f.id === filterId).filter)
    }

    const onChange = (prop) => (event) => {
        setEditUser({ ...editUser, [prop]: event.target.value });
    }

    const saveEdit = () => {
        enqueueSnackbar("Saving...", {variant: "info"});
        put(USERS, {body: {...editUser, birthDate : Date.parse(editUser.birthDate)}, elem: editUser.uid})
            .then(() => {
                enqueueSnackbar("Saved", {variant: "success"});
                setIsEditable(false);
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([USERS, {user: user.uid}]));
    }

    const edit = () => {
        setEditUser({...user});
        if(user.birthDate) setEditUser({...user, "birthDate": dayjs(new Date(user.birthDate)).format("YYYY-MM-DD")});
        setIsEditable(true);
    }

    return (
        <Paper className={classes.page}>
            <Grid container className={classes.container}>
                <Grid item xs={6} style={{display: "flex", justifyContent: "flex-start"}} >
                    {isUserLogged &&
                         <Button
                            color="primary"
                            startIcon={<LockIcon/>}
                            onClick={() => setIsEditingCredentials(true)}
                        >
                            Cambia Password
                        </Button>
                    }
                </Grid>
                <Grid item xs={6} style={{display: "flex", justifyContent: "flex-end"}} >
                    {isUserLogged && (isEditable ?
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
                        </IconButton>)
                    }
                </Grid>


                <Grid item xs={12} style={{display: "flex", justifyContent: "center"}}>
                    {!isEditable
                        ?
                    <Avatar src={user.avatarUrl || userImage} alt="userImage" className={classes.userImage}/>
                        :
                        <div style={{width: "13rem"}}>
                            <FileInput folder="users/avatars" prefix={user.uid} onRequestSave={(e) => setEditUser({ ...editUser, "avatarUrl": e })}/>
                        </div>}
                </Grid>

            </Grid>

            <Grid container alignItems={"flex-end"} className={classes.container}>
                {
                    form.map(f => (!isEditable || !f.notEditable) &&
                        <Grid item xs={12} md={6} style={{display: "flex"}} key={f.name}>
                            {isEditable ?

                                <InputBase
                                    className={classes.textField}
                                    value={editUser[f.name]}
                                    onChange={onChange(f.name)}
                                    placeholder={f.label}
                                    type={f.type}/>

                                    :

                                <Box xs={6} className={classes.box}>
                                    <div style={{fontWeight: "normal"}}>{f.label}</div>
                                    <div style={{marginLeft: "1rem"}}>
                                        {f.type === "date" ?
                                            user[f.name] ? dayjs(new Date(user[f.name])).format("DD/MM/YYYY") : "-"
                                            : user[f.name] || "-"}
                                    </div>
                                </Box>
                            }
                        </Grid>)
                }
                {!isEditable &&
                    <Grid item xs={12} md={6} style={{display : "flex"}}>
                        <Box xs={6} className={classes.box}>
                            <div style={{fontWeight : "normal"}}>Indirizzo Casa</div>
                            <div style={{marginLeft : "1rem"}}>
                                {user["homeAddress"] ?
                                    user["homeAddress"] + " n." +user[ "homeNumber" ] + ", " + (cities.length !== 0 && cities.find(c => c.istatId === user[ "homeCity" ]).city)
                                    : "-"
                                }
                            </div>
                        </Box>
                    </Grid>
                }
                {!isEditable && workAddresses &&
                    workAddresses.map(wa => <Grid item xs={12} md={6} style={{display: "flex"}}>
                        <Box xs={6} className={classes.box}>
                            <div style={{fontWeight: "normal", paddingBottom: "0.1rem"}}>Indirizzo Lavoro {organizations.length !== 0 && organizations.find(o => o.id === wa.organization) && organizations.find(o => o.id === wa.organization).title}</div>
                            <div style={{marginLeft: "1rem"}}>
                                {wa.name}, {wa.address} n.{wa.number}, {wa.cityName}
                            </div>
                        </Box>
                    </Grid>)
                }
            </Grid>

            <Grid container className={classes.filterBar}>
                <FilterAndSearchBar
                    filters={filters}
                    onFilter={onFilter}
                    selected={filters.find(f => f.filter === filter).id}
                />
            </Grid>
            <div className={classes.page}>
                {filters.find(f => f.filter === filter).component}
            </div>

            <ChangeCredientalsModal open={isEditingCredentials} onClose={() => setIsEditingCredentials(false)}/>
        </Paper>
    );
}

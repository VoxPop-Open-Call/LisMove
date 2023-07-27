import {useGetOrganizations} from "../services/ContentManager";
import makeStyles from '@mui/styles/makeStyles';
import {useSnackbar} from "notistack";
import Grid from "@mui/material/Grid";
import {CircularProgress,TextField} from "@mui/material";
import {useState} from "react";
import Button from "@mui/material/Button";
import Avatar from "@mui/material/Avatar";
import {useHistory} from "react-router-dom";
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import OrganizationModal from "../components/modals/OrganizationModal";
import {getErrorMessage,ORGANIZATIONS,post} from "../services/Client";
import {useQueryClient} from "react-query";

const useStyles = makeStyles((theme) => ({
    pill: {
        backgroundColor: theme.palette.primary.light,
        color: theme.palette.primary.dark,
        fontWeight: "bold",
        '&:hover': {
            backgroundColor: theme.palette.primary.dark,
            color: "white",
            cursor: "pointer"
        }
    },
    searchbar: {
        width: '100%',
        marginRight: theme.spacing(2)
    },
    icon: {
        backgroundColor: theme.palette.primary.dark,
    },
    iconHover: {
        backgroundColor: theme.palette.primary.light,
        color: theme.palette.primary.dark,
    }
}));

function OrganizationPill({organization}) {
    let classes = useStyles();
    let history = useHistory();
    let [hover, setHover] = useState(false);

    return (
        <Grid container
                     spacing={2}
                     justifyContent={"space-between"}
                     alignItems={"center"}
                     key={organization.id}
                     onMouseOver={() => setHover(true)}
                     onMouseOut={() => setHover(false)}
                     className={classes.pill}
                     onClick={() =>  history.push("/dashboard/" + organization.id + "/details")}
        >
            <Grid item>
                <Avatar src={organization.notificationLogo} className={hover ? classes.iconHover : classes.icon}><AccountBalanceIcon/></Avatar>
            </Grid>
            <Grid item>{organization.title}</Grid>
            <Grid/>
        </Grid>
    );
}

export function OrganizationsList(){

    let [search, setSearch] = useState();
    let [modal, setModal] = useState(false);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let history = useHistory();
    let classes = useStyles();

    let {status, organizations, error} = useGetOrganizations();
    if (error) enqueueSnackbar(error, {variant: "error"});

    function saveOrganization(organization){
        enqueueSnackbar("Saving...", {variant: "info"});
        post(ORGANIZATIONS, {body: organization})
            .then(org => {
                enqueueSnackbar("Saved", {variant: "success"});
                history.push("/dashboard/" + org.data.id);
            })
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries(ORGANIZATIONS));
    }

    if(status === "loading") return <Grid container><CircularProgress/></Grid>

    if(organizations && organizations.length === 1){
        history.push("/dashboard/" + organizations[0].id + "/details")
        return <div><CircularProgress/></div>
    }

    let data = organizations ? organizations.slice() : [];
    if (search) {
        let searches = search.toLowerCase().split(' ');
        searches.forEach(s => data = data.filter(elem => elem.title.toLowerCase().includes(s) || elem.id.toString().includes(s)));
    }
    data.sort((a, b) => b.id - a.id);

    return <Grid container>
        <Grid container alignItems={"center"} spacing={2}>
            <Grid item xs>
                <TextField margin={"dense"} className={classes.searchbar} label="Search" variant="outlined"
                           value={search} onChange={({target}) => setSearch(target.value)}/>
            </Grid>
            <Grid item>
                <Button variant="contained" color="primary" disableElevation onClick={() => setModal(true)}>
                    +
                </Button>
            </Grid>

        </Grid>
        <Grid item xs={12}>
            <Grid container spacing={3} style={{marginTop: 0}}>
                {data.map(o => <Grid item sm={6} key={o.id}><OrganizationPill organization={o}/></Grid>)}
            </Grid>
        </Grid>
        <OrganizationModal
            open={modal}
            onClose={() => setModal(false)}
            onSubmit={saveOrganization}
        />
    </Grid>
}
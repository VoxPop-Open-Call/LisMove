import {useHistory,useParams} from "react-router-dom";
import makeStyles from '@mui/styles/makeStyles';
import {useGetOrganization} from "../services/ContentManager";
import {CircularProgress,Paper} from "@mui/material";
import Grid from "@mui/material/Grid";
import AccountBalanceIcon from "@mui/icons-material/AccountBalance";
import FilterAndSearchBar from "../components/layout/FilterAndSearchBar";
import Details from "./Dashboard/Details";
import AreaManager from "./Dashboard/AreaManager";
import UserManager from "./Dashboard/UserManager";
import Codes from "./Dashboard/Codes";
import Seats from "./Dashboard/Seats";
import OrganizationRanksManager from "./Dashboard/OrganizationRanksManager";
import OrganizationAchievements from "./Dashboard/OrganizationAchievements";
import Settings from "./Dashboard/Settings/Settings";
import Messages from "./Dashboard/Messages/Messages";
import HeatmapContainer from "./Dashboard/HeatmapContainer";

const useStyles = makeStyles(theme => ({
    page: {
        padding: theme.spacing(2)
    },
    filterBar: {
        padding: theme.spacing(4),
        paddingBottom: 0
    },
    title: {
        paddingLeft: theme.spacing(2),
        color: theme.palette.primary.main,
        fontWeight: "bold",
        fontSize: 40
    },
    notificationLogo: {
        width: "5vw",
        color: theme.palette.primary.main,
        fontSize: 60,
    },
    initiativeLogo: {
        maxWidth: "80vw",
        maxHeight: "17vh"
    },
}));

const organizationType = {
    PA: 0,
    COMPANY: 1
}

function getFilters(organization){
    let filters = [];
    let id = 0;

    filters.push({
        id,
        filter: "details",
        name: "Dettagli",
        component: <Details/>
    });
    id++;
    filters.push({
        id,
        filter: "heatmap",
        name: "Heatmap",
        component: <HeatmapContainer/>
    });
    id++;
    filters.push({
        id,
        filter: "area",
        name: "Area iniziativa",
        component: <AreaManager/>
    });
    id++;
    if(organization.type === organizationType.COMPANY){
        filters.push({
            id,
            filter: "seats",
            name: "Sedi",
            component: <Seats/>
        });
        id++;
    }
    filters.push({
        id,
        filter: "codes",
        name: "Codici iniziativa",
        component: <Codes/>
    });
    id++;
    filters.push({
        id,
        filter: "achievements",
        name: "Coppe",
        component: <OrganizationAchievements/>
    });
    id++;
    filters.push({
        id,
        filter: "ranks",
        name: "Classifiche",
        component: <OrganizationRanksManager/>
    });
    id++;
    filters.push({
        id,
        filter: "managers",
        name: "Gestori",
        component: <UserManager/>
    });
    if(process.env.REACT_APP_ENVIRONMENT!=='production') {
        id++;
        filters.push({
            id,
            filter: "messages",
            name: "Messaggi",
            component: <Messages/>
        });
    }
    id++;
    filters.push({
        id,
        filter: "settings",
        name: "Impostazioni",
        component: <Settings/>
    });

    return filters;
}

export default function Dashboard(){

    let {id, filter} = useParams();
    let {status, organization} = useGetOrganization(id);
    let filters = getFilters(organization);
    let classes = useStyles();
    let history = useHistory();

    const onFilter = (filterId) => {
        history.push(filters.find(f => f.id === filterId).filter)
    }

    if(status === "loading" ) return <Grid container><CircularProgress/></Grid>

    return (
        <Paper className={classes.page}>
            <Grid container>
                {organization.initiativeLogo ?
                    <Grid item xs={12} align={"center"}>
                        <img className={classes.initiativeLogo} src={organization.initiativeLogo}/>
                    </Grid>
                    :
                    <Grid container direction="row" justifyContent="center" alignItems="center">
                        {organization.notificationLogo ? <img className={classes.notificationLogo} src={organization.notificationLogo}/> : <AccountBalanceIcon className={classes.notificationLogo}/>}
                        <div className={classes.title}>{organization.title}</div>
                    </Grid>
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
        </Paper>
    );

}

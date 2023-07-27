import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import MapIcon from '@mui/icons-material/Map';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import EuroIcon from '@mui/icons-material/Euro';
import BugReportIcon from '@mui/icons-material/BugReport';
import BarChartIcon from '@mui/icons-material/BarChart';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import drinkingFountain from '../images/pin_drinking_fountain_no_background.png';
import Icon from "@mui/material/Icon";
import { NoteOutlined } from '@mui/icons-material';
import VendorManagement from "../containers/VendorManagement";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';

const dashboard = {
	name : "Dashboard",
	url : "/dashboard",
	icon: <DashboardIcon/>
};

const users = {
	name: "Users",
	url: "/users",
	icon: <PeopleIcon/>
}

const sessions = {
	name: "Sessions",
	url: "/sessions",
	icon: <DirectionsBikeIcon/>
}

const nationalRanks = {
	name: "Classifica Nazionale",
	url: "/nationalRank",
	icon: <BarChartIcon/>
}

const nationalAchievements = {
	name: "Coppe Nazionali",
	url: "/nationalAchievements",
	icon: <EmojiEventsIcon/>
}

const drinkingFountains = {
	name: "Fontane",
	url: "/drinkingFountains",
	icon: <Icon style={{textAlign: 'center'}}><img style={{display: 'flex', height: 'inherit'}} src={drinkingFountain} alt={"drinkingFountain"}/></Icon>
}


const map = {
	name: "Map",
	url: "/map",
	icon: <MapIcon/>
}

const revolut = {
	name: "Revolut",
	url: "/revolut",
	icon: <EuroIcon/>
}

const debug = {
	name: "Debug",
	url: "/debug",
	icon: <BugReportIcon/>
}

const coupons = {
	name: "Coupon",
	url: "/coupons",
	icon: <NoteOutlined/>
}

const vendors = {
	name: "Vendors",
	url: "/vendors",
	icon: <ShoppingCartIcon/>,
}

const customAwards = {
	name: "Premi personalizzati",
	url: "/customAwards",
	icon: <AssignmentIndIcon/>,
}

export default {
	dashboard,
	users,
	map,
	sessions,
	nationalRanks,
	nationalAchievements,
	drinkingFountains,
	revolut,
	coupons,
	debug,
	vendors,
	customAwards
};


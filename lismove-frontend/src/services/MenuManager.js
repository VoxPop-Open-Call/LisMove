import menuItems from "../constants/_nav";
import {resources} from "./ability";

let setMenu = (ability) => {
	let items = [menuItems.dashboard];

	items.push(menuItems.users);
	items.push(menuItems.sessions);
	if(ability.can('read', resources.NATIONALRANKS)) items.push(menuItems.nationalRanks)
	if(ability.can('read', resources.NATIONALACHIEVEMENTS)) items.push(menuItems.nationalAchievements)
	if(ability.can('read', resources.NATIONALACHIEVEMENTS)) items.push(menuItems.customAwards)
	if(ability.can('read', resources.DRINKINGFOUNTAINS)) items.push(menuItems.drinkingFountains)
	if(ability.can('read', resources.DEBUG)) items.push(menuItems.debug)
	if(ability.can('read', resources.REVOLUT)) items.push(menuItems.revolut)
	if(ability.can('read', resources.VENDOR)) items.push(menuItems.vendors)
	if(ability.can('read', resources.COUPONS)) items.push(menuItems.coupons)
	return items;
};

let setMenuVendor = (ability) => {
	let items = [menuItems.dashboard];

	items.push(menuItems.users);
	items.push(menuItems.sessions);
	if(ability.can('read', resources.NATIONALRANKS)) items.push(menuItems.nationalRanks)
	if(ability.can('read', resources.NATIONALACHIEVEMENTS)) items.push(menuItems.nationalAchievements)
	if(ability.can('read', resources.DEBUG)) items.push(menuItems.debug)
	if(ability.can('read', resources.REVOLUT)) items.push(menuItems.revolut)
	return items;
};

export {setMenu,setMenuVendor};

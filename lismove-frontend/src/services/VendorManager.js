import {SHOP_ROUTE_ADMIN} from "../constants/vendors";

/**
 * rimpiazza il :uid dal url e imposta l'uid dato in iput
 * @param route
 * @param uid
 * @returns {*}
 */
export function setRouteUid(route, uid){
    return route.replace(":uid", uid);
}
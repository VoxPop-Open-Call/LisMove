import {useQuery} from "react-query";
import {
    get,
    SESSIONS,
    USERS,
    CITIES,
    ORGANIZATIONS,
    CODES,
    SEATS,
    RANKS,
    RANKINGS,
    CUSTOMFIELDS,
    ACHIEVEMENTS,
    AWARDS, SENSORS, ENROLLMENTS, SETTINGS, SMARTPHONES, VENDORS, SHOPS, MESSAGES, CATEGORIES, COORDINATES, ARTICLES, PARTIALS,COUPONS
} from "./Client";

export function useGetUsers() {
    const {status, data, error} = useQuery(USERS, () => get(USERS));
    return {status, users: data || [], error};
}

export function useGetVendors(uid) {
    const {status, data, error} = useQuery([VENDORS, {vendor: uid}], () => get(VENDORS, {elem: uid}));
    return {status, vendors: data || [], error};
}

export function useGetShops(uid) {
    const {status, data, error} = useQuery([SHOPS, {vendor: uid}], () => get(VENDORS, {elem: uid + '/' + SHOPS}));
    return {status, shops: data || [], error};
}

export function useGetShop(uid, shopId) {
    const {status, data, error} = useQuery([SHOPS, {shop: shopId}], () => get(VENDORS, {elem: uid + '/' + SHOPS + '/' + shopId}));
    return {status, shop: data || [], error};
}

export function useGetArticles(shopId) {
    const {status, data, error} = useQuery([ARTICLES, {shop: shopId}], () => get(SHOPS, {elem: shopId + '/' + ARTICLES}));
    return {status, articles: data || [], error};
}

export function useGetCategories() {
    const {status, data, error} = useQuery(CATEGORIES, () => get(VENDORS, {elem: CATEGORIES}));
    return {status, categories: data || [], error};
}

export function useGetCoordinates(address) {
    const {status, data, error} = useQuery([COORDINATES, address], () => get(VENDORS, {elem: COORDINATES + '/' + address}));
    return {status, coordinates: data || [], error};
}

export function useGetCoupons(organizationId) {
    let elem = '';
    if(organizationId)
        elem = organizationId + '/' + 'get-coupons';
    else
        elem = 'get-coupons'

    const {status, data, error} = useQuery([COUPONS, {organization: organizationId}], () => get(ORGANIZATIONS , {elem: elem}));
    return {status, coupons: data || [], error};
}

export function useGetProfileUser(uid) {
    const {status, data, error} = useQuery([USERS, {user: uid}], () => get(USERS, {elem: uid}));
    return {status, user: data || {}, error};
}

export function useGetProfileUserSensors(uid) {
    const {
        status,
        data,
        error
    } = useQuery([USERS, {user: uid}, SENSORS], () => get(USERS, {elem: uid + "/" + SENSORS}));
    return {status, sensors: data || [], error};
}

export function useGetProfileUserSmartphones(uid) {
    let {status, data, error} = useQuery([USERS, {user: uid}, SMARTPHONES], () => get(USERS ,{elem: uid + "/" + SMARTPHONES}));
    return {status, smartphones: data || [], error};
}

export function useGetProfileUserEnrollments(uid) {
    const {
        status,
        data,
        error
    } = useQuery([USERS, {user: uid}, ENROLLMENTS], () => get(USERS, {elem: uid + "/" + ENROLLMENTS}));
    return {status, enrollments: data || [], error};
}

export function useGetProfileUserAchievements(uid) {
    const {
        status,
        data,
        error
    } = useQuery([USERS, {user: uid}, ACHIEVEMENTS], () => get(USERS, {elem: uid + "/" + ACHIEVEMENTS}));
    return {status, achievements: data || [], error};
}

export function useGetSessions() {
	const {status, data, error} = useQuery(SESSIONS, () => get(SESSIONS));
	return {status, sessions: data, error};
}

export function useGetLatestSessions(limit) {
	const {status, data, error} = useQuery([SESSIONS,{limit}], () => get(SESSIONS, {params: {limit}}));
	return {status, latestSessions: data, error};
}

export function useGetSessionInfo(id) {
    const {status, data, error} = useQuery([SESSIONS, {id : id}], () => get(SESSIONS, {elem: id, params: {partials: true}}));
    return {status, session: data, error};
}

export function useGetPartialParameters() {
    const {status, data, error} = useQuery(SESSIONS + "DefaultParameters", () => get(SESSIONS + "/parameters"));
    return {status, defaultParameters: data || [], error};
}

/**
 * restituisce una lista di città
 * @returns {{cities: (*|*[]|Array.<{istatId:int, city:string}>), error: unknown, status: "idle" | "error" | "loading" | "success"}}
 */
export function useGetCities() {
    const {status, data, error} = useQuery(CITIES, () => get(CITIES), {staleTime: Infinity});
    return {status, cities: data || [], error};
}

export function useGetCity(istatId) {
    const {status, data, error} = useQuery([CITIES, {istatId : istatId}], () => get(CITIES, {elem: istatId}), {staleTime: Infinity});
    return {status, city: data || {}, error};
}

export function useGetOrganizations() {
    const {status, data, error} = useQuery(ORGANIZATIONS, () => get(ORGANIZATIONS));
    return {status, organizations: data || [], error};
}

export function useGetOrganization(id) {
    const {status, data, error} = useQuery([ORGANIZATIONS, {id: id}], () => get(ORGANIZATIONS, {elem: id}));
    return {status, organization: data || {}, error};
}

export function useGetOrganizationCodes(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, CODES], () => get(ORGANIZATIONS, {elem: id + "/" + CODES}));
    return {status, codes: data || [], error};
}

export function useGetOrganizationSeats(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, SEATS], () => get(ORGANIZATIONS, {elem: id + "/" + SEATS}));
    return {status, seats: data || [], error};
}

export function useGetOrganizationManagers(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, USERS], () => get(ORGANIZATIONS, {elem: id + "/" + USERS}));
    return {status, managers: data || [], error};
}

export function useGetOrganizationMessages(id) {
    const {status, data, error} = useQuery([ORGANIZATIONS, {id: id}, MESSAGES], () => get(ORGANIZATIONS, {elem: id + "/" + MESSAGES}));
    return {status, messages: data || [], error};
}

export function useGetMessageDetails(id) {
    const {status, data, error} = useQuery([MESSAGES, {id: id}], () => get(MESSAGES, {elem: id}));
    return {status, message: data || [], error};
}

export function useGetOrganizationRanks(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, RANKINGS], () => get(ORGANIZATIONS, {elem: id + "/" + RANKINGS}));
    return {status, ranks: data || [], error};
}

export function useGetOrganizationCustomField(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, CUSTOMFIELDS], () => get(ORGANIZATIONS, {elem: id + "/" + CUSTOMFIELDS}));
    return {status, customField: data || [], error};
}

export function useGetOrganizationAchievements(id) {
    const {
        status,
        data,
        error
    } = useQuery([ORGANIZATIONS, {id: id}, ACHIEVEMENTS], () => get(ORGANIZATIONS, {elem: id + "/" + ACHIEVEMENTS}));
    return {status, achievements: data || [], error};
}

export function useGetOrganizationSettings(id) {
    const {status, data, error} = useQuery([ORGANIZATIONS, {id: id}, SETTINGS], () => get(ORGANIZATIONS, {elem: id + "/" + SETTINGS}));
    return {status, settings: data || [], error};
}

export function useGetOrganizationPartials(id, filters) {
	const {status, data, error} = useQuery([ORGANIZATIONS, {id: id}, PARTIALS, {filter: filters}],
        () => get(ORGANIZATIONS, {elem: id + "/" + PARTIALS, params: filters}),{staleTime: Infinity});
	return {status, partials: data, error};
}

export function useGetNationalRanks() {
    const {status, data, error} = useQuery(["National", RANKS], () => get(RANKINGS, {params: {"national": true}}));
    return {status, ranks: data || [], error};
}

export function useGetNationalAchievements() {
    const {
        status,
        data,
        error
    } = useQuery(["National", ACHIEVEMENTS], () => get(ACHIEVEMENTS, {params: {"national": true}}));
    return {status, achievements: data || [], error};
}

export function useGetRank(id) {
    const {status, data, error} = useQuery([RANKS, id], () => get(RANKINGS, {elem: id, params: {"withUsers": true}}));
    return {status, rank: data || {}, error};
}

export function useGetAchievement(id) {
    const {status, data, error} = useQuery([ACHIEVEMENTS, id], () => get(ACHIEVEMENTS, {elem: id + "/users"}));
    return {status, achievement: data || [], error};
}

export function useGetRankingAwards(rankingId) {
    const {status, data, error} = useQuery([AWARDS, {rid: rankingId}], () => get(`${RANKINGS}/${rankingId}/${AWARDS}`));
    return {status, awards: data || [], error};
}

export function useGetAchievementsAwards(achievementId) {
    const {
        status,
        data,
        error
    } = useQuery([AWARDS, {aid: achievementId}], () => get(`${ACHIEVEMENTS}/${achievementId}/${AWARDS}`));
    return {status, awards: data || [], error};
}

export function getTableColumns(key, defaultColumns) {
    let colJson = localStorage.getItem(key);
    if (colJson) {
        let visibles = JSON.parse(colJson)
        let cols = defaultColumns.slice()
        cols.forEach(c => c.hide = (visibles.find(v => v.headerName === c.headerName) || {}).hide)
        return cols
    }
    return defaultColumns
}

export function useGetCustomAwards() {
    const {status, data, error} = useQuery(["AwardsCustom"], () => get(`${AWARDS}/customs`));
    return {status, customAwards: data || [], error};
}

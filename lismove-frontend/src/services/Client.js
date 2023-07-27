import {baseUrl} from "../constants/network";
import {firebaseAuth} from "../firebase";

let axios = require('axios');

export const USERS = "users";
export const MESSAGES = "messages";
export const SENSORS = "sensor"
export const SMARTPHONES = "smartphones"
export const ENROLLMENTS = "enrollments";
export const SESSIONS = "sessions";
export const SESSIONPOINTS = "sessionPoints";
export const OFFLINE_SESSIONS = "sessions/offline";
export const CITIES = "cities";
export const ORGANIZATIONS = "organizations";
export const CODES = "codes";
export const SEATS = "seats";
export const RANKS = "ranks";
export const RANKINGS = "rankings";
export const CUSTOMFIELDS = "custom-fields";
export const ACHIEVEMENTS = "achievements";
export const AWARDS = "awards";
export const SETTINGS = "settings";
export const VENDORS = "vendors";
export const SHOPS = 'shops';
export const CATEGORIES = 'categories';
export const COORDINATES = 'coordinates';
export const ARTICLES = 'articles';
export const PARTIALS = 'partials';
export const COUPONS = "coupon";
export const INSERTIONS = "insertions";

export function get(url, config = {elem: "", params: {}, header: {}}) {
	return firebaseAuth.currentUser.getIdToken(true).then(token => axios.get(getUrl(url, config.elem), getConfig(config, token)).then(({data}) => data));
}

export function post(url, config = {elem: "", body: {}, params: {}, header: {}}) {
	return firebaseAuth.currentUser.getIdToken(true).then(token => axios.post(getUrl(url, config.elem), config.body, getConfig(config, token)));
}

export function put(url, config = {elem: "", body: {}, params: {}, header: {}}) {
	return firebaseAuth.currentUser.getIdToken(true).then(token => axios.put(getUrl(url, config.elem), config.body, getConfig(config, token)));
}

export function deleteElem(url, config = {elem: "", body: {}, params: {}, header: {}}) {
	return firebaseAuth.currentUser.getIdToken(true).then(token => axios.delete(getUrl(url, config.elem), getConfig(config, token)));
}

export function download(url) {
	return firebaseAuth.currentUser.getIdToken(true).then(token => axios.get(getUrl(url), {responseType: 'blob', ...getConfig({}, token)}));
}

export function getErrorMessage(e) {
	let error = "Unknown error";
	if (e.response) {
		// The request was made and the server responded with a status code
		// that falls out of the range of 2xx
		error = e.response.data.error;
	} else {
		// Something happened in setting up the request that triggered an Error
		error = e.message || e.error;
	}
	console.log(error);
	return error;
}

function getConfig({params = {}, headers = {}}, token) {
	if (token) headers['Authorization'] = `Bearer ${token}`;
	return {
		params: params,
		headers
	};
}

export function getUrl(url, elem) {
	return elem ? `${baseUrl}${url}/${elem}` : `${baseUrl}${url}`;
}


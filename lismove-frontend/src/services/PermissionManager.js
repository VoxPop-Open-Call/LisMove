const roles = {
	USER: "DEVELOPER",
	ADMIN: "ADMIN",
	SUPER: "SUPERUSER"
};

const types = [
	{
		code: roles.USER,
		name: "utente",
	},
	{
		code: roles.ADMIN,
		name: "amministratore",
	},
	{
		code: roles.SUPER,
		name: "superutente",
	},
];

const adminResources = {
	VENUE: "venues",
	DEVELOPER: "developers",
	USER: "users",
};

const resources = {
	BEACONS: "beacons",
	EVENTS: "events",
	OUTDOOR: "outdoor",
	POI: "poi",
	PATH: "paths",
	ASSET: "assets",
	DEVELOPER: "developers",
	MAPS: "maps",
	REALTIME: "realtime",
	ANALYTICS: "stats",
	TOURS: "tours",
	CONTACT_TRACING: "contact_tracing",
};
let isSuperUser = user => user.role === roles.SUPER;

let isAdmin = user => user.role === roles.ADMIN || isSuperUser(user);

let getRoleName = role => types.find(t => t.code === role).name;

//Se superutente posso assegnare tutte le risorse, se Admin posso assegnare tutte le risorse che mi sono state assegnate, con l'eccezione di quelle riservate agli admin
let getUserResources = (user, venue) => isSuperUser(user) ? Object.values(resources) : user.authorizations.filter(a => a.venue === venue.id).map(a => a.resource).filter(r => !Object.values(adminResources).find(e => e === r));

export {
	adminResources,
	resources,
	roles,
	types,
	getRoleName,
	isAdmin,
	isSuperUser,
	getUserResources
};

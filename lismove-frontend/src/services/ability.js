import { Ability, AbilityBuilder } from "@casl/ability"

export const roles = {
    LISMOVER: 0,
    MANAGER: 2,
    VENDOR: 1,
    ADMIN: 3
};

const abilities = {
    WRITE: "write",
    READ: "read"
}

const resources = {
    NATIONALACHIEVEMENTS: "nationalAchievements",
    NATIONALRANKS: "nationalRanks",
    DRINKINGFOUNTAINS: "drinkingFountains",
    DEBUG: "debug",
    REVOLUT: "revolut",
    VENDOR: "vendor",
    VENDOR_PROFILE: "vendorProfile",
    COUPONS: "coupons"
};

function subjectName(item) {
    if (!item || typeof item === "string") {
        return item
    }
    return item.__type
}

function defineRulesFor(user) {

    const { can, cannot, rules } = new AbilityBuilder()
    if (user.userType === roles.ADMIN) {
        can(abilities.WRITE, "all")
        can(abilities.READ, "all");
    }
    if(user.userType === roles.VENDOR){
        can(abilities.WRITE, resources.VENDOR);
        can(abilities.READ,resources.VENDOR);

        can(abilities.WRITE, resources.VENDOR_PROFILE)
        can(abilities.READ,resources.VENDOR_PROFILE);
    }
    if(user.userType === roles.MANAGER){

    }
    return rules
}
function ability(user) {
    return new Ability(defineRulesFor(user || {}), { subjectName });
}

export default ability;

export {
    resources
}

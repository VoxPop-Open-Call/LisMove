import dayjs from "dayjs";
import {checkVAT, countries, italy} from "jsvat";

/**
 * verifica che una variabile é vuota
 * @param string
 * @returns {boolean}
 */
export function isEmpty(string){
    if(string){
        return !Boolean((string + '').replace(/\s/g, ''));
    }
    return true
}

export function isValidVat(vat)
{
    if(isEmpty(vat))
        return false
    return checkVAT(vat,countries).isValid;
}

export function isValidIBAN(input) {
    let CODE_LENGTHS = {
        AD: 24, AE: 23, AT: 20, AZ: 28, BA: 20, BE: 16, BG: 22, BH: 22, BR: 29,
        CH: 21, CY: 28, CZ: 24, DE: 22, DK: 18, DO: 28, EE: 20, ES: 24,
        FI: 18, FO: 18, FR: 27, GB: 22, GI: 23, GL: 18, GR: 27, GT: 28, HR: 21,
        HU: 28, IE: 22, IL: 23, IS: 26, IT: 27, JO: 30, KW: 30, KZ: 20, LB: 28,
        LI: 21, LT: 20, LU: 20, LV: 21, MC: 27, MD: 24, ME: 22, MK: 19, MR: 27,
        MT: 31, MU: 30, NL: 18, NO: 15, PK: 24, PL: 28, PS: 29, PT: 25, QA: 29,
        RO: 24, RS: 22, SA: 24, SE: 24, SI: 19, SK: 24, SM: 27, TN: 24, TR: 26,
        AL: 28, BY: 28, CR: 22, EG: 29, GE: 22, IQ: 23, LC: 32, SC: 31, ST: 25,
        SV: 28, TL: 23, UA: 29, VA: 22, VG: 24, XK: 20
    };
    let iban = String(input).toUpperCase().replace(/[^A-Z0-9]/g, ''), // keep only alphanumeric characters
        code = iban.match(/^([A-Z]{2})(\d{2})([A-Z\d]+)$/), // match and capture (1) the country code, (2) the check digits, and (3) the rest
        digits;
    // check syntax and length
    if (!code || iban.length !== CODE_LENGTHS[code[1]]) {
        return false;
    }
    // rearrange country code and check digits, and convert chars to ints
    digits = (code[3] + code[1] + code[2]).replace(/[A-Z]/g, function (letter) {
        return letter.charCodeAt(0) - 55;
    });
    // final check
    return mod97(digits) === 1;
}

function mod97(string) {
    let checksum = string.slice(0, 2), fragment;
    for (let offset = 2; offset < string.length; offset += 7) {
        fragment = String(checksum) + string.substring(offset, offset + 7);
        checksum = parseInt(fragment, 10) % 97;
    }
    return checksum;
}

/**
 * controlla se la string adata e' un web address valido
 * @param string
 * @returns {boolean}
 */
export function isValidHttpUrl(string) {
    if(!isEmpty(string)){
        let regexp = /^(?:(?:https?|ftp):\/\/)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/\S*)?$/;
        return regexp.test(string);
    }
    return false;
}

export function isValidPhone(phoneNumber)
{
    if(isEmpty(phoneNumber))
        return false
    let regEx = /^\+{0,2}([\-. ])?(\(?\d{0,3}\))?([\-. ])?\(?\d{0,3}\)?([\-. ])?\d{3}([\-. ])?\d{4}/;
    return phoneNumber.match(regEx);
}

/**
 * convet unix timestamp to correct date format
 * @param date
 * @returns {string|null}
 */
export function unixToString(date) {
    if (date) {
        date = dayjs(new Date(date));
        return date.format('YYYY-MM-DD');//mm-gg-aaaa
    }
    return null;
}

/**
 * convet unix timestamp to correct date format
 * @param date
 * @returns {null|number}
 */
export function stringToUnix(date) {
    if (date) {
        return dayjs(date).valueOf();
    }
    return null;
}
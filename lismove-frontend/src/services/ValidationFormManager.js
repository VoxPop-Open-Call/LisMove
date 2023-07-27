import {isEmpty, isValidIBAN, isValidPhone, isValidVat} from "./helper";
import {INVALID_IBAN, INVALID_PHONE, INVALID_VAT, MANDATORY} from "../constants/errorMessages";
import {checkVAT, countries, italy} from "jsvat";

/**
 *
 * @param data oggetto data con tutti i campi del form
 * @returns {({}|boolean)[]}
 */
export function validateVendorData(data){
    let isValid = true;
    let errors = {};
    if(isEmpty(data.businessName)){
        errors.businessName = MANDATORY;
        isValid = false;
    }
    if(isEmpty(data.address)){
        errors.address = MANDATORY;
        isValid = false;
    }
    if(!isValidPhone(data.phone)){
        errors.phone = INVALID_PHONE;
        isValid = false;
    }
    if(!isValidVat(data.vatNumber)){
        errors.vatNumber = INVALID_VAT;
        isValid = false;
    }
    if(!isValidIBAN(data.iban)){
        errors.iban = INVALID_IBAN;
        isValid = false;
    }
    if(isEmpty(data.bic)){
        errors.bic = MANDATORY;
        isValid = false;
    }
    return [errors,isValid]
}
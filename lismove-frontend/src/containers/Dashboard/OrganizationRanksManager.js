import {useParams} from "react-router-dom";
import {useGetOrganizationCustomField,useGetOrganizationRanks} from "../../services/ContentManager";
import RanksManager from "../RanksManager";


export default function OrganizationRanksManager(){

    let {id} = useParams();
    let {ranks = []} = useGetOrganizationRanks(id);
    let {customField = []} = useGetOrganizationCustomField(id);

    return <RanksManager ranks={ranks} organizationId={id} customField={customField}/>
}
import {useParams} from "react-router-dom";
import {useGetOrganizationAchievements,useGetOrganizationCustomField} from "../../services/ContentManager";
import AchievementsManager from "../AchievementsManager";


export default function OrganizationAchievements(){

    let {id} = useParams();
    let {achievements = []} = useGetOrganizationAchievements(id);
    let {customField = []} = useGetOrganizationCustomField(id);

    return <AchievementsManager achievements={achievements} organizationId={id}  customField={customField}/>
}
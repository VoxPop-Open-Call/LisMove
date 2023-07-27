import {useGetProfileUserEnrollments} from "../../services/ContentManager";
import {GridCellExpand} from "./renderCellExpand";

export default function RenderUserEnrollments(params){

    const {enrollments = []} = useGetProfileUserEnrollments(params.id)
    let values = [];
    let active = enrollments.filter(e => e.endDate > new Date())
    let expired = enrollments.filter(e => e.endDate < new Date())
    if(active.length !== 0) {
        values.push("Attive:")
        active.map(e => values.push(e.organizationTitle))
    }
    if(expired.length !== 0){
        values.push("Scadute:")
        expired.map(e => values.push(e.organizationTitle))
    }

    return <GridCellExpand value={values.length !== 0 ? values : ""} width={params.colDef.width} placeholder={ values.length !== 0 ? values[0] + " " + values[1]+"..." : ""}/>

}
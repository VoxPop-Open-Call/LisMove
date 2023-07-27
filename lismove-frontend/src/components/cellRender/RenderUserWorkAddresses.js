import {GridCellExpand} from "./renderCellExpand";
import dayjs from "dayjs";

export default function RenderUserWorkAddresses(params) {

    if(params.row.workAddresses) {

        let values = []
        let placeholder = ""
        const active = params.row.workAddresses.filter(a => !a.endAssociation)
        const expired = params.row.workAddresses.filter(a => !!a.endAssociation)

        if(active.length !== 0) {
            values.push("Attivi:")
            active.map(wa => values.push("- " + wa.address + " n." + wa.number + ", " + wa.cityName + "\n  dal " + dayjs(wa.startAssociation).format("DD/MM/YYYY")))
        }

        if(expired.length !== 0) {
            values.push("Cancellati:")
            expired.map(wa => values.push("- " + wa.address + " n." + wa.number + ", " + wa.cityName + "\n  dal " + dayjs(wa.startAssociation).format("DD/MM/YYYY") + " al " + dayjs(wa.endAssociation).format("DD/MM/YYYY")))
        }

        if(active.length !== 0) placeholder = values[ 1 ] + "..."
        else if(expired.length !== 0) placeholder = "--"

        return <GridCellExpand value={values.length !== 0 ? values : ""} width={params.colDef.width} placeholder={placeholder}/>

    } else return ""
}
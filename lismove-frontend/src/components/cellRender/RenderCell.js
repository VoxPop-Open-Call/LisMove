import EditableLabel from "./EditableLabel";
import dayjs from "dayjs";

export default function RenderCell ({params, saveEdit, type, measureUnit = "", decimals, required, min, max, step, options, infoMessage, warningMessage}) {

    const getValue = () => {
        if(type === "date") return dayjs(params.value).format("DD/MM/YYYY")
        if(type ==="number" && decimals) return parseFloat(params.value).toFixed(decimals)
        return params.value;
    }

    let displayValue = "-";
    if(params.value !== null && params.value !== undefined) {
        displayValue = `${getValue()} ${measureUnit}`
    }

    return <div>
        {saveEdit ?
            <EditableLabel value={params.row[params.field]} required={required} displayValue={displayValue}
                        onChange={newValue => saveEdit(params.row.id,params.field,newValue)}
                        title={params.colDef.headerName} measureUnit={measureUnit}
                        type={type} min={min} max={max} step={step} options={options}
                        infoMessage={infoMessage} warningMessage={warningMessage}
            />
            :
            displayValue
        }
    </div>
}

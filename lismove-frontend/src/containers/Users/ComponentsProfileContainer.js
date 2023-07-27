import {
    useGetOrganizationCustomField,
    useGetProfileUserAchievements,
    useGetProfileUserEnrollments,
    useGetProfileUserSensors, useGetProfileUserSmartphones
} from "../../services/ContentManager";
import NTMXGrid,{timestampTypeToDate,timestampTypeToDateTime} from "../../components/NTMXGrid";
import React from "react";
import RenderBoolean from "../../components/cellRender/RenderBoolean";
import Avatar from "@mui/material/Avatar";
import getUsefulValues from "../../constants/rankingUsefulValues";
import getRankingFilter from "../../constants/rankingFilter";
import {sessionType} from "../../constants/sessionType";

export function UserSensors({uid}){

    const {sensors = []} = useGetProfileUserSensors(uid)

    const defaultColumns = [
        {
            headerName: 'Nome',
            field: 'name',
            width: 310,
        },
        {
            headerName: 'Tipo Bicicletta',
            field: 'bikeType',
            width: 250,
        },
        {
            headerName: 'Inizio Associazione',
            field: 'startAssociation',
            width: 250,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Fine Associazione',
            field: 'endAssociation',
            width: 250,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Diametro Ruota',
            field: 'wheelDiameter',
            width: 250,
        },
        {
            headerName: 'Versione Firmware',
            field: 'firmware',
            width: 250,
            hide: true,
        },
        {
            headerName: 'Rubato',
            field: 'stolen',
            width: 250,
            renderCell: (params) => <RenderBoolean params={params}/>
        },
    ];

    return <NTMXGrid
        columns={defaultColumns}
        rows={sensors || []}
        title="Sensori"
        getRowId={(row) => sensors && row.uuid}
    />
}

export function UserEnrollments({uid}){

    const {enrollments = []} = useGetProfileUserEnrollments(uid)

    const defaultColumns = [
        {
            headerName: 'Codice Usato',
            field: 'code',
            width: 250,
        },
        {
            headerName: 'Data attivazione',
            field: 'activationDate',
            width: 250,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Data inizio',
            field: 'startDate',
            width: 250,
            ...timestampTypeToDate
        },
        {
            headerName: 'Data fine',
            field: 'endDate',
            width: 250,
            ...timestampTypeToDate
        },
        {
            headerName: 'Organizzazione',
            field: 'organizationTitle',
            width: 250,
        },
        {
            headerName: 'Punti Acquisiti',
            field: 'points',
            width: 250,
        },
    ];

    return <NTMXGrid
        columns={defaultColumns}
        rows={enrollments || []}
        title="Iniziative"
    />
}

export function UserAchievements({uid}){

    const {achievements = []} = useGetProfileUserAchievements(uid)

    function OrganizationFilterValue ({params}) {
        let {customField = []} = useGetOrganizationCustomField(params.row.organization);
        const achievementsFilter = getRankingFilter(customField);

        return (params.value !== null && params.value !== undefined ) ? achievementsFilter.find(elem => elem.id === params.value).name : "";
    }

    function NationalFilterValue ({params}) {
        const achievementsFilter = getRankingFilter();

        return (params.value !== null && params.value !== undefined ) ? achievementsFilter.find(elem => elem.id === params.value).name : "";
    }

    const defaultColumns = [
        {
            headerName: 'Logo',
            field: 'logo',
            width: 100,
            renderCell: ({value}) => value ? <Avatar src={value}/> : "",
        },
        {
            headerName: 'Nome',
            field: 'name',
            width: 200
        },
        {
            headerName: 'Data inizio',
            field: 'startDate',
            width: 200,
            hide: true,
            ...timestampTypeToDate
        },
        {
            headerName: 'Data fine',
            field: 'endDate',
            width: 200,
            hide: true,
            ...timestampTypeToDate
        },
        {
            headerName: 'Durata in Giorni',
            field: 'duration',
            width: 200
        },
        {
            headerName: 'Valore',
            field: 'value',
            width: 200,
            valueGetter: (params) => {
                const usefulValues = getUsefulValues(!params.row.organization);
                return usefulValues.find(elem => elem.id === params.value).name
            }
        },
        {
            headerName: 'Target',
            field: 'target',
            width: 200,
        },
        {
            headerName: 'Filtro',
            field: 'filter',
            width: 200,
            renderCell: (params) => {
                if(params.value){
                    if(params.row.organization) return <OrganizationFilterValue params={params}/>
                    else return <NationalFilterValue params={params}/>
                }
                else return ""
            }
        },
        {
            headerName: 'Valore filtro',
            field: 'filterValue',
            width: 200,
            valueGetter: (params) => {
                if(params.row["filter"] !== null && params.row["filter"] !== undefined){
                    if(params.row["filter"] === 0) {
                        return sessionType.find(st => st.id == params.value).name       //usa == e non ==== perchè uno dei due puo' essere una stringa e l'altro un numero
                    }
                    if(params.row["filter"] === 1) {
                        return params.value
                    }
                    if(params.row["filter"] === 2) {
                        return params.value === "M" ? "Maschio" : "Femmina";
                    }
                }
                return "";
            }
        },
        {
            headerName: 'Organizzazione',
            field: 'organizationTitle',
            width: 250,
        },
        {
            headerName: 'Punteggio',
            field: 'score',
            width: 250,
        },
        {
            headerName: 'Pieno',
            field: '"fullfilled": ',
            width: 250,
        }
    ];

    return <NTMXGrid
        columns={defaultColumns}
        rows={achievements || []}
        title="Coppe"
    />
}
export function UserSmartphones({uid}){

    let {smartphones = []} = useGetProfileUserSmartphones(uid)

    const defaultColumns = [
        {
            headerName: 'Modello',
            field: 'model',
            width: 310,
        },
        {
            headerName: 'Piattaforma',
            field: 'platform',
            width: 250,
        },
        {
            headerName: 'Versione App',
            field: 'appVersion',
            width: 250,
        },
        {
            headerName: 'Inizio Associazione',
            field: 'startAssociation',
            width: 250,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Fine Associazione',
            field: 'endAssociation',
            width: 250,
            ...timestampTypeToDateTime
        }
    ];

    return <NTMXGrid
        columns={defaultColumns}
        rows={[...smartphones].sort((a,b) => b.startAssociation - a.startAssociation) || []}
        title="Telefoni"
        getRowId={(row) => smartphones && row.id}
    />
}

export default function getRankingFilter (customField) {
    let rankingFilter = [
        {
            id : 0,
            name : "Tipo sessione"
        },
        {
            id : 1,
            name : "Anni"
        },
        {
            id : 2,
            name : "Genere"
        }
    ];
    if(customField) {
        customField.map((cf, i) => rankingFilter.push({
            id : i+3,
            name : cf.name
        }))
    }
    return rankingFilter;
}
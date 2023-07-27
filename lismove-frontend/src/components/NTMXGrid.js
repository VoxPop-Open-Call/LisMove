import React,{useState} from "react";
import makeStyles from '@mui/styles/makeStyles';
import {
    GridToolbarExport,
    GridToolbarFilterButton,
    GridToolbarColumnsButton,
    DataGridPro,
} from "@mui/x-data-grid-pro";
import Typography from "@mui/material/Typography";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid";
import dayjs from "dayjs";

const GRID_DEFAULT_LOCALE_TEXT = {
    // Root
    rootGridLabel: 'grid',
    noRowsLabel: 'Non ci sono righe',
    errorOverlayDefaultLabel: 'Si è verificato un errore.',

    // Density selector toolbar button text
    toolbarDensity: 'Densità',
    toolbarDensityLabel: 'Densità',
    toolbarDensityCompact: 'Compatto',
    toolbarDensityStandard: 'Standard',
    toolbarDensityComfortable: 'Confortevole',

    // Columns selector toolbar button text
    toolbarColumns: 'Colonne',
    toolbarColumnsLabel: 'Seleziona colonne',

    // Filters toolbar button text
    toolbarFilters: 'Filtri',
    toolbarFiltersLabel: 'Mostra filtri',
    toolbarFiltersTooltipHide: 'Nascondi filtri',
    toolbarFiltersTooltipShow: 'Mostra filtri',
    toolbarFiltersTooltipActive: (count) =>
        count !== 1 ? `${count} filtri attivi` : `${count} filtro attivo`,

    // Export selector toolbar button text
    toolbarExport: 'Esporta',
    toolbarExportLabel: 'Esporta',
    toolbarExportCSV: 'Scarica CSV',

    // Columns panel text
    columnsPanelTextFieldLabel: 'Cerca colonna',
    columnsPanelTextFieldPlaceholder: 'Titolo colonna',
    columnsPanelDragIconLabel: 'Reorder column',
    columnsPanelShowAllButton: 'Mostra tutte',
    columnsPanelHideAllButton: 'Nascondi tutte',

    // Filter panel text
    filterPanelAddFilter: 'Aggiungi filtro',
    filterPanelDeleteIconLabel: 'Cancella',
    filterPanelOperators: 'Operatori',
    filterPanelOperatorAnd: 'E',
    filterPanelOperatorOr: 'O',
    filterPanelColumns: 'Colonne',
    filterPanelInputLabel: 'Valore',
    filterPanelInputPlaceholder: 'Filtra valore',

    // Filter operators text
    filterOperatorContains: 'contiene',
    filterOperatorEquals: 'equivale',
    filterOperatorStartsWith: 'inizia con',
    filterOperatorEndsWith: 'finisce con',
    filterOperatorIs: 'è',
    filterOperatorNot: 'non è',
    filterOperatorAfter: 'è dopo',
    filterOperatorOnOrAfter: 'è uguale o dopo',
    filterOperatorBefore: 'è prima',
    filterOperatorOnOrBefore: 'è uguale o prima',

    // Column menu text
    columnMenuLabel: 'Menu',
    columnMenuShowColumns: 'Mostra colonne',
    columnMenuFilter: 'Filtra',
    columnMenuHideColumn: 'Nascondi',
    columnMenuUnsort: 'Elimina ordine',
    columnMenuSortAsc: 'Ordina Crescente',
    columnMenuSortDesc: 'Ordina Decrescente',

    // Column header text
    columnHeaderFiltersTooltipActive: (count) =>
        count !== 1 ? `${count} filtri attivi` : `${count} filtro attivo`,
    columnHeaderFiltersLabel: 'Mostra filtri',
    columnHeaderSortIconLabel: 'Ordina',

    // Rows selected footer text
    footerRowSelected: (count) =>
        count !== 1
            ? ``
            : ``,

    // Total rows footer text
    footerTotalRows: 'Righe Totali:',
};

const useStyles = makeStyles((theme) => ({
    root : {
        border: 0,
        '& .MuiDataGrid-columnsContainer' : {
            backgroundColor : theme.palette.primary.main,
            color : "#FFF",
            textTransform : "uppercase",
        },
        '& .MuiIconButton-root': {
            color: theme.palette.secondary.main
        }
    },
    title: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
        textAlign: "center"
    }
}))



export const timestampTypeToDateTime = {
    valueFormatter: ({ value }) => value ? dayjs(new Date(value)).format("HH:mm:ss DD/MM/YYYY") : "",
}
export const timestampTypeToDate = {
    valueFormatter: ({ value }) => value ? dayjs(new Date(value)).format("DD/MM/YYYY") : "",
}
export const timestampTypeToTime = {
    valueFormatter: ({ value }) => value ? dayjs(new Date(value)).format("HH:mm:ss") : "",
}

export const timestampToDuration = {
    valueFormatter: ({value}) => value ? dayjs.duration(value*1000).format("HH:mm:ss") : "",
}

function CustomToolbar({title, rightButton}) {

    const classes = useStyles();

    return (
        <Toolbar>
            <Grid container>
                <Grid item xs={12} lg={5}>
                    <GridToolbarColumnsButton />
                    <GridToolbarFilterButton />
                    <GridToolbarExport />
                </Grid>
                <Grid item xs={12} lg={2}>
                    <Typography variant="h6" className={classes.title}>
                        {title}
                    </Typography>
                </Grid>
                <Grid item xs={12} lg={5}>
                    {rightButton}
                </Grid>
            </Grid>
        </Toolbar>
    );
}

//https://github.com/mui-org/material-ui-x/blob/18cd8e182b74a2cfa5889c7b9fba8d7d5016441d/packages/grid/_modules_/grid/models/api/gridColumnApi.ts
export default function NTMXGrid({columns, rows, title, options={}, rightButton, getRowId, density = "standard", apiRef, checkboxSelection, onSelectionModelChange,
                                     selectionModel, isRowSelectable, disableToolbar, autoHeight, defaultPageSize = 25, rowsPerPageOptions,  height, onColumnVisibilityChange}){

    const classes = useStyles();
    let [pageSize, setPageSize] = useState(defaultPageSize);

    return (
        <div style={{ height: height || '75vh', width: '100%' }}>
            <DataGridPro
                apiRef={apiRef}
                disableColumnMenu
                pagination
                checkboxSelection={checkboxSelection}
                onColumnVisibilityChange={onColumnVisibilityChange}
                pageSize={pageSize}
                onPageSizeChange={(newPageSize) => setPageSize(newPageSize)}
                rowsPerPageOptions={rowsPerPageOptions || [25, 50, 100]}
                className={classes.root}
                getRowId={getRowId}
                columns={columns}
                density	={density} //comfortable, standard, compact
                rows={rows}
                onSelectionModelChange={onSelectionModelChange}
                selectionModel={selectionModel}
                isRowSelectable={isRowSelectable}
                localeText = {GRID_DEFAULT_LOCALE_TEXT}
                components={{
                    Toolbar:  () => !disableToolbar && <CustomToolbar title={title} rightButton={rightButton}/>
                }}
                autoHeight={autoHeight}
                {...options}
            />
        </div>
    );
}

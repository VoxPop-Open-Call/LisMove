import React from 'react';
import ReactDOM from 'react-dom';
import reportWebVitals from './reportWebVitals';

import { adaptV4Theme } from "@mui/material";
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { StylesProvider } from "@mui/styles";
import {SnackbarProvider} from "notistack";
import Button from "@mui/material/Button";
import {LicenseInfo} from "@mui/x-data-grid-pro";

import {
    QueryClient,
    QueryClientProvider
} from "react-query";
import App from "./containers/App";
import {itIT} from "@mui/material/locale";
import {LoadScript} from "@react-google-maps/api";
import {libraries} from "./components/MapContainer";
import LocalizationProvider from '@mui/lab/LocalizationProvider'
import AdapterDateFns from '@mui/lab/AdapterDateFns';
import it from "date-fns/locale/it"
import {Close} from "@mui/icons-material";

const theme = createTheme(adaptV4Theme({
    palette: {
        primary: {
            main: "#dd3333",
            light: "#fcd4d4",
            dark: "#e14d43", //"#32323a",
            contrastText: '#fff',
        },
        secondary: {
            main: "#32323a", //"#e14d43",
            light: "#e7e7ef",
        },
        text: {
            primary: "#290e08"
        },
        error: {
            main: "#ec1807",
            light: "#ffc900"
        }
    },
    typography: {
        body1: {
            fontSize: "0.875rem",
            lineHeight: 1.43,
            letterSpacing: "0.01071em",
        }
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    whiteSpace: "nowrap",
                    verticalAlign: "middle",
                    userSelect: "none",
                    border: "1px solid transparent",
                    padding: "0.75rem 1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    borderRadius: 0,
                    minWidth: "auto",
                    fontSize: "0.875rem",
                    lineHeight: "1.25",
                }
            }
        },
        MuiTableRow: {
            styleOverrides: {
                root : {
                    "&:hover" : {backgroundColor : '#D4EDFC'}
                }
            }
        },
        MuiCssBaseline: {
            styleOverrides: {
                body: {
                    fontSize: '0.875rem',
                    lineHeight: 1.43,
                    letterSpacing: '0.01071em',
                },
            },
        },
    }
}, itIT));

const notistackRef = React.createRef();
const onClickDismiss = key => () => {
    notistackRef.current.closeSnackbar(key);
};

LicenseInfo.setLicenseKey(
    '',
);

const queryClient = new QueryClient();


ReactDOM.render(
    <React.StrictMode>
        <StylesProvider injectFirst>
            <ThemeProvider theme={theme}>
                <LocalizationProvider dateAdapter={AdapterDateFns} locale={it}>
                    <SnackbarProvider
                        ref={notistackRef}
                        action={(key) => (
                            <Button onClick={onClickDismiss(key)}>
                                {<Close/>}
                            </Button>
                        )}
                        autoHideDuration={3000}>
                        <LoadScript googleMapsApiKey={""} libraries={libraries}>
                            <QueryClientProvider client={queryClient}>
                                <App/>
                            </QueryClientProvider>
                        </LoadScript>
                    </SnackbarProvider>
                </LocalizationProvider>
            </ThemeProvider>
        </StylesProvider>
    </React.StrictMode>,
    document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

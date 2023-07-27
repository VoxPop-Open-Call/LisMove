import {
    Button,
    ClickAwayListener,
    FormControl, Grid,
    Grow,
    InputAdornment,
    MenuItem, MenuList,
    Paper,
    Popper
} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import React, {useEffect, useRef, useState} from "react";
import TextInput from "./forms/TextInput";
import {ArrowDropDown, ArrowDropUp, Close} from "@mui/icons-material";


/**
 * select con opzione di ricerca
 * @param label
 * @param value {string | Array.<string>} impostato come array viene considerata come selezione multipla
 * @param items {Array.<{value:string, text:string}>} array di elementi
 * @param onChange
 * @param onDelete callback se è multiselect e si elimina una selezione
 * @param maxItemsSize dimesione massima degli elementi della select mostrati
 * @returns {JSX.Element}
 * @constructor
 */
export default function NTMSelect({label, value, items, onChange, onDelete, maxItemsSize = 20, required, error, color= "primary"}) {
    //value può essere un array nel caso di selezione multipla
    let isArray = value instanceof Array;
    const useStyles = makeStyles(theme => ({
        root: {
            width: '100%',

        },
        marginRight: {
            marginRight: theme.spacing(2),
        },
        selected: {
            backgroundColor: theme.palette.secondary.light
        },
        input: {
            '& .MuiInputBase-input': {
                cursor: 'pointer'
            },
            '& input': isArray && //se è un array allora nascondo l'input per mostrare i pulsanti della multiselezione
                {
                    visibility: 'hidden',
                    width: 0
                },
        },
        search: {
            marginTop: theme.spacing(1)
        },
        item: {
            padding: theme.spacing(0.5),
            cursor: 'pointer'
        },
        paper: {
            padding: theme.spacing(0.5),
            backgroundColor: theme.palette.secondary.light
        },
        close: { fontSize:'1rem'},
        list: {
            maxHeight: '50vh',
            overflowY: 'hidden',
            overflowX: 'hidden',
            textOverflow: 'ellipsis',
        },
        multiselect:{
            marginTop:theme.spacing(0.5),
            marginBottom:theme.spacing(0.5)
        }
    }));

    //elemento della multiselect
    function Item({onClick, children}) {
        return (
            <Grid item className={classes.item}>
                <Paper
                    onClick={(e) => {
                        e.stopPropagation();
                        onClick()
                    }} className={classes.paper}
                >
                    <Grid container direction={'row'} alignItems={'center'} alignContent={'center'}>
                        <Close className={classes.close}/>
                        {children}
                    </Grid>
                </Paper>
            </Grid>
        );
    }

    const classes = useStyles();

    const [open, setOpen] = useState(false);

    const [searched, setSearched] = useState([]);
    useEffect(() => {
        setSearched(() => items.slice(0, maxItemsSize));
    }, [items]);

    const anchorRef = React.useRef(null);

    const handleClose = (event) => {
        if (anchorRef.current && anchorRef.current.contains(event.target)) {
            return;
        }

        setOpen(false);
    };

    function handleListKeyDown(event) {
        if (event.key === 'Tab') {
            event.preventDefault();
            setOpen(false);
        }
    }

    // return focus to the button when we transitioned from !open -> open
    const prevOpen = useRef(open);
    useEffect(() => {
        if (prevOpen.current === true && open === false) {
            anchorRef.current.focus();
        }

        prevOpen.current = open;
    }, [open]);

    //se è singola allora value sarà una stringa altrimenti sarà un array di items
    let values = [];
    let text = '';
    if(isArray)
        values = items.filter(i => !!value.find(v => v === i.value));
    else
        text = (items.slice().find(i => i.value === value) || {}).text

    return (
        <FormControl variant="outlined" className={classes.root}>
            <TextInput
                label={label}
                value={text || ''}
                readOnly
                required={required}
                error={error}
                InputProps={{
                    endAdornment:
                        <InputAdornment position="end" style={{cursor:'pointer'}}> {open ? <ArrowDropUp/> : <ArrowDropDown/>} </InputAdornment>,
                    startAdornment:
                        isArray &&
                        <Grid container direction={'row'} className={classes.multiselect}>
                            {
                                values.map((item) => <Item key={item.value}
                                                          onClick={() => onDelete(item.value)}>{item.text}</Item>)
                            }
                        </Grid>
                }}
                color={color}
                onClick={() => {
                    setOpen((open) => !open);
                }}
                className={classes.input}
            />
            {/* usato solo per collegare il menù a tendina */}
            <Button
                ref={anchorRef}
                style={{visibility: 'hidden', height: 0, margin: 0, padding: 0}}
                aria-controls={open ? 'menu-list-grow' : undefined}
                aria-haspopup="true"
            />
            <Popper open={open} anchorEl={anchorRef.current} role={undefined} transition disablePortal
                    style={{width: '100%', zIndex: 100, marginTop: -7}}>
                {({TransitionProps, placement}) => (
                    <Grow
                        {...TransitionProps}
                        style={{
                            transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom',
                        }}
                    >
                        <ClickAwayListener onClickAway={handleClose}>
                            <Paper style={{marginTop: placement !== 'bottom' && '5rem'}}>
                                <MenuItem>
                                    <TextInput
                                        className={classes.search}
                                        label={"Cerca"}
                                        type="text"
                                        color={'primary'}
                                        onTextChange={(text) => setSearched(
                                            () => items.filter(s => !text || s.text.toLowerCase().includes(text.toLowerCase())).slice(0, maxItemsSize)
                                        )}
                                        autoFocus={open}
                                    />
                                </MenuItem>
                                <MenuList id="menu-list-grow" onKeyDown={handleListKeyDown} className={classes.list}>
                                    {
                                        searched.map(i =>
                                            <MenuItem
                                                className={(isArray ? values.find(v => v.value === i.value) : value === i.value) ? classes.selected : ''}
                                                key={i.value}
                                                onClick={(e) => {
                                                    onChange(i.value);
                                                    handleClose(e);
                                                }}
                                            >
                                                {i.text}
                                            </MenuItem>
                                        )
                                    }
                                </MenuList>
                            </Paper>
                        </ClickAwayListener>
                    </Grow>
                )}
            </Popper>

        </FormControl>
    );
}
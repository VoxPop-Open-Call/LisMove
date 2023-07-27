import * as React from 'react';
import makeStyles from '@mui/styles/makeStyles';
import PropTypes from 'prop-types';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Popper from '@mui/material/Popper';
import Grid from "@mui/material/Grid";
import EditIcon from "@mui/icons-material/Edit";
import {useState} from "react";
import IconButton from "@mui/material/IconButton";
import EditExpandedCellModal from "../modals/EditExpandedCellModal";

const useStyles = makeStyles(theme => ({
    root: {
        alignItems: 'center',
        lineHeight: '24px',
        width: '100%',
        height: '100%',
        position: 'relative',
        display: 'flex',
        '& .cellValue': {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
        },
    },
    editable: {
        backgroundColor: theme.palette.primary.light,
    },
    editIcon: {
        color: theme.palette.primary.main
    }
}));

export const GridCellExpand = React.memo(function GridCellExpand(props) {
    const { width, value, saveEdit, placeholder } = props;
    let isString = typeof value === "string";
    const wrapper = React.useRef(null);
    const cellDiv = React.useRef(null);
    const cellValue = React.useRef(null);
    const [anchorEl, setAnchorEl] = React.useState(null);
    let [modal, setModal] = useState(false);
    const classes = useStyles();
    const [showFullCell, setShowFullCell] = React.useState(false);
    const [showPopper, setShowPopper] = React.useState(false);

    function isOverflown() {
        return !!value
    }

    const handleMouseEnter = () => {
        const isCurrentlyOverflown = isOverflown();
        setShowPopper(isCurrentlyOverflown);
        setAnchorEl(cellDiv.current);
        setShowFullCell(true);
    };

    const handleMouseLeave = () => {
        setShowFullCell(false);
    };

    React.useEffect(() => {
        if (!showFullCell) {
            return undefined;
        }

        function handleKeyDown(nativeEvent) {
            // IE11, Edge (prior to using Bink?) use 'Esc'
            if (nativeEvent.key === 'Escape' || nativeEvent.key === 'Esc') {
                setShowFullCell(false);
            }
        }

        document.addEventListener('keydown', handleKeyDown);

        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [setShowFullCell, showFullCell]);

    return (
        <div
            ref={wrapper}
            className={classes.root}
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
        >
            <div
                ref={cellDiv}
                style={{
                    height: 1,
                    width,
                    display: 'block',
                    position: 'absolute',
                    top: 0,
                }}
            />
            <div ref={cellValue} className="cellValue">
                {placeholder || (isString ? value : <p key={0}>{(saveEdit ? value[0].showValue : value[0]) + "..."}</p>)}
            </div>
            {showPopper && (
                <Popper
                    open={showFullCell && anchorEl !== null}
                    anchorEl={anchorEl}
                    style={{ width, marginLeft: -17 }}
                >
                    <Paper
                        elevation={1}
                        style={{ minHeight: wrapper.current.offsetHeight - 3 }}
                        className={saveEdit && classes.editable}
                    >
                        <Grid container direction="row" justifyContent="space-between">
                            <Typography variant="body2" style={{ padding: 8, "white-space": "pre-wrap" }}>
                                {isString ? value :
                                    value.map((v) => {
                                        return <div>{saveEdit ? v.showValue : v}</div>;
                                    })
                                }
                            </Typography>
                            {saveEdit &&
                                <IconButton onClick={() => setModal(true)} className={classes.editIcon} size="large">
                                    <EditIcon/>
                                </IconButton>
                            }
                        </Grid>
                    </Paper>
                </Popper>
            )}
            <EditExpandedCellModal open={modal} onSubmit={saveEdit} defaultValues={value}
                                   onClose={() => {
                                       setModal(false)
                                       setShowFullCell(false)
                                   }}/>
        </div>
    );
});

GridCellExpand.propTypes = {
    value: PropTypes.any.isRequired,
    width: PropTypes.number.isRequired,
};

export default function renderCellExpand(params, saveEdit) {
    return (
        <GridCellExpand
            value={params.value || ''}
            width={params.colDef.width}
            saveEdit={saveEdit}
        />
    );
}

renderCellExpand.propTypes = {
    /**
     * The column of the row that the current cell belongs to.
     */
    colDef: PropTypes.any.isRequired,
    /**
     * The cell value, but if the column has valueGetter, use getValue.
     */
    value: PropTypes.array.isRequired
};
import AccordionSummary from "@mui/material/AccordionSummary";
import makeStyles from '@mui/styles/makeStyles';
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import AccordionDetails from "@mui/material/AccordionDetails";
import Accordion from "@mui/material/Accordion";
import Grid from "@mui/material/Grid";
import {useState} from "react";

const useStyles = makeStyles((theme) => ({
    root: {
        margin: theme.spacing(1),
    },
    header: {
        fontWeight: "bold",
        width:'100%',
    },
    expanded: {
        backgroundColor: theme.palette.primary.dark
    },
    textItem: {
        color: "white",
        borderColor: theme.palette.primary.light,
        height: "100%",
    },
    container:{
        width:'100%'
    },
    image: {
        width:'2rem'
    },
}));

/**
 * @param title title when collapsed
 * @param titleExpanded title when expanded
 * @param smallIcon icon when collapsed
 * @param largeIcon icon when expanded
 * @param id
 * @param children  element when expanded AccordionDetails
 * @param icon  se presente smallIcon e largeIcon verranno ignorati e l'icona impostata sarà quella in icon
 * @param expanded valore di default di expanded se setExpanded non è settata
 * @param setExpanded callback quando c'è un espansione
 * @returns {JSX.Element}
 * @constructor
 */
export default function NTMExpansionPanel({title, titleExpanded = null, smallIcon, largeIcon, id, children, icon, expanded = false, setExpanded = null}) {

    if(icon)
    {
        smallIcon = largeIcon = icon;
    }

    let classes = useStyles();

    const [expandedState, setExpandedState] = useState(expanded);

    title = expandedState && titleExpanded ? titleExpanded : title;//se non c'é un titleExpanded allora imposta lo stesso titolo ad entrambi
    return (
        <Accordion
            TransitionProps={{unmountOnExit: true}}
            classes={{root: classes.root, expanded: classes.expanded}}
            square
            onChange={(e, expanded) => {
                if(setExpanded)
                    setExpanded(expanded)
                setExpandedState(expanded);
            }}
            id={id}
            expanded={setExpanded? expanded : expandedState}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon className={classes.expandMoreIcon} />}
                aria-controls="panel1a-content"
            >
                <Grid
                    container
                    direction="row"
                    justifyContent="flex-start"
                    alignItems="center"
                >
                    <Grid className={classes.image}>
                        {expandedState ? largeIcon : smallIcon}
                    </Grid>
                    <Grid item xs style={{textAlign:'center'}}>
                        <span color={"textPrimary"} className={classes.header}>{title}</span>
                    </Grid>
                </Grid>
            </AccordionSummary>
            <AccordionDetails>
                <Grid className={classes.container}>
                    {children}
                </Grid>
            </AccordionDetails>
        </Accordion>
    );
}

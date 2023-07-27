import Grid from "@mui/material/Grid";
import TextInput from "../../../components/forms/TextInput";
import DeleteIcon from "@mui/icons-material/Delete";
import StartIconButton from "../../../components/buttons/StartIconButton";
import React from "react";
import {IconButton} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import {customFieldManagerFields} from "./settingsFields";
import RenderFields from "./RenderFields";

export default function CustomFieldManager({settings, setSettings, customField, setCustomField}){

    let fields = customFieldManagerFields(settings, setSettings);

    const onTextChange = (index, prop) => (value) => {
        let newCustomField = [...customField];
        newCustomField[index][prop] = value;
        setCustomField(newCustomField)
    }

    const deleteField = (index) => {
        let newCustomField = [...customField];
        newCustomField.splice(index, 1)
        setCustomField(newCustomField)
    }

    const addField = () => {
        let newCustomField = [...customField];
        newCustomField.push({name: ""})
        setCustomField(newCustomField)
    }

    return (
        <Grid container spacing={4} style={{margin : 0,width : "100%",marginBottom : "0.5rem"}} >

            <RenderFields fields={fields} values={settings}/>

            {
                customField.map((cF, i) => <>
                    <Grid item xs={5}>
                        <TextInput label="Nome"
                                   value={cF.name} color={"dark"}
                                   whiteBackground required
                                   onTextChange={onTextChange(i, "name")}
                                   InputLabelProps={{
                                       shrink : true,
                                   }}/>

                    </Grid>
                    <Grid item xs={5}>
                        <TextInput label="Descrizione"
                                   value={cF.description} color={"dark"}
                                   whiteBackground required
                                   onTextChange={onTextChange(i, "description")}
                                   InputLabelProps={{
                                       shrink : true,
                                   }}/>

                    </Grid>
                    <Grid item xs={1}>
                        <IconButton onClick={() => deleteField(i)} color={"primary"} size="large">
                            <DeleteIcon/>
                        </IconButton>
                    </Grid>
                </>)
            }

            {customField.length < 3 &&
                <Grid container justifyContent={"center"}>
                    <StartIconButton onClick={() => addField()} color={"primary"} startIcon={<AddIcon/>}
                                     title={"aggiungi nuovo valore jolly"}/>
                </Grid>
            }

        </Grid>
    );
}
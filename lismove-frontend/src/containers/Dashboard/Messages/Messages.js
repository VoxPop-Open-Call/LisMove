import {Link, useParams} from "react-router-dom";
import { useGetOrganizationMessages} from "../../../services/ContentManager";
import {useSnackbar} from "notistack";
import {useQueryClient} from "react-query";
import Grid from "@mui/material/Grid";
import StartIconButton from "../../../components/buttons/StartIconButton";
import SendIcon from "@mui/icons-material/Send";
import NTMXGrid, { timestampTypeToDateTime} from "../../../components/NTMXGrid";
import React,{useState} from "react";
import {getErrorMessage, MESSAGES, ORGANIZATIONS, post, USERS} from "../../../services/Client";
import SendMessageModal from "../../../components/modals/SendMessageModal";
import {IconButton} from "@mui/material";
import GroupIcon from "@mui/icons-material/Group";
import MessageInfo from "./MessageInfo";

export default function Messages() {

    let {id} = useParams();
    let {messages} = useGetOrganizationMessages(id);
    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();
    let [isSendingMessage, setIsSendingMessage] = useState(false);
    let [showInfo, setShowInfo] = useState(false);

    const defaultColumns = [
        {
            headerName: 'Data di Invio',
            field: 'createdDate',
            width: 180,
            ...timestampTypeToDateTime
        },
        {
            headerName: 'Titolo',
            field: 'title',
            width: 220
        },
        {
            headerName: "Corpo",
            field: "body",
            width: 220
        },
        {
            headerName: "Destinatari",
            field: "",
            width: 130,
            renderCell: (params) =>  <IconButton onClick={() => setShowInfo(params.row)} size="large"><GroupIcon/></IconButton>
        }
    ];

    const sendMessage = (values) => {
        enqueueSnackbar("Saving...", {variant: "info"});
        values.organization=id
        post(`${MESSAGES}/send`, {body: values})
            .then(() =>enqueueSnackbar("Inviato", {variant: "success"}))
            .catch(e => enqueueSnackbar(getErrorMessage(e), {variant: "error"}))
            .finally(() => queryClient.invalidateQueries([ORGANIZATIONS, {id: id}, MESSAGES],));
    }

    console.log(showInfo)
    if(showInfo) return <MessageInfo goBack={() => setShowInfo(null)} messageId={showInfo.id}/>

    return (
        <div>
            <NTMXGrid
                columns={defaultColumns}
                rows={messages || []}
                title="Messaggi"
                getRowId={(row) => messages && row.id}
                rightButton={<Grid container justifyContent={"flex-end"}>
                    <StartIconButton onClick={() => setIsSendingMessage(true)} title="Invia messaggio" startIcon={<SendIcon/>}/>
                </Grid>}
            />
            <SendMessageModal open={!!isSendingMessage} onClose={() => setIsSendingMessage(false)} onSave={sendMessage}/>
        </div>
    );
}

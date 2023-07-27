import BaseModal from "./BaseModal";
import TextInput from "../forms/TextInput";
import React, {useState} from "react";
import Grid from "@mui/material/Grid";
import {Visibility, VisibilityOff} from "@mui/icons-material";
import {firebaseAuth} from "../../firebase";
import {useSnackbar} from "notistack";
import firebase from "firebase";
import {put, USERS} from "../../services/Client";
import {useQueryClient} from "react-query";


export default function ChangeCredentialsModal({open, onClose}) {


    let [newEmail, setNewEmail] = useState("");
    let [currentPassword, setCurrentPassword] = useState("");
    let [newPassword, setNewPassword] = useState("");
    let [repeatPassword, setRepeatPassword] = useState("");

    let [error, setError] = useState({});

    let [showPasswords, setShowPasswords] = useState(false);

    const {enqueueSnackbar} = useSnackbar();
    let queryClient = useQueryClient();

    const save = () => {

        if (currentPassword === "") {
            setError({old: "Campo obbligatorio"});
            return;
        }
        if (newPassword !== repeatPassword) {
            setError({repeat: "Le password non coincidono", new: "Le password non coincidono"});
            return;
        }

        let user = firebaseAuth.currentUser;
        const credential = firebase.auth.EmailAuthProvider.credential(user.email, currentPassword);
        const currentEmail = user.email;
        firebaseAuth.currentUser.reauthenticateWithCredential(credential).then(() => {

            if (newEmail !== currentEmail) {
                firebaseAuth.currentUser.updateEmail(newEmail).then(() => {

                    put(USERS, {body: {email: newEmail}, elem: user.uid})
                        .then(() => enqueueSnackbar("Email Saved", {variant: "success"}))
                        .catch(() => {
                            firebaseAuth.currentUser.updateEmail(currentEmail).then(() => {
                                enqueueSnackbar("Error saving new Email", {variant: "error"});
                            })
                        })
                        .finally(() => queryClient.invalidateQueries(USERS));

                }).catch(() => enqueueSnackbar("Error saving new Email", {variant: "error"}))

            } else {
                enqueueSnackbar("The new email and the current one are the same", {variant: "info"});
            }

            if (newPassword) {
                firebaseAuth.currentUser.updatePassword(newPassword).then(() => {
                    enqueueSnackbar("New Password Saved", {variant: "success"});
                }).catch(() => enqueueSnackbar("Error saving new Password", {variant: "error"}))
            }

        }).catch(() => enqueueSnackbar("Authentication error", {variant: "error"}))

        close();
    }

    const close = () => {
        setNewEmail("");
        setCurrentPassword("");
        setNewPassword("");
        setRepeatPassword("");
        setError({});
        onClose();
    }

    return (
        <BaseModal open={open} onClose={close} onSave={save}
                   iconButton={showPasswords ? <VisibilityOff/> : <Visibility/>}
                   onClickButton={() => setShowPasswords(!showPasswords)}>
            <Grid container spacing={4} style={{margin: 0, width: "100%", marginBottom: "1.5rem"}}>

                <Grid item xs={12}>
                    <TextInput label={"Nuova Email"} value={newEmail}
                               onTextChange={(value) => {
                                   setNewEmail(value.trim());
                                   setError({});
                               }} error={error.email}/>
                </Grid>

                <Grid item xs={12}>
                    <TextInput required label={"Password Attuale"} value={currentPassword}
                               type={showPasswords ? "text" : "password"}
                               onTextChange={(value) => {
                                   setCurrentPassword(value.trim());
                                   setError({});
                               }} error={error.old}/>
                </Grid>

                <Grid item xs={12}>
                    <TextInput label={"Nuova Password"} value={newPassword} type={showPasswords ? "text" : "password"}
                               onTextChange={(value) => {
                                   setNewPassword(value.trim());
                                   setError({});
                               }} error={error.new}/>
                </Grid>

                <Grid item xs={12}>
                    <TextInput label={"Ripeti Password"} value={repeatPassword}
                               type={showPasswords ? "text" : "password"}
                               onTextChange={(value) => {
                                   setRepeatPassword(value.trim());
                                   setError({});
                               }} error={error.repeat}/>
                </Grid>

            </Grid>
        </BaseModal>
    )

}
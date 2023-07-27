import * as React from "react";
import firebase from "firebase/app";
import "firebase/storage";
import {FilePond, registerPlugin} from "react-filepond";
import FilePondPluginImageExifOrientation from "filepond-plugin-image-exif-orientation";
import FilePondPluginImagePreview from "filepond-plugin-image-preview";
import FilePondPluginFileValidateType from 'filepond-plugin-file-validate-type';

// And import the necessary css
import "filepond/dist/filepond.min.css";
import "filepond-plugin-image-preview/dist/filepond-plugin-image-preview.css";
import "./filepond.css";	//remove credits

// register the filepond plugins for additional functionality
registerPlugin(FilePondPluginImageExifOrientation, FilePondPluginImagePreview, FilePondPluginFileValidateType);

// make a reference to our firebase storage
const storage = firebase.storage().ref();

/**
 * carica uno o molteplici file su firebase e li mostra, se eliminati verranno eliminati anche da firebase
 * @param onRequestSave
 * @param folder
 * @param prefix
 * @param acceptedFileTypes
 * @param defaultFiles
 * @param label
 * @param allowMultiple
 * @param maxFiles
 * @param showLoadedImages se settato mostra le immagini caricate nel container, se settato a false ignora la visualizzazione di ogni nuovo file caricato
 * @returns {JSX.Element}
 * @constructor
 */
export default function FileInput({
                                      onRequestSave,
                                      folder = "images",
                                      prefix = "",
                                      acceptedFileTypes = ['image/*'],
                                      defaultFiles = [],
                                      label = "Trascina il nuovo file",
                                      allowMultiple = false,
                                      maxFiles = 1,
                                      showLoadedImages = true
                                  }) {

    const translation = {
        labelIdle: label + ' o <span class="filepond--label-action">Sfoglia</span>',
        labelInvalidField: 'Il file non è valido',
        labelFileWaitingForSize: 'In attesa della dimensione',
        labelFileSizeNotAvailable: 'Dimensione non disponibile',
        labelFileCountSingular: 'file in list',
        labelFileCountPlural: 'files in list',
        labelFileLoading: 'Caricamento',
        labelFileAdded: 'Aggiunto',
        labelFileLoadError: 'Errore durante il caricamento',
        labelFileRemoved: 'Eliminato',
        labelFileRemoveError: 'Errore durante la rimozione',
        labelFileProcessing: 'Caricamento',
        labelFileProcessingComplete: 'Caricamento completato',
        labelFileProcessingAborted: 'Caricamento annullato',
        labelFileProcessingError: 'Erore durante il caricamento',
        labelFileProcessingRevertError: 'Errore durante il ripristino',
        labelTapToCancel: 'Click per annullare',
        labelTapToRetry: 'Clic per riprovare',
        labelTapToUndo: 'Click per annullare',
        labelButtonRemoveItem: 'Elimina',
        labelButtonAbortItemLoad: 'Annulla',
        labelButtonRetryItemLoad: 'Riprova',
        labelButtonAbortItemProcessing: 'Cancella',
        labelButtonUndoItemProcessing: 'Annulla',
        labelButtonRetryItemProcessing: 'Riprova',
        labelButtonProcessItem: 'Carica'
    };

    // use a useState hook to maintain our files collection
    const [files, setFiles] = React.useState(defaultFiles);

    const server = {
        // this uploads the image using firebase
        process: (fieldName, file, metadata, load, error, progress, abort) => {
            // create a unique id for the file
            const path = `${folder}/${prefix}/${new Date().getTime()}`;
            // upload the image to firebase
            const task = storage.child(path).put(file);
            // monitor the task to provide updates to FilePond
            task.on(
                firebase.storage.TaskEvent.STATE_CHANGED,
                snap => {
                    // provide progress updates
                    progress(true, snap.bytesTransferred, snap.totalBytes)
                },
                err => {
                    // provide errors
                    error(err.message)
                },
                () => {
                    // the file has been uploaded
                    load(path);
                    storage.child(path).getDownloadURL().then(url => {
                        if(!showLoadedImages){//elimino la lista di file per non mostrarle nel container
                            setFiles([]);
                        }
                        onRequestSave(url);
                    })
                }
            )
        },
        // this loads an already uploaded image to firebase
        load: (source, load, error, progress, abort) => {
            // reset our progress
            progress(true, 0, 1024);
            // fetch the download URL from firebase
            storage
                .child(source)
                .getDownloadURL()
                .then(url => {
                    // fetch the actual image using the download URL
                    // and provide the blob to FilePond using the load callback
                    let xhr = new XMLHttpRequest();
                    xhr.responseType = 'blob';
                    xhr.onload = function (event) {
                        let blob = xhr.response;
                        load(blob)
                    };
                    xhr.open('GET', url);
                    xhr.send()
                })
                .catch(err => {
                    error(err.message);
                    abort()
                })
        },

        revert: (uniqueFileId, load) => {
            storage.child(uniqueFileId).delete().then(null)
            load()
        }
    };

    return (
        <div>
            <FilePond
                files={files}
                acceptedFileTypes={acceptedFileTypes}
                allowMultiple={allowMultiple}
                maxFiles={maxFiles}
                onupdatefiles={fileItems => {
                    setFiles(fileItems.map(fileItem => fileItem.file))
                }}
                server={server}
                {...translation}
            />
        </div>
    )
}
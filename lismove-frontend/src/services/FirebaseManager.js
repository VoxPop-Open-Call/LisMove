import {firebaseStorage} from "../firebase";

function _deleteFirebaseElement(url) {
    let ref = null;
    try {
        ref = firebaseStorage.refFromURL(url)
    } catch (e) {
        console.log(e, 'ERROR Remove ' + url);
    }
    if (ref) {
        ref.delete()
            .then(() => {
                console.log('removed ' + url);
            })
            .catch(() => {
                console.log('ERROR Remove ' + url);
            });
    }
}

/**
 * elimina tutte le immagini nella lista da firebase
 * @param list vettore di stringa con gli url delle immagini da eliminare
 */
export function removeImagesFirebase(list) {

    if (!list)
        return

    list.forEach(url => {
        _deleteFirebaseElement(url)
    });
}

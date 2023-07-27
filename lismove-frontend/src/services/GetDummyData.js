import {useEffect, useState} from "react";

export function useGetDummyShops(vendorID) {
    // return {shops : []};
    let dummy = [
        {
            id: 1,
            idVendor: 1,
            name: 'Negozio bellissimo',
            website: 'www.the-best-in-tha-forest.com',
            claim: 'il meglio nella foresta',
            description: 'aadsasasdasd',
            logo: 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api',
            images: ['https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api', 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api', 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api', 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api', 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api', 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api'],
            isEcommerce: false,
            city: 'comune bellissimo',
            address: 'address bellisssimo',
            phone: '1231231231',
            categories: [1, 2, 3],
            primary: true,
            latitude: null,
            longitude: null,
        },
        {
            id: 2,
            idVendor: 1,
            name: 'Negozio bellissimissimo woo',
            website: 'www.the-best-in-tha-forest.com',
            claim: 'il meglio nella foresta',
            description: 'aadsasasdasd',
            logo: '',
            images: [],
            isEcommerce: false,
            city: 'city bellissimo',
            address: 'address bellisssimo',
            phone: '1231231231',
            categories: [1],
            primary: false,
            latitude: null,
            longitude: null,
        },
        {
            id: 3,
            idVendor: 1,
            name: 'Negozio bellissimissimo woo',
            website: 'www.the-best-in-tha-forest.com',
            claim: 'il meglio nella foresta',
            description: 'aadsasasdasd',
            logo: '',
            images: [],
            isEcommerce: false,
            city: 'city bellissimo',
            address: 'address bellisssimo',
            phone: '1231231231',
            categories: [],
            primary: false,
            latitude: null,
            longitude: null,
        }
    ];
    return useGetDummy(dummy, 'shops')
}


export default function useGetDummyInsertions(vendorId) {
    let dummy = [
        {
            id: 1,
            idVendor: 1,
            title: 'inserezione 1',
            text: 'una bella inserzione',
            image: 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api',
            link: 'www.url.it',
            cities: [15002],
            shops: [1, 2]
        },
        {
            id: 2,
            idVendor: 1,
            title: 'inserezione 2',
            text: 'una bella inserzione',
            image: 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api',
            link: 'www.url.it',
            cities: [15002],
            shops: []
        },
    ];

    return useGetDummy(dummy, 'insertions');
}

export function useGetDummyInsertion(id) {
    let dummy =
        {
            id: 2,
            idVendor: 1,
            title: 'inserezione 2',
            text: 'una bella inserzione',
            image: 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api',
            link: 'www.url.it',
            cities: [15002],
            shops: []
        };

    return useGetDummy(dummy, 'insertion');
}

export function useGetDummyArticles(shop) {
    let dummy = [
        {
            id: 1,
            idShop: 1,
            title: 'prova',
            description: 'desc',
            point: 123,
            image: 'https://tse3.mm.bing.net/th?id=OIP.d2sdc2OWYeZDkofh4N-AhAHaEK&pid=Api',
            expirationDate: 1630933145000,
            numberArticles: null

        },
        {
            id: 2,
            idShop: 1,
            title: 'prova2',
            description: 'desc2',
            point: 123,
            image: null,
            expirationDate: 1630933145000,
            numberArticles: 12

        }
        ,
        {
            id: 3,
            idShop: 1,
            title: 'piripillo',
            description: 'desc2',
            point: 123,
            image: null,
            expirationDate: 1630933145000,
            numberArticles: 12

        }
    ];

    return useGetDummy(dummy, 'articles');
}

export function useGetDummyVendor(uid) {
    // return {shops : []};
    let dummy =
        {
            id: 1,
            uid: 'cFHVWXpWQ1PPHmYtYqxrhradQKv2',
            businessName: 'ragione',
            seat: 'Sede',
            phone: 'Telefono',
            vatNumber: 'iva',
            iban: 'iban',
            BIC: 'bic',
            visible: true,
            enableCoupon: true
        };
    return useGetDummy(dummy, 'vendor')
}

export function useGetDummyCategories() {
    // return {shops : []};
    let dummy = [
        {
            id: 1,
            name: 'Abbigliamento'
        },
        {
            id: 2,
            name: 'Articoli per la persona'
        },
        {
            id: 3,
            name: ' Articoli Sanitari'
        },
        {
            id: 4,
            name: 'Articoli Sportivi'
        },
    ];
    return useGetDummy(dummy, 'categories')
}

export function useGetDummyCoordinates(address) {
    // return {shops : []};
    let dummy = {
        latitude: 40.9776536,
        longitude: 17.1130185
    };
    return useGetDummy(dummy, 'coordinates')
}

/**
 * hooks that return the dummy after 100 ms
 * @param dummy
 * @param itemName
 */
function useGetDummy(dummy, itemName) {
    let [items, setItems] = useState([]);
    let [status, setStatus] = useState('loading');
    useEffect(() => {
        window.setInterval(() => {
            setStatus('success');
            setItems(dummy);
        }, 3000);
        window.setTimeout(() => {
            setStatus('success');
            setItems(dummy);
        }, 200);
    }, []);

    return {[itemName]: items || [], status: status};
}
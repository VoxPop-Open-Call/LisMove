package it.lismove.app.android.general.network

import it.lismove.app.android.BuildConfig
import it.lismove.app.android.general.BUILD_VARIANT_PROD

object NetworkConfig {
    private const val BASE_URL_TEST = "https://api.lismove-test.nextome-ext.com"
    private const val BASE_URL_PROD = "https://api.lismove.nextome-ext.com"
    private const val BASE_URL_LOCALE = "http://192.168.1.124:8080"

    private const val FIREBASE_RTDB_URL_DEV = "https://tester-nxt-default-rtdb.europe-west1.firebasedatabase.app/"
    private const val FIREBASE_RTDB_URL_PROD = "https://lismove-1521450884928.firebaseio.com/"

//    private const val ZOHO_APP_KEY_DEV = "L02SqpNliaD60bvzqpDAVroS47aQdUNP5BEPKZVVJBQ%3D_eu"
//    private const val ZOHO_ACCESS_KEY_DEV = "BBEo2jGRUMoPVTgOpC1tAoMNEjsHF0mWkoscBXYTEgJRpSTKPG6CzbdOQpIHwHkRpRDwtBfUO2u9e0RwvNHYrnkoBQ63rRtY%2BecLGmaTnVtmzjzt94QP6v028oUad3qttisXkXzElHa4lQS7O%2F%2Fh%2F6ENv1tzLhFL"
    private const val ZOHO_APP_KEY_DEV = "PKD83xyTlimI6riBoU5ov%2FlgwQjtco6XZrKybSKvgNGTWZcM5gZ5NmiaAibZxduk_eu"
    private const val ZOHO_ACCESS_KEY_DEV = "BBEo2jGRUMoPVTgOpC1tAgJ1xqSCK9Tp3Mw3sjPrE1QLFEdSQ%2B%2FDZrxeW9TY45gseaAREqDISJrI5lhJ2PdGAfP7rpDx5JIMkCuq3T56ZsmLUwZgFMxuTnAuF9DFEd0puM%2B5FK0JZZ3D5ZHzb3XFszMs68f2QPKz"
    private const val ZOHO_APP_KEY_PROD = "PKD83xyTlik5AaCejtS6%2BxjC%2BrPmsDzizUWiiYEOOL0%3D_eu"
    private const val ZOHO_ACCESS_KEY_PROD = "BBEo2jGRUMpxmPh0CYwTDC1dgFyi0GnRNLKpMB6MSBQ1dwdworMncxaDWvLPqMX%2FJ9H3KeFlmlEMFRfCjTXQ5wa56CHhWxkUQMvMHc3NzeQ2Iw9TlyttrHqBCEJuezAqBk9HYHcTelBFkuHwrJL4bA%3D%3D"

    val BASE_URL = if (BuildConfig.FLAVOR == BUILD_VARIANT_PROD) BASE_URL_PROD else BASE_URL_TEST
    val FIREBASE_RTDB_URL = if (BuildConfig.FLAVOR == BUILD_VARIANT_PROD) FIREBASE_RTDB_URL_PROD else FIREBASE_RTDB_URL_DEV
    val ZOHO_APP_KEY = if (BuildConfig.FLAVOR == BUILD_VARIANT_PROD) ZOHO_APP_KEY_PROD else ZOHO_APP_KEY_DEV
    val ZOHO_ACCESS_KEY = if (BuildConfig.FLAVOR == BUILD_VARIANT_PROD) ZOHO_ACCESS_KEY_PROD else ZOHO_ACCESS_KEY_DEV
}
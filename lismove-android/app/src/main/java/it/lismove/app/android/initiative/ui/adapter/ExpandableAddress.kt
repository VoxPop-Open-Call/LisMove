package it.lismove.app.android.initiative.ui.adapter

import it.lismove.app.android.initiative.ui.data.WorkAddress
data class ExpandableAddress(
    var address: WorkAddress = WorkAddress(),
    var showMapsButton: Boolean,
    var showName: Boolean = false,
    var isInitiallyOpen: Boolean = false,
){
    var completeName: String = ""
        get() {
            val addressString = "${address.address ?: ""} ${address.number ?: ""}, ${address.cityExtended?.getFullName() ?: ""}"
            if(address.address.isNullOrEmpty()){
                return ""
            }else if(showName){
                return "${address.name ?: ""} \n$addressString"
            }else{
                return "$addressString"

            }
        }
}

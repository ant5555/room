package com.example.room.ui

import com.example.room.data.Contact
import com.example.room.ui.SortType

data class ContactState(
    val contacts: List<Contact> = emptyList(),
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isAddingContact: Boolean = false,
    val sortType: SortType = SortType.FIRST_NAME,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val phoneNumberError: String? = null,
    val error: String? = null,
)

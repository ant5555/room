package com.example.room.ui

import com.example.room.data.Contact
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.room.ui.SortType
import com.example.room.data.ContactDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel(
    private val dao: ContactDao
): ViewModel() {

    private val _sortType = MutableStateFlow(SortType.FIRST_NAME)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _contacts = _sortType
        .flatMapLatest { sortType ->
            when(sortType) {
                SortType.FIRST_NAME -> dao.getContactsOrderedByFirstName()
                SortType.LAST_NAME -> dao.getContactsOrderedByLastName()
                SortType.PHONE_NUMBER -> dao.getContactsOrderedByPhoneNumber()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(ContactState())
    val state = combine(_state, _sortType, _contacts) { state, sortType, contacts ->
        state.copy(
            contacts = contacts,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), ContactState())

    fun onEvent(event: ContactEvent) {
        when(event) {
            is ContactEvent.DeleteContact -> {
                viewModelScope.launch {
                    dao.deleteContact(event.contact)
                }
            }
            ContactEvent.HideDialog -> {
                _state.update {
                    it.copy(
                        isAddingContact = false
                    )
                }
            }
            ContactEvent.SaveContact -> {
                val firstName = state.value.firstName.trim()
                val lastName = state.value.lastName.trim()
                val phoneNumber = state.value.phoneNumber.trim()

                val firstNameError = if (firstName.isBlank()) "이름을 입력해주세요" else null
                val lastNameError = if (lastName.isBlank()) "성을 입력해주세요" else null
                val phoneNumberError = when {
                    phoneNumber.isBlank() -> "전화번호를 입력해주세요"
                    !phoneNumber.all { it.isDigit() || it == '-' } -> "올바른 전화번호를 입력해주세요"
                    else -> null
                }

                if (firstNameError != null || lastNameError != null || phoneNumberError != null) {
                    _state.update {
                        it.copy(
                            firstNameError = firstNameError,
                            lastNameError = lastNameError,
                            phoneNumberError = phoneNumberError
                        )
                    }
                    return
                }


                val contact = Contact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber
                )

                viewModelScope.launch {
                    dao.insertContact(contact)
                }

                _state.update {
                    it.copy(
                        isAddingContact = false,
                        firstName = "",
                        lastName = "",
                        phoneNumber = "",
                        firstNameError = null,
                        lastNameError = null,
                        phoneNumberError = null
                    )
                }
            }
            is ContactEvent.SetFirstName -> {
                _state.update {
                    it.copy(firstName = event.firstName, firstNameError = null)
                }
            }
            is ContactEvent.SetLastName -> {
                _state.update {
                    it.copy(lastName = event.lastName, lastNameError = null)
                }
            }
            is ContactEvent.SetPhoneNumber -> {
                _state.update {
                    it.copy(phoneNumber = event.phoneNumber, phoneNumberError = null)
                }
            }
            ContactEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingContact = true
                    )
                }
            }
            is ContactEvent.SortContacts -> {
                _sortType.value = event.sortType
            }
        }
    }
}
package com.example.room.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DeleteConfirmDialog(
    state: ContactState,
    onEvent: (ContactEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val contact = state.contactToDelete ?: return
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onEvent(ContactEvent.CancelDelete) },
        title = { Text("연락처 삭제") },
        text = { Text("${contact.firstName} ${contact.lastName}을(를) 삭제하시겠습니까?") },
        confirmButton = {
            Button(onClick = { onEvent(ContactEvent.ConfirmDelete(contact)) }) {
                Text("삭제")
            }
        },
        dismissButton = {
            Button(onClick = { onEvent(ContactEvent.CancelDelete) }) {
                Text("취소")
            }
        }
    )
}
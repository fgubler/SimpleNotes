package ch.abwesend.simplenotes.view

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.simplenotes.R
import ch.abwesend.simplenotes.repository.NoteRepository
import ch.abwesend.simplenotes.ui.theme.noteBackground
import ch.abwesend.simplenotes.view.components.LoadingIndicatorFullScreen
import kotlinx.coroutines.flow.first

@ExperimentalMaterial3Api
object MainScreen {
    private val lineSeparator: String by lazy { System.lineSeparator() }

    /**
     * The value is only loaded initially from the Data-Store.
     * The intermediate state is kept here directly for performance-reasons.
     */
    @Composable
    fun Screen() {
        val context = LocalContext.current
        val placeHolder = stringResource(id = R.string.notes_text_placeholder)

        var initialNotesText: String? by remember { mutableStateOf(null) }
        var currentNotesText by remember { mutableStateOf(placeHolder) }
        var initialized by remember { mutableStateOf(false) }

        val onValueChanged: (String) -> Unit = { newValue ->
            currentNotesText = newValue
            NoteRepository.storeNotes(context, newValue)
        }

        LaunchedEffect(Unit) {
            currentNotesText = NoteRepository.getNotes(context).first()

            if (!currentNotesText.startsWith(lineSeparator)) {
                currentNotesText = lineSeparator + currentNotesText
            }

            initialNotesText = currentNotesText
            initialized = true
        }

        Scaffold(topBar = { TopBar(currentNotesText, initialNotesText, onValueChanged) }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (initialized) {
                    Notes(currentNotesText, onValueChanged)
                } else {
                    LoadingIndicatorFullScreen(textAfterIndicator = R.string.loading_notes)
                }
            }
        }
    }

    @Composable
    private fun Notes(currentValue: String, valueChanged: (String) -> Unit) {
        BasicTextField(
            value = currentValue,
            onValueChange = valueChanged,
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            modifier = Modifier
                .fillMaxSize()
                .background(noteBackground)
                .padding(10.dp)
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
        )
    }
}
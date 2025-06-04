package com.rix.womblab.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rix.womblab.R
import com.rix.womblab.presentation.auth.register.UserProfile
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var workplace by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var wantsNewsletter by remember { mutableStateOf(true) }
    var wantsNotifications by remember { mutableStateOf(true) }

    var showProfessionDropdown by remember { mutableStateOf(false) }
    var showSpecializationDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userProfile) {
        uiState.userProfile?.let { profile ->
            firstName = profile.firstName
            lastName = profile.lastName
            profession = profile.profession
            specialization = profile.specialization ?: ""
            workplace = profile.workplace ?: ""
            city = profile.city ?: ""
            phone = profile.phone ?: ""
            wantsNewsletter = profile.wantsNewsletter
            wantsNotifications = profile.wantsNotifications
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            onProfileUpdated()
            viewModel.clearUpdateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.edit_profile_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val updatedProfile = UserProfile(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                profession = profession,
                                specialization = specialization.trim().takeIf { it.isNotEmpty() },
                                workplace = workplace.trim().takeIf { it.isNotEmpty() },
                                city = city.trim().takeIf { it.isNotEmpty() },
                                phone = phone.trim().takeIf { it.isNotEmpty() },
                                wantsNewsletter = wantsNewsletter,
                                wantsNotifications = wantsNotifications
                            )
                            viewModel.updateProfile(updatedProfile)
                        },
                        enabled = !uiState.isLoading && firstName.isNotBlank() && lastName.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = stringResource(id = R.string.personal_info)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(stringResource(id = R.string.first_name)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(stringResource(id = R.string.last_name)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(id = R.string.phone_optional)) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(id = R.string.city_optional)) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true
                )
            }

            SectionCard(title = stringResource(id = R.string.professional_info)) {
                ExposedDropdownMenuBox(
                    expanded = showProfessionDropdown,
                    onExpandedChange = { showProfessionDropdown = it }
                ) {
                    OutlinedTextField(
                        value = profession,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.profession)) },
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProfessionDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = showProfessionDropdown,
                        onDismissRequest = { showProfessionDropdown = false }
                    ) {
                        Constants.MEDICAL_PROFESSIONS.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text(prof) },
                                onClick = {
                                    profession = prof
                                    showProfessionDropdown = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = showSpecializationDropdown,
                    onExpandedChange = { showSpecializationDropdown = it }
                ) {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.specialization_optional)) },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSpecializationDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = showSpecializationDropdown,
                        onDismissRequest = { showSpecializationDropdown = false }
                    ) {
                        Constants.MEDICAL_SPECIALIZATIONS.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec) },
                                onClick = {
                                    specialization = spec
                                    showSpecializationDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = workplace,
                    onValueChange = { workplace = it },
                    label = { Text(stringResource(id = R.string.workplace_optional)) },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true
                )
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    WombLabTheme {
        EditProfileScreen(
            onNavigateBack = { },
            onProfileUpdated = { }
        )
    }
}
package com.rix.womblab.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Handle registration success
    LaunchedEffect(uiState.isRegistrationComplete) {
        if (uiState.isRegistrationComplete) {
            onRegistrationSuccess()
        }
    }

    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar here
            // For now, we'll just print the error
            println("Registration Error: $error")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completa Registrazione") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF006B5B),
                            Color(0xFF004D42)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Welcome Section
                WelcomeSection(userName = uiState.currentUser?.displayName ?: "")

                Spacer(modifier = Modifier.height(32.dp))

                // Registration Form
                RegistrationForm(
                    uiState = uiState,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onProfessionChange = viewModel::onProfessionChange,
                    onSpecializationChange = viewModel::onSpecializationChange,
                    onWorkplaceChange = viewModel::onWorkplaceChange,
                    onCityChange = viewModel::onCityChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onNewsletterChange = viewModel::onNewsletterChange,
                    onNotificationsChange = viewModel::onNotificationsChange,
                    onCompleteRegistration = viewModel::completeRegistration,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Terms and Privacy
                TermsAndPrivacy()
            }
        }
    }
}

@Composable
private fun WelcomeSection(userName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Text(
                text = "🔬",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Benvenuto in WombLab${if (userName.isNotEmpty()) ", $userName" else ""}!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006B5B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Completa il tuo profilo per personalizzare la tua esperienza e ricevere eventi su misura per te.",
                fontSize = 14.sp,
                color = Color(0xFF004D42),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun RegistrationForm(
    uiState: RegisterUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onProfessionChange: (String) -> Unit,
    onSpecializationChange: (String) -> Unit,
    onWorkplaceChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNewsletterChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onCompleteRegistration: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Personal Information Section
            SectionTitle("Informazioni Personali")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("Nome *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF006B5B),
                        focusedLabelColor = Color(0xFF006B5B)
                    )
                )

                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Cognome *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF006B5B),
                        focusedLabelColor = Color(0xFF006B5B)
                    )
                )
            }

            // Professional Information Section
            SectionTitle("Informazioni Professionali")

            ProfessionDropdown(
                selectedProfession = uiState.profession,
                onProfessionSelected = onProfessionChange
            )

            OutlinedTextField(
                value = uiState.specialization,
                onValueChange = onSpecializationChange,
                label = { Text("Specializzazione") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF006B5B),
                    focusedLabelColor = Color(0xFF006B5B)
                )
            )

            OutlinedTextField(
                value = uiState.workplace,
                onValueChange = onWorkplaceChange,
                label = { Text("Luogo di Lavoro") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF006B5B),
                    focusedLabelColor = Color(0xFF006B5B)
                )
            )

            // Contact Information Section
            SectionTitle("Informazioni di Contatto")

            OutlinedTextField(
                value = uiState.city,
                onValueChange = onCityChange,
                label = { Text("Città") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF006B5B),
                    focusedLabelColor = Color(0xFF006B5B)
                )
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Telefono") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF006B5B),
                    focusedLabelColor = Color(0xFF006B5B)
                )
            )

            // Preferences Section
            SectionTitle("Preferenze")

            // Newsletter Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.wantsNewsletter,
                    onCheckedChange = onNewsletterChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF006B5B)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Desidero ricevere la newsletter con gli ultimi eventi e aggiornamenti",
                    fontSize = 14.sp,
                    color = Color(0xFF004D42)
                )
            }

            // Notifications Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.wantsNotifications,
                    onCheckedChange = onNotificationsChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF006B5B)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Desidero ricevere notifiche sui miei eventi preferiti",
                    fontSize = 14.sp,
                    color = Color(0xFF004D42)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Complete Registration Button
            Button(
                onClick = onCompleteRegistration,
                enabled = !isLoading && uiState.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006B5B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Completa Registrazione",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF006B5B),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfessionDropdown(
    selectedProfession: String,
    onProfessionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val professions = Constants.MEDICAL_PROFESSIONS

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedProfession,
            onValueChange = { },
            readOnly = true,
            label = { Text("Professione *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF006B5B),
                focusedLabelColor = Color(0xFF006B5B)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            professions.forEach { profession ->
                DropdownMenuItem(
                    text = { Text(profession) },
                    onClick = {
                        onProfessionSelected(profession)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TermsAndPrivacy() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Completando la registrazione accetti i nostri",
                fontSize = 12.sp,
                color = Color(0xFF004D42),
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { /* TODO: Open Terms */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Termini di Servizio",
                        fontSize = 12.sp,
                        color = Color(0xFF006B5B)
                    )
                }

                Text(
                    text = "e la",
                    fontSize = 12.sp,
                    color = Color(0xFF004D42)
                )

                TextButton(
                    onClick = { /* TODO: Open Privacy Policy */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 12.sp,
                        color = Color(0xFF006B5B)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    WombLabTheme {
        RegisterScreen(
            onNavigateBack = { },
            onRegistrationSuccess = { }
        )
    }
}
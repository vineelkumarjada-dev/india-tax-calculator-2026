package com.taxcalc.india.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxcalc.india.ui.components.*
import com.taxcalc.india.ui.theme.*
import com.taxcalc.india.viewmodel.TaxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxCalculatorScreen(viewModel: TaxViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "India Tax Calculator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FY 2026-27 (AY 2027-28)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.resetForm() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset",
                            tint = OnPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === SALARY SECTION ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "💰 Salary Details",
                        subtitle = "Enter your annual salary components"
                    )

                    CurrencyInputField(
                        label = "Gross Annual Salary *",
                        value = uiState.grossSalary,
                        onValueChange = viewModel::updateGrossSalary,
                        hint = "e.g., 1200000",
                        errorMessage = uiState.errors["grossSalary"]
                    )
                }
            }

            // === HRA SECTION ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "🏠 HRA Details",
                        subtitle = "For Old Regime HRA exemption calculation"
                    )

                    CurrencyInputField(
                        label = "HRA Received (Annual)",
                        value = uiState.hraReceived,
                        onValueChange = viewModel::updateHraReceived,
                        hint = "e.g., 240000"
                    )

                    CurrencyInputField(
                        label = "Actual Rent Paid (Annual)",
                        value = uiState.actualRentPaid,
                        onValueChange = viewModel::updateActualRentPaid,
                        hint = "e.g., 180000"
                    )

                    CityTypeSelector(
                        selectedCityType = uiState.cityType,
                        onCityTypeSelected = viewModel::updateCityType
                    )
                }
            }

            // === DEDUCTIONS SECTION (Old Regime) ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "📋 Deductions (Old Regime)",
                        subtitle = "These deductions apply only under the Old Tax Regime"
                    )

                    CurrencyInputField(
                        label = "Section 80C (Max ₹1,50,000)",
                        value = uiState.section80C,
                        onValueChange = viewModel::updateSection80C,
                        hint = "PPF, ELSS, LIC, etc."
                    )

                    CurrencyInputField(
                        label = "Section 80D - Health Insurance",
                        value = uiState.section80D,
                        onValueChange = viewModel::updateSection80D,
                        hint = "Medical insurance premium"
                    )

                    CurrencyInputField(
                        label = "Other Deductions",
                        value = uiState.otherDeductions,
                        onValueChange = viewModel::updateOtherDeductions,
                        hint = "NPS (80CCD), Education Loan (80E), etc."
                    )
                }
            }

            // === CALCULATE BUTTON ===
            Button(
                onClick = { viewModel.calculateTax() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Calculate Tax",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // === RESULTS ===
            AnimatedVisibility(
                visible = uiState.showResult && uiState.result != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut()
            ) {
                uiState.result?.let { result ->
                    TaxResultCard(result = result)
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
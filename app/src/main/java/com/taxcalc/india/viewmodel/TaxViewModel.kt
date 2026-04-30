package com.taxcalc.india.viewmodel

import androidx.lifecycle.ViewModel
import com.taxcalc.india.logic.TaxCalculator
import com.taxcalc.india.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TaxUiState(
    val grossSalary: String = "",
    val hraReceived: String = "",
    val actualRentPaid: String = "",
    val cityType: CityType = CityType.METRO,
    val section80C: String = "",
    val section80D: String = "",
    val otherDeductions: String = "",
    val result: TaxResult? = null,
    val showResult: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

class TaxViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TaxUiState())
    val uiState: StateFlow<TaxUiState> = _uiState.asStateFlow()

    fun updateGrossSalary(value: String) {
        _uiState.update { it.copy(grossSalary = value, showResult = false) }
    }

    fun updateHraReceived(value: String) {
        _uiState.update { it.copy(hraReceived = value, showResult = false) }
    }

    fun updateActualRentPaid(value: String) {
        _uiState.update { it.copy(actualRentPaid = value, showResult = false) }
    }

    fun updateCityType(cityType: CityType) {
        _uiState.update { it.copy(cityType = cityType, showResult = false) }
    }

    fun updateSection80C(value: String) {
        _uiState.update { it.copy(section80C = value, showResult = false) }
    }

    fun updateSection80D(value: String) {
        _uiState.update { it.copy(section80D = value, showResult = false) }
    }

    fun updateOtherDeductions(value: String) {
        _uiState.update { it.copy(otherDeductions = value, showResult = false) }
    }

    fun calculateTax() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        val gross = state.grossSalary.toDoubleOrNull()
        if (gross == null || gross < 0) {
            errors["grossSalary"] = "Enter a valid salary amount"
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        val input = TaxInput(
            grossAnnualSalary = gross ?: 0.0,
            hraReceived = state.hraReceived.toDoubleOrNull() ?: 0.0,
            actualRentPaid = state.actualRentPaid.toDoubleOrNull() ?: 0.0,
            cityType = state.cityType,
            section80C = state.section80C.toDoubleOrNull() ?: 0.0,
            section80D = state.section80D.toDoubleOrNull() ?: 0.0,
            otherDeductions = state.otherDeductions.toDoubleOrNull() ?: 0.0
        )

        val result = TaxCalculator.calculate(input)
        _uiState.update { it.copy(result = result, showResult = true, errors = emptyMap()) }
    }

    fun resetForm() {
        _uiState.value = TaxUiState()
    }
}
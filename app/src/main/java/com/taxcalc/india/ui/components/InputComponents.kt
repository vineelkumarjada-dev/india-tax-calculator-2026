package com.taxcalc.india.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.taxcalc.india.model.CityType
import com.taxcalc.india.ui.theme.*

@Composable
fun CurrencyInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    onValueChange(newValue)
                }
            },
            label = { Text(label) },
            placeholder = if (hint.isNotEmpty()) {{ Text(hint, color = OnSurfaceLight) }} else null,
            prefix = { Text("₹ ", style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = errorMessage != null,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = CardBorder
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun CityTypeSelector(
    selectedCityType: CityType,
    onCityTypeSelected: (CityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "City Type",
            style = MaterialTheme.typography.labelLarge,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CityType.entries.forEach { type ->
                val isSelected = type == selectedCityType
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCityTypeSelected(type) }
                        .then(
                            if (isSelected) Modifier.border(
                                2.dp, Primary, RoundedCornerShape(12.dp)
                            ) else Modifier.border(
                                1.dp, CardBorder, RoundedCornerShape(12.dp)
                            )
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Primary.copy(alpha = 0.08f) else Surface,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onCityTypeSelected(type) },
                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                        )
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) Primary else OnSurface
                        )
                        Text(
                            text = if (type == CityType.METRO)
                                "Mumbai, Delhi, etc." else "Other cities",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Primary
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceLight
            )
        }
    }
}
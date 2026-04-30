package com.taxcalc.india

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taxcalc.india.ui.screens.TaxCalculatorScreen
import com.taxcalc.india.ui.theme.IndiaTaxCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IndiaTaxCalcTheme {
                TaxCalculatorScreen()
            }
        }
    }
}
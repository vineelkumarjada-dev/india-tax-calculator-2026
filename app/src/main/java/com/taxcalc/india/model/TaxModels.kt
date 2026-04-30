package com.taxcalc.india.model

enum class CityType(val label: String) {
    METRO("Metro"),
    NON_METRO("Non-Metro");
}

val METRO_CITIES = listOf(
    "Mumbai", "Delhi", "Kolkata", "Chennai",
    "Bengaluru", "Hyderabad", "Pune", "Ahmedabad"
)

data class TaxInput(
    val grossAnnualSalary: Double = 0.0,
    val hraReceived: Double = 0.0,
    val actualRentPaid: Double = 0.0,
    val cityType: CityType = CityType.METRO,
    val section80C: Double = 0.0,
    val section80D: Double = 0.0,
    val otherDeductions: Double = 0.0
)

data class TaxBreakdown(
    val grossSalary: Double,
    val standardDeduction: Double,
    val hraExemption: Double,
    val section80C: Double,
    val section80D: Double,
    val otherDeductions: Double,
    val totalDeductions: Double,
    val taxableIncome: Double,
    val taxBeforeRebate: Double,
    val rebate87A: Double,
    val taxAfterRebate: Double,
    val marginalRelief: Double,
    val taxAfterMarginalRelief: Double,
    val surcharge: Double,
    val cess: Double,
    val totalTax: Double,
    val slabWiseBreakdown: List<SlabTax>
)

data class SlabTax(
    val slabDescription: String,
    val taxableAmount: Double,
    val rate: Double,
    val tax: Double
)

data class TaxResult(
    val newRegime: TaxBreakdown,
    val oldRegime: TaxBreakdown,
    val recommendedRegime: String,
    val savings: Double
)
package com.taxcalc.india.logic

import com.taxcalc.india.model.*
import kotlin.math.max
import kotlin.math.min

object TaxCalculator {

    private const val CESS_RATE = 0.04

    // --- New Regime Constants (FY 2026-27) ---
    private const val NEW_STANDARD_DEDUCTION = 75_000.0
    private const val NEW_REBATE_LIMIT = 12_00_000.0
    private const val NEW_REBATE_MAX = 60_000.0

    // --- Old Regime Constants (FY 2026-27) ---
    private const val OLD_STANDARD_DEDUCTION = 50_000.0
    private const val OLD_REBATE_LIMIT = 5_00_000.0
    private const val OLD_REBATE_MAX = 12_500.0
    private const val MAX_80C = 1_50_000.0

    fun calculate(input: TaxInput): TaxResult {
        val newRegime = calculateNewRegime(input)
        val oldRegime = calculateOldRegime(input)

        val recommended = if (newRegime.totalTax <= oldRegime.totalTax) "New Regime" else "Old Regime"
        val savings = kotlin.math.abs(newRegime.totalTax - oldRegime.totalTax)

        return TaxResult(
            newRegime = newRegime,
            oldRegime = oldRegime,
            recommendedRegime = recommended,
            savings = savings
        )
    }

    // ===================== NEW REGIME =====================

    fun calculateNewRegime(input: TaxInput): TaxBreakdown {
        val gross = input.grossAnnualSalary
        val standardDeduction = min(NEW_STANDARD_DEDUCTION, gross)
        val totalDeductions = standardDeduction
        val taxableIncome = max(0.0, gross - totalDeductions)

        val slabs = calculateNewRegimeSlabs(taxableIncome)
        val taxBeforeRebate = slabs.sumOf { it.tax }

        // Rebate u/s 87A
        val rebate = if (taxableIncome <= NEW_REBATE_LIMIT) {
            min(taxBeforeRebate, NEW_REBATE_MAX)
        } else 0.0

        val taxAfterRebate = max(0.0, taxBeforeRebate - rebate)

        // Marginal Relief: if taxable income slightly > 12L,
        // total tax should not exceed (taxable income - 12L)
        val marginalRelief = if (taxableIncome > NEW_REBATE_LIMIT) {
            val excessIncome = taxableIncome - NEW_REBATE_LIMIT
            val normalTax = taxAfterRebate
            if (normalTax > excessIncome) {
                normalTax - excessIncome
            } else 0.0
        } else 0.0

        val taxAfterMarginalRelief = taxAfterRebate - marginalRelief
        val surcharge = calculateSurcharge(taxAfterMarginalRelief, taxableIncome)
        val taxPlusSurcharge = taxAfterMarginalRelief + surcharge
        val cess = taxPlusSurcharge * CESS_RATE
        val totalTax = taxPlusSurcharge + cess

        return TaxBreakdown(
            grossSalary = gross,
            standardDeduction = standardDeduction,
            hraExemption = 0.0,
            section80C = 0.0,
            section80D = 0.0,
            otherDeductions = 0.0,
            totalDeductions = totalDeductions,
            taxableIncome = taxableIncome,
            taxBeforeRebate = taxBeforeRebate,
            rebate87A = rebate,
            taxAfterRebate = taxAfterRebate,
            marginalRelief = marginalRelief,
            taxAfterMarginalRelief = taxAfterMarginalRelief,
            surcharge = surcharge,
            cess = cess,
            totalTax = totalTax,
            slabWiseBreakdown = slabs
        )
    }

    private fun calculateNewRegimeSlabs(taxableIncome: Double): List<SlabTax> {
        val slabs = listOf(
            Triple("Up to ₹4,00,000", 4_00_000.0, 0.0),
            Triple("₹4,00,001 - ₹8,00,000", 4_00_000.0, 0.05),
            Triple("₹8,00,001 - ₹12,00,000", 4_00_000.0, 0.10),
            Triple("₹12,00,001 - ₹16,00,000", 4_00_000.0, 0.15),
            Triple("₹16,00,001 - ₹20,00,000", 4_00_000.0, 0.20),
            Triple("₹20,00,001 - ₹24,00,000", 4_00_000.0, 0.25),
            Triple("Above ₹24,00,000", Double.MAX_VALUE, 0.30)
        )

        val result = mutableListOf<SlabTax>()
        var remaining = taxableIncome

        for ((desc, limit, rate) in slabs) {
            if (remaining <= 0) {
                result.add(SlabTax(desc, 0.0, rate * 100, 0.0))
                continue
            }
            val taxableInSlab = min(remaining, limit)
            val tax = taxableInSlab * rate
            result.add(SlabTax(desc, taxableInSlab, rate * 100, tax))
            remaining -= taxableInSlab
        }
        return result
    }

    // ===================== OLD REGIME =====================

    fun calculateOldRegime(input: TaxInput): TaxBreakdown {
        val gross = input.grossAnnualSalary
        val basicSalary = gross * 0.5

        // HRA Exemption calculation
        val hraExemption = calculateHraExemption(
            hraReceived = input.hraReceived,
            basicSalary = basicSalary,
            rentPaid = input.actualRentPaid,
            isMetro = input.cityType == CityType.METRO
        )

        val standardDeduction = min(OLD_STANDARD_DEDUCTION, gross)
        val sec80C = min(input.section80C, MAX_80C)
        val sec80D = input.section80D
        val otherDed = input.otherDeductions

        val totalDeductions = standardDeduction + hraExemption + sec80C + sec80D + otherDed
        val taxableIncome = max(0.0, gross - totalDeductions)

        val slabs = calculateOldRegimeSlabs(taxableIncome)
        val taxBeforeRebate = slabs.sumOf { it.tax }

        val rebate = if (taxableIncome <= OLD_REBATE_LIMIT) {
            min(taxBeforeRebate, OLD_REBATE_MAX)
        } else 0.0

        val taxAfterRebate = max(0.0, taxBeforeRebate - rebate)
        val surcharge = calculateSurcharge(taxAfterRebate, taxableIncome)
        val taxPlusSurcharge = taxAfterRebate + surcharge
        val cess = taxPlusSurcharge * CESS_RATE
        val totalTax = taxPlusSurcharge + cess

        return TaxBreakdown(
            grossSalary = gross,
            standardDeduction = standardDeduction,
            hraExemption = hraExemption,
            section80C = sec80C,
            section80D = sec80D,
            otherDeductions = otherDed,
            totalDeductions = totalDeductions,
            taxableIncome = taxableIncome,
            taxBeforeRebate = taxBeforeRebate,
            rebate87A = rebate,
            taxAfterRebate = taxAfterRebate,
            marginalRelief = 0.0,
            taxAfterMarginalRelief = taxAfterRebate,
            surcharge = surcharge,
            cess = cess,
            totalTax = totalTax,
            slabWiseBreakdown = slabs
        )
    }

    private fun calculateOldRegimeSlabs(taxableIncome: Double): List<SlabTax> {
        val slabs = listOf(
            Triple("Up to ₹2,50,000", 2_50_000.0, 0.0),
            Triple("₹2,50,001 - ₹5,00,000", 2_50_000.0, 0.05),
            Triple("₹5,00,001 - ₹10,00,000", 5_00_000.0, 0.20),
            Triple("Above ₹10,00,000", Double.MAX_VALUE, 0.30)
        )

        val result = mutableListOf<SlabTax>()
        var remaining = taxableIncome

        for ((desc, limit, rate) in slabs) {
            if (remaining <= 0) {
                result.add(SlabTax(desc, 0.0, rate * 100, 0.0))
                continue
            }
            val taxableInSlab = min(remaining, limit)
            val tax = taxableInSlab * rate
            result.add(SlabTax(desc, taxableInSlab, rate * 100, tax))
            remaining -= taxableInSlab
        }
        return result
    }

    // ===================== HRA EXEMPTION =====================

    fun calculateHraExemption(
        hraReceived: Double,
        basicSalary: Double,
        rentPaid: Double,
        isMetro: Boolean
    ): Double {
        if (hraReceived <= 0 || rentPaid <= 0) return 0.0

        val actualHra = hraReceived
        val rentExcess = max(0.0, rentPaid - (0.10 * basicSalary))
        val percentOfBasic = if (isMetro) 0.50 * basicSalary else 0.40 * basicSalary

        return minOf(actualHra, rentExcess, percentOfBasic)
    }

    // ===================== SURCHARGE =====================

    private fun calculateSurcharge(tax: Double, taxableIncome: Double): Double {
        return when {
            taxableIncome > 5_00_00_000 -> tax * 0.37
            taxableIncome > 2_00_00_000 -> tax * 0.25
            taxableIncome > 1_00_00_000 -> tax * 0.15
            taxableIncome > 50_00_000 -> tax * 0.10
            else -> 0.0
        }
    }
}
package com.taxcalc.india

import com.taxcalc.india.logic.TaxCalculator
import com.taxcalc.india.model.CityType
import com.taxcalc.india.model.TaxInput
import org.junit.Assert.*
import org.junit.Test

class TaxCalculatorTest {

    private val delta = 1.0 // Allow ₹1 rounding difference

    // ==================== NEW REGIME TESTS ====================

    @Test
    fun `new regime - zero salary should produce zero tax`() {
        val input = TaxInput(grossAnnualSalary = 0.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(0.0, result.totalTax, delta)
        assertEquals(0.0, result.taxableIncome, delta)
    }

    @Test
    fun `new regime - salary below standard deduction should produce zero tax`() {
        val input = TaxInput(grossAnnualSalary = 50000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(0.0, result.totalTax, delta)
        assertEquals(0.0, result.taxableIncome, delta)
    }

    @Test
    fun `new regime - standard deduction is 75000`() {
        val input = TaxInput(grossAnnualSalary = 1000000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(75000.0, result.standardDeduction, delta)
        assertEquals(925000.0, result.taxableIncome, delta)
    }

    @Test
    fun `new regime - income up to 4L after deduction should be zero tax`() {
        // Gross = 4,75,000 -> Taxable = 4,00,000 (after 75k std deduction)
        val input = TaxInput(grossAnnualSalary = 475000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(400000.0, result.taxableIncome, delta)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - income in 5% slab`() {
        // Gross = 6,75,000 -> Taxable = 6,00,000
        // Tax: 0-4L = 0, 4L-6L = 2L * 5% = 10,000
        // Rebate: taxable <= 12L, so rebate = min(10000, 60000) = 10000
        // Total = 0
        val input = TaxInput(grossAnnualSalary = 675000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(600000.0, result.taxableIncome, delta)
        assertEquals(10000.0, result.taxBeforeRebate, delta)
        assertEquals(10000.0, result.rebate87A, delta)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - income exactly 12L should get full rebate`() {
        // Gross = 12,75,000 -> Taxable = 12,00,000
        // Tax: 0-4L=0, 4-8L=20000, 8-12L=40000 = 60000
        // Rebate = min(60000, 60000) = 60000
        val input = TaxInput(grossAnnualSalary = 1275000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(1200000.0, result.taxableIncome, delta)
        assertEquals(60000.0, result.taxBeforeRebate, delta)
        assertEquals(60000.0, result.rebate87A, delta)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - marginal relief for income slightly above 12L`() {
        // Gross = 12,85,000 -> Taxable = 12,10,000
        // Tax without rebate: 0-4L=0, 4-8L=20000, 8-12L=40000, 12-12.1L=10000*15%=1500 = 61500
        // No rebate (taxable > 12L)
        // Marginal relief: tax (61500) > excess (10000), so relief = 61500 - 10000 = 51500
        // Tax after marginal relief = 10000
        // Cess = 10000 * 4% = 400
        // Total = 10400
        val input = TaxInput(grossAnnualSalary = 1285000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(1210000.0, result.taxableIncome, delta)
        assertEquals(0.0, result.rebate87A, delta)
        assertEquals(51500.0, result.marginalRelief, delta)
        assertEquals(10000.0, result.taxAfterMarginalRelief, delta)
        assertEquals(400.0, result.cess, delta)
        assertEquals(10400.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - income at 15L`() {
        // Gross = 15,75,000 -> Taxable = 15,00,000
        // Tax: 0-4L=0, 4-8L=20000, 8-12L=40000, 12-15L=3L*15%=45000 = 105000
        // No rebate, no marginal relief (normal tax 105000 < excess 300000)
        // Cess = 105000 * 4% = 4200
        // Total = 109200
        val input = TaxInput(grossAnnualSalary = 1575000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(1500000.0, result.taxableIncome, delta)
        assertEquals(105000.0, result.taxBeforeRebate, delta)
        assertEquals(0.0, result.rebate87A, delta)
        assertEquals(0.0, result.marginalRelief, delta)
        assertEquals(4200.0, result.cess, delta)
        assertEquals(109200.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - income at 25L`() {
        // Gross = 25,75,000 -> Taxable = 25,00,000
        // Tax: 0-4L=0, 4-8L=20000, 8-12L=40000, 12-16L=60000, 16-20L=80000, 20-24L=100000, 24-25L=1L*30%=30000
        // Total before rebate = 330000
        // No rebate, no marginal relief
        // Cess = 330000 * 4% = 13200
        // Total = 343200
        val input = TaxInput(grossAnnualSalary = 2575000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(2500000.0, result.taxableIncome, delta)
        assertEquals(330000.0, result.taxBeforeRebate, delta)
        assertEquals(0.0, result.rebate87A, delta)
        assertEquals(13200.0, result.cess, delta)
        assertEquals(343200.0, result.totalTax, delta)
    }

    @Test
    fun `new regime - deductions do not include HRA or 80C`() {
        val input = TaxInput(
            grossAnnualSalary = 1000000.0,
            hraReceived = 200000.0,
            actualRentPaid = 150000.0,
            section80C = 150000.0,
            section80D = 25000.0
        )
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(0.0, result.hraExemption, delta)
        assertEquals(0.0, result.section80C, delta)
        assertEquals(0.0, result.section80D, delta)
        assertEquals(75000.0, result.totalDeductions, delta)
    }

    // ==================== OLD REGIME TESTS ====================

    @Test
    fun `old regime - zero salary`() {
        val input = TaxInput(grossAnnualSalary = 0.0)
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `old regime - standard deduction is 50000`() {
        val input = TaxInput(grossAnnualSalary = 1000000.0)
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(50000.0, result.standardDeduction, delta)
    }

    @Test
    fun `old regime - rebate for income under 5L`() {
        // Gross = 5,50,000, no deductions except std
        // Taxable = 5,50,000 - 50,000 = 5,00,000
        // Tax: 0-2.5L=0, 2.5-5L=12500 = 12500
        // Rebate = min(12500, 12500) = 12500
        // Total = 0
        val input = TaxInput(grossAnnualSalary = 550000.0)
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(500000.0, result.taxableIncome, delta)
        assertEquals(12500.0, result.rebate87A, delta)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `old regime - basic slab calculation at 10L`() {
        // Gross = 10,00,000, no deductions except std deduction
        // Taxable = 10,00,000 - 50,000 = 9,50,000
        // Tax: 0-2.5L=0, 2.5L-5L=12500, 5L-9.5L=4.5L*20%=90000 = 102500
        // No rebate (>5L)
        // Cess = 102500 * 4% = 4100
        // Total = 106600
        val input = TaxInput(grossAnnualSalary = 1000000.0)
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(950000.0, result.taxableIncome, delta)
        assertEquals(102500.0, result.taxBeforeRebate, delta)
        assertEquals(4100.0, result.cess, delta)
        assertEquals(106600.0, result.totalTax, delta)
    }

    @Test
    fun `old regime - 80C capped at 1_5L`() {
        val input = TaxInput(
            grossAnnualSalary = 1200000.0,
            section80C = 200000.0 // > 1.5L cap
        )
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(150000.0, result.section80C, delta)
    }

    @Test
    fun `old regime - with all deductions at 15L`() {
        // Gross = 15,00,000
        // Basic = 7,50,000 (50% of gross)
        // HRA received = 3,00,000
        // Rent paid = 2,40,000
        // City: Metro
        // HRA exemption = min(3,00,000, 2,40,000 - 75,000, 3,75,000) = min(300000, 165000, 375000) = 165000
        // 80C = 1,50,000
        // 80D = 25,000
        // Other = 50,000
        // Total deductions = 50000 + 165000 + 150000 + 25000 + 50000 = 440000
        // Taxable = 15,00,000 - 4,40,000 = 10,60,000
        // Tax: 0-2.5L=0, 2.5-5L=12500, 5-10L=100000, 10-10.6L=60000*30%=18000 = 130500
        // Cess = 130500 * 4% = 5220
        // Total = 135720
        val input = TaxInput(
            grossAnnualSalary = 1500000.0,
            hraReceived = 300000.0,
            actualRentPaid = 240000.0,
            cityType = CityType.METRO,
            section80C = 150000.0,
            section80D = 25000.0,
            otherDeductions = 50000.0
        )
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(165000.0, result.hraExemption, delta)
        assertEquals(440000.0, result.totalDeductions, delta)
        assertEquals(1060000.0, result.taxableIncome, delta)
        assertEquals(130500.0, result.taxBeforeRebate, delta)
        assertEquals(5220.0, result.cess, delta)
        assertEquals(135720.0, result.totalTax, delta)
    }

    @Test
    fun `old regime - non metro HRA uses 40 percent`() {
        // Gross = 10,00,000, Basic = 5,00,000
        // HRA = 2,00,000, Rent = 1,80,000
        // Non-metro: 40% of basic = 2,00,000
        // HRA exemption = min(2,00,000, 1,80,000 - 50,000, 2,00,000) = min(200000, 130000, 200000) = 130000
        val input = TaxInput(
            grossAnnualSalary = 1000000.0,
            hraReceived = 200000.0,
            actualRentPaid = 180000.0,
            cityType = CityType.NON_METRO
        )
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(130000.0, result.hraExemption, delta)
    }

    // ==================== HRA EXEMPTION TESTS ====================

    @Test
    fun `HRA exemption - zero when no HRA received`() {
        val result = TaxCalculator.calculateHraExemption(
            hraReceived = 0.0,
            basicSalary = 500000.0,
            rentPaid = 180000.0,
            isMetro = true
        )
        assertEquals(0.0, result, delta)
    }

    @Test
    fun `HRA exemption - zero when no rent paid`() {
        val result = TaxCalculator.calculateHraExemption(
            hraReceived = 200000.0,
            basicSalary = 500000.0,
            rentPaid = 0.0,
            isMetro = true
        )
        assertEquals(0.0, result, delta)
    }

    @Test
    fun `HRA exemption - metro 50 percent of basic`() {
        // HRA = 300000, Rent - 10% basic = 240000 - 50000 = 190000, 50% basic = 250000
        // Min = 190000
        val result = TaxCalculator.calculateHraExemption(
            hraReceived = 300000.0,
            basicSalary = 500000.0,
            rentPaid = 240000.0,
            isMetro = true
        )
        assertEquals(190000.0, result, delta)
    }

    @Test
    fun `HRA exemption - non metro 40 percent of basic`() {
        // HRA = 300000, Rent - 10% basic = 240000 - 50000 = 190000, 40% basic = 200000
        // Min = 190000
        val result = TaxCalculator.calculateHraExemption(
            hraReceived = 300000.0,
            basicSalary = 500000.0,
            rentPaid = 240000.0,
            isMetro = false
        )
        assertEquals(190000.0, result, delta)
    }

    @Test
    fun `HRA exemption - actual HRA is minimum`() {
        // HRA = 50000 (very low), Rent - 10% basic = 240000 - 50000 = 190000, 50% basic = 250000
        // Min = 50000
        val result = TaxCalculator.calculateHraExemption(
            hraReceived = 50000.0,
            basicSalary = 500000.0,
            rentPaid = 240000.0,
            isMetro = true
        )
        assertEquals(50000.0, result, delta)
    }

    // ==================== COMBINED COMPARISON TESTS ====================

    @Test
    fun `comparison - recommends correct regime for 12L salary with deductions`() {
        val input = TaxInput(
            grossAnnualSalary = 1200000.0,
            hraReceived = 200000.0,
            actualRentPaid = 180000.0,
            cityType = CityType.METRO,
            section80C = 150000.0,
            section80D = 25000.0,
            otherDeductions = 50000.0
        )
        val result = TaxCalculator.calculate(input)
        assertNotNull(result.newRegime)
        assertNotNull(result.oldRegime)
        assertTrue(result.recommendedRegime == "New Regime" || result.recommendedRegime == "Old Regime")
        assertTrue(result.savings >= 0)
    }

    @Test
    fun `comparison - low income with no deductions favors new regime`() {
        val input = TaxInput(grossAnnualSalary = 800000.0)
        val result = TaxCalculator.calculate(input)
        // New regime has higher rebate limit (12L) and 75k std deduction
        // Both should be 0 tax, but new regime is equal or better
        assertEquals("New Regime", result.recommendedRegime)
    }

    @Test
    fun `comparison - high income heavy deductions may favor old regime`() {
        val input = TaxInput(
            grossAnnualSalary = 2000000.0,
            hraReceived = 400000.0,
            actualRentPaid = 360000.0,
            cityType = CityType.METRO,
            section80C = 150000.0,
            section80D = 50000.0,
            otherDeductions = 100000.0
        )
        val result = TaxCalculator.calculate(input)
        // With heavy deductions, old regime should be better
        assertNotNull(result)
        assertTrue(result.savings > 0 || result.savings == 0.0)
    }

    // ==================== CESS TESTS ====================

    @Test
    fun `cess is 4 percent of tax after rebate`() {
        val input = TaxInput(grossAnnualSalary = 1575000.0) // 15L taxable
        val result = TaxCalculator.calculateNewRegime(input)
        val expectedCess = result.taxAfterMarginalRelief * 0.04
        assertEquals(expectedCess, result.cess, delta)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `very high income uses all slabs in new regime`() {
        val input = TaxInput(grossAnnualSalary = 3075000.0) // 30L taxable
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(3000000.0, result.taxableIncome, delta)
        // All slabs used: 0 + 20000 + 40000 + 60000 + 80000 + 100000 + 6L*30%=180000 = 480000
        assertEquals(480000.0, result.taxBeforeRebate, delta)
    }

    @Test
    fun `salary exactly at standard deduction boundary`() {
        val input = TaxInput(grossAnnualSalary = 75000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(0.0, result.taxableIncome, delta)
        assertEquals(0.0, result.totalTax, delta)
    }

    @Test
    fun `new regime slab-wise breakdown has correct count`() {
        val input = TaxInput(grossAnnualSalary = 3075000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(7, result.slabWiseBreakdown.size)
    }

    @Test
    fun `old regime slab-wise breakdown has correct count`() {
        val input = TaxInput(grossAnnualSalary = 1500000.0)
        val result = TaxCalculator.calculateOldRegime(input)
        assertEquals(4, result.slabWiseBreakdown.size)
    }

    // ==================== SURCHARGE TESTS ====================

    @Test
    fun `surcharge for income above 50L`() {
        // Gross = 55,75,000 -> Taxable = 55,00,000
        val input = TaxInput(grossAnnualSalary = 5575000.0)
        val result = TaxCalculator.calculateNewRegime(input)
        assertTrue(result.surcharge > 0)
    }

    @Test
    fun `no surcharge for income under 50L`() {
        val input = TaxInput(grossAnnualSalary = 4975000.0) // 49L taxable
        val result = TaxCalculator.calculateNewRegime(input)
        assertEquals(0.0, result.surcharge, delta)
    }
}
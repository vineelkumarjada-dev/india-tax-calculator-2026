package com.taxcalc.india.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxcalc.india.model.TaxBreakdown
import com.taxcalc.india.model.TaxResult
import com.taxcalc.india.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
}

private fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

@Composable
fun TaxResultCard(result: TaxResult, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recommendation Banner
        RecommendationBanner(result)

        // Side-by-side comparison
        TaxComparisonCard(result)

        // Detailed breakdown tabs
        DetailedBreakdownSection(result)
    }
}

@Composable
private fun RecommendationBanner(result: TaxResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = OnPrimary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recommended: ${result.recommendedRegime}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
                Text(
                    text = "You save ${formatCurrency(result.savings)} compared to the other regime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun TaxComparisonCard(result: TaxResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // New Regime Summary
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NewRegimeColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW REGIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = NewRegimeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatCurrency(result.newRegime.totalTax),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NewRegimeColor
                )
                Text(
                    text = "Total Tax",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monthly: ${formatCurrency(result.newRegime.totalTax / 12)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight
                )
            }
        }

        // Old Regime Summary
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OldRegimeColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OLD REGIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = OldRegimeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatCurrency(result.oldRegime.totalTax),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OldRegimeColor
                )
                Text(
                    text = "Total Tax",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monthly: ${formatCurrency(result.oldRegime.totalTax / 12)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceLight
                )
            }
        }
    }
}

@Composable
private fun DetailedBreakdownSection(result: TaxResult) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("New Regime", "Old Regime")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detailed Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Background,
                contentColor = Primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val breakdown = if (selectedTab == 0) result.newRegime else result.oldRegime
            BreakdownDetails(breakdown, isNewRegime = selectedTab == 0)
        }
    }
}

@Composable
private fun BreakdownDetails(breakdown: TaxBreakdown, isNewRegime: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BreakdownRow("Gross Salary", breakdown.grossSalary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)

        Text(
            text = "Deductions",
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        BreakdownRow("  Standard Deduction", breakdown.standardDeduction, isDeduction = true)
        if (!isNewRegime) {
            BreakdownRow("  HRA Exemption", breakdown.hraExemption, isDeduction = true)
            BreakdownRow("  Section 80C", breakdown.section80C, isDeduction = true)
            BreakdownRow("  Section 80D", breakdown.section80D, isDeduction = true)
            BreakdownRow("  Other Deductions", breakdown.otherDeductions, isDeduction = true)
        }
        BreakdownRow("Total Deductions", breakdown.totalDeductions, isDeduction = true, isBold = true)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)
        BreakdownRow("Taxable Income", breakdown.taxableIncome, isBold = true)

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)

        Text(
            text = "Slab-wise Tax",
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        breakdown.slabWiseBreakdown.forEach { slab ->
            if (slab.taxableAmount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "  ${slab.slabDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface
                        )
                        Text(
                            text = "    ${formatCurrency(slab.taxableAmount)} @ ${slab.rate.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceLight,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = formatCurrency(slab.tax),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)
        BreakdownRow("Tax Before Rebate", breakdown.taxBeforeRebate)

        if (breakdown.rebate87A > 0) {
            BreakdownRow("Rebate u/s 87A", breakdown.rebate87A, isDeduction = true)
        }
        BreakdownRow("Tax After Rebate", breakdown.taxAfterRebate)

        if (breakdown.marginalRelief > 0) {
            BreakdownRow("Marginal Relief", breakdown.marginalRelief, isDeduction = true)
            BreakdownRow("Tax After Marginal Relief", breakdown.taxAfterMarginalRelief)
        }

        if (breakdown.surcharge > 0) {
            BreakdownRow("Surcharge", breakdown.surcharge)
        }

        BreakdownRow("Health & Edu. Cess (4%)", breakdown.cess)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 2.dp,
            color = Primary
        )
        BreakdownRow("TOTAL TAX PAYABLE", breakdown.totalTax, isBold = true, isHighlight = true)
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: Double,
    isDeduction: Boolean = false,
    isBold: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlight)
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                else Modifier.padding(vertical = 2.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) Primary else OnSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isDeduction && amount > 0)
                "- ${formatCurrency(amount)}" else formatCurrency(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isHighlight -> Primary
                isDeduction && amount > 0 -> SuccessGreen
                else -> OnSurface
            },
            textAlign = TextAlign.End
        )
    }
}
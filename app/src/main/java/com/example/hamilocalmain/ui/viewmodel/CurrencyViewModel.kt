package com.example.hamilocalmain.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val rateFromNPR: Double   // how many units of this currency = 1 NPR
)

val supportedCurrencies = listOf(
    CurrencyOption("NPR", "NPR", "Nepali Rupee",     1.0),
    CurrencyOption("USD", "$",   "US Dollar",         0.0075),
    CurrencyOption("INR", "₹",   "Indian Rupee",      0.625),
    CurrencyOption("EUR", "€",   "Euro",              0.0069),
    CurrencyOption("GBP", "£",   "British Pound",     0.0059),
    CurrencyOption("AUD", "A$",  "Australian Dollar", 0.0115),
    CurrencyOption("CNY", "¥",   "Chinese Yuan",      0.054),
    CurrencyOption("JPY", "¥",   "Japanese Yen",      1.13),
    CurrencyOption("SAR", "﷼",   "Saudi Riyal",       0.028),
    CurrencyOption("AED", "د.إ", "UAE Dirham",         0.0275)
)

class CurrencyViewModel : ViewModel() {
    private val _selectedCurrency = MutableStateFlow(supportedCurrencies[0])
    val selectedCurrency: StateFlow<CurrencyOption> = _selectedCurrency.asStateFlow()

    fun setCurrency(currency: CurrencyOption) {
        _selectedCurrency.value = currency
    }

    fun convert(amountNPR: Double): Double {
        return amountNPR * _selectedCurrency.value.rateFromNPR
    }

    fun format(amountNPR: Double): String {
        val converted = convert(amountNPR)
        val symbol = _selectedCurrency.value.symbol
        return "$symbol ${String.format("%.2f", converted)}"
    }
}

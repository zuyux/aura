package io.aura.android.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.text.Normalizer
import java.util.Locale

internal data class PhoneCountry(
    val regionCode: String,
    val name: String,
    val callingCode: String,
) {
    val flag: String = regionCode
        .uppercase(Locale.ROOT)
        .map { char -> Character.toChars(0x1F1E6 + (char.code - 'A'.code)).concatToString() }
        .joinToString("")
}

@Composable
internal fun CountryPhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countries = remember { availablePhoneCountries() }
    val defaultRegion = remember { Locale.getDefault().country.uppercase(Locale.ROOT) }
    var selectedCountry by remember {
        mutableStateOf(countryForNumber(value, countries) ?: countries.firstOrNull { it.regionCode == defaultRegion } ?: countries.first())
    }
    LaunchedEffect(value) {
        val numberCountry = countryForNumber(value, countries)
        if (numberCountry != null && !value.filter(Char::isDigit).startsWith(selectedCountry.callingCode)) {
            selectedCountry = numberCountry
        }
    }
    var showCountryPicker by remember { mutableStateOf(false) }
    val nationalNumber = value.filter(Char::isDigit).removePrefix(selectedCountry.callingCode)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { showCountryPicker = true },
            modifier = Modifier.widthIn(min = 112.dp),
        ) {
            Text("${selectedCountry.flag} +${selectedCountry.callingCode}")
            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Elegir país")
        }
        OutlinedTextField(
            value = nationalNumber,
            onValueChange = { input ->
                val digits = input.filter(Char::isDigit).take(MAX_PHONE_NUMBER_LENGTH - selectedCountry.callingCode.length)
                onValueChange(if (digits.isEmpty()) "" else "+${selectedCountry.callingCode}$digits")
            },
            modifier = Modifier.weight(1f),
            label = { Text("Teléfono") },
            placeholder = { Text("Número") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            countries = countries,
            onCountrySelected = { country ->
                selectedCountry = country
                showCountryPicker = false
                onValueChange(if (nationalNumber.isEmpty()) "" else "+${country.callingCode}$nationalNumber")
            },
            onDismiss = { showCountryPicker = false },
        )
    }
}

@Composable
private fun CountryPickerDialog(
    countries: List<PhoneCountry>,
    onCountrySelected: (PhoneCountry) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.normalizedForSearch().removePrefix("+")
    val filteredCountries = remember(countries, normalizedQuery) {
        if (normalizedQuery.isBlank()) countries else countries.filter { country ->
            country.name.normalizedForSearch().contains(normalizedQuery) ||
                country.regionCode.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                country.callingCode.contains(normalizedQuery)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona un país") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar país o código") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                )
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredCountries, key = { it.regionCode }) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .padding(vertical = 14.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(country.flag, modifier = Modifier.padding(end = 12.dp))
                            Text(country.name, modifier = Modifier.weight(1f))
                            Text("+${country.callingCode}")
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

internal fun availablePhoneCountries(locale: Locale = Locale.getDefault()): List<PhoneCountry> {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return phoneNumberUtil.supportedRegions
        .mapNotNull { regionCode ->
            val callingCode = phoneNumberUtil.getCountryCodeForRegion(regionCode)
            if (callingCode <= 0) null else PhoneCountry(
                regionCode = regionCode,
                name = Locale("", regionCode).getDisplayCountry(locale),
                callingCode = callingCode.toString(),
            )
        }
        .sortedBy { it.name.lowercase(locale) }
}

private fun countryForNumber(value: String, countries: List<PhoneCountry>): PhoneCountry? {
    if (!value.trim().startsWith("+")) return null
    val digits = value.filter(Char::isDigit)
    return countries
        .filter { digits.startsWith(it.callingCode) }
        .maxByOrNull { it.callingCode.length }
}

private fun String.normalizedForSearch(): String = Normalizer
    .normalize(trim().lowercase(Locale.getDefault()), Normalizer.Form.NFD)
    .replace(Regex("\\p{Mn}+"), "")

private const val MAX_PHONE_NUMBER_LENGTH = 15

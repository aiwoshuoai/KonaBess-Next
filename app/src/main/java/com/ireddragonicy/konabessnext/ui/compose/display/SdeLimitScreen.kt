package com.ireddragonicy.konabessnext.ui.compose.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ireddragonicy.konabessnext.R
import com.ireddragonicy.konabessnext.model.display.SdeLimitModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdeLimitScreen(
    sdeLimits: SdeLimitModel?,
    onBack: () -> Unit,
    onEditMaxBandwidth: (Long) -> Unit,
    onEditPerPipeBandwidth: (Int, Long) -> Unit,
    onEditClockMaxRate: (Int, Long) -> Unit
) {
    if (sdeLimits == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.sde_limits_not_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sde_limits_not_found_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text(stringResource(R.string.btn_back))
                }
            }
        }
        return
    }

    var editType by remember { mutableStateOf<EditDialogType?>(null) }
    var editIndex by remember { mutableStateOf(-1) }
    var editValue by remember { mutableStateOf(0L) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_back), null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_back))
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Helper Text
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.sde_limits_overclock),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.sde_limits_help_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Max Bandwidth Section
            sdeLimits.maxBw?.let { maxBw ->
                item {
                    SectionHeader(stringResource(R.string.sde_max_bandwidth))
                    ValueCard(
                        title = stringResource(R.string.total_max_bandwidth),
                        value = maxBw,
                        onClick = {
                            editType = EditDialogType.MAX_BW
                            editValue = maxBw
                            showDialog = true
                        }
                    )
                }
            }

            // Per Pipe Bandwidth Section
            if (sdeLimits.perPipeBw.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.sde_per_pipe_bandwidth))
                }
                itemsIndexed(sdeLimits.perPipeBw) { index, bw ->
                    ValueCard(
                        title = stringResource(R.string.pipe_index, index),
                        value = bw,
                        onClick = {
                            editType = EditDialogType.PER_PIPE_BW
                            editIndex = index
                            editValue = bw
                            showDialog = true
                        }
                    )
                }
            }

            // Clock Max Rates Section
            if (sdeLimits.clockMaxRates.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.sde_clock_max_rates))
                }
                itemsIndexed(sdeLimits.clockMaxRates) { index, rate ->
                    ValueCard(
                        title = stringResource(R.string.clock_state_index, index),
                        value = rate,
                        onClick = {
                            editType = EditDialogType.CLOCK_RATE
                            editIndex = index
                            editValue = rate
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog && editType != null) {
        var inputVal by remember { mutableStateOf(editValue.toString()) }

        val dialogTitle = when (editType) {
            EditDialogType.MAX_BW -> stringResource(R.string.edit_max_bandwidth)
            EditDialogType.PER_PIPE_BW -> stringResource(R.string.edit_per_pipe_bandwidth, editIndex)
            EditDialogType.CLOCK_RATE -> stringResource(R.string.edit_clock_rate, editIndex)
            null -> ""
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.current_value_hex_dec, editValue, editValue.toString(16)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        label = { Text(stringResource(R.string.new_value)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    parseDecOrHexInput(inputVal)?.let { newVal ->
                        when (editType) {
                            EditDialogType.MAX_BW -> onEditMaxBandwidth(newVal)
                            EditDialogType.PER_PIPE_BW -> onEditPerPipeBandwidth(editIndex, newVal)
                            EditDialogType.CLOCK_RATE -> onEditClockMaxRate(editIndex, newVal)
                            null -> {}
                        }
                    }
                    showDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueCard(
    title: String,
    value: Long,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.bandwidth_hex_dec_format, value.toString(16), value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun parseDecOrHexInput(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("0x", ignoreCase = true)) {
        return try {
            java.lang.Long.decode(trimmed)
        } catch (_: NumberFormatException) {
            null
        }
    }
    return trimmed.toLongOrNull()
}

private enum class EditDialogType {
    MAX_BW, PER_PIPE_BW, CLOCK_RATE
}

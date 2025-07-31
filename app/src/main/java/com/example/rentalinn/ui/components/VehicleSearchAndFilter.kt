package com.example.rentalinn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class VehicleFilter(
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val transmission: String? = null,
    val fuelType: String? = null,
    val minPassengers: Int? = null,
    val availableOnly: Boolean = true
)

enum class SortOption(val displayName: String) {
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    ALPHABETICAL("A to Z")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search vehicles..."
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = false,
        onActiveChange = { },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { 
            Icon(Icons.Default.Search, contentDescription = "Search") 
        },
        trailingIcon = {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }
    ) { }
}

@Composable
fun VehicleCategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                selected = selectedCategory == null,
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
        
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                selected = selectedCategory == category,
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFilterDialog(
    filter: VehicleFilter,
    onFilterChange: (VehicleFilter) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    categories: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var tempFilter by remember { mutableStateOf(filter) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = "Filter Vehicles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category filter
                if (categories.isNotEmpty()) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    var expandedCategory by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = tempFilter.category ?: "All Categories",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories") },
                                onClick = {
                                    tempFilter = tempFilter.copy(category = null)
                                    expandedCategory = false
                                }
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        tempFilter = tempFilter.copy(category = category)
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Price range
                Text(
                    text = "Price Range (per day)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempFilter.minPrice?.toString() ?: "",
                        onValueChange = { 
                            tempFilter = tempFilter.copy(
                                minPrice = it.toDoubleOrNull()
                            )
                        },
                        label = { Text("Min Price") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = tempFilter.maxPrice?.toString() ?: "",
                        onValueChange = { 
                            tempFilter = tempFilter.copy(
                                maxPrice = it.toDoubleOrNull()
                            )
                        },
                        label = { Text("Max Price") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Transmission
                Text(
                    text = "Transmission",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Manual", "Automatic").forEach { transmission ->
                        FilterChip(
                            onClick = { 
                                tempFilter = tempFilter.copy(
                                    transmission = if (transmission == "All") null else transmission
                                )
                            },
                            label = { Text(transmission) },
                            selected = if (transmission == "All") {
                                tempFilter.transmission == null
                            } else {
                                tempFilter.transmission == transmission
                            }
                        )
                    }
                }
                
                // Available only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available only",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Switch(
                        checked = tempFilter.availableOnly,
                        onCheckedChange = { 
                            tempFilter = tempFilter.copy(availableOnly = it)
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFilterChange(tempFilter)
                    onApply()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = {
                    tempFilter = VehicleFilter()
                    onReset()
                }) {
                    Text("Reset")
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun VehicleSortChip(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        FilterChip(
            onClick = { expanded = true },
            label = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(currentSort.displayName)
                }
            },
            selected = true
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    leadingIcon = if (option == currentSort) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

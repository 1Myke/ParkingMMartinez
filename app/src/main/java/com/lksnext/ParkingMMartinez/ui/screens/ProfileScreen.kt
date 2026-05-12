package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.components.ProfileHeaderSection
import com.lksnext.ParkingMMartinez.ui.components.VehicleCard
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.bookingCardColor
import com.lksnext.ParkingMMartinez.ui.theme.lightGray
import com.lksnext.ParkingMMartinez.ui.theme.navyBlue
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onLogoutClick: () -> Unit
){
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }
    
    Scaffold(
        // SOLO para el boton flotante naranja
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onOpenDialog() },
                containerColor = LksOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) //????HACE FALTA
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.Default.Logout,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp))
                }
            }

            ProfileHeaderSection(
                viewModel.userName,
                viewModel.userRole,
                viewModel.userEmail
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = lightGray)

            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = LksOrange,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.profile_vehicles),//"My Vehicles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // LISTA DINÁMICA <- de momento! Luego tendre que ver como hacerlo con Firebase
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val canDelete = viewModel.vehicles.size > 1

                viewModel.vehicles.forEach { vehicle ->
                    VehicleCard(
                        name = vehicle.name,
                        plate = vehicle.plate,
                        type = vehicle.type,
                        onDeleteClick = if (canDelete) {
                            { viewModel.askDeleteVehicle(vehicle) }
                        } else null
                    )
                }
            }
        }

        if (viewModel.showAddVehicleDialog) {
            AddVehicleDialog(viewModel)
        }

        if (viewModel.showDeleteConfirmation) {
            DeleteConfirmationDialog(viewModel)
        }

    }
}

@Composable
fun AddVehicleDialog(viewModel: ProfileViewModel) {
    Dialog(
        onDismissRequest = { viewModel.onCloseDialog() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.profile_add_vehicle_title), // "+ Add New Vehicle"
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.profile_label_nickname), // "NICKNAME"
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                LksTextField(
                    value = viewModel.newVehicleName,
                    onValueChange = { viewModel.newVehicleName = it },
                    label = stringResource(R.string.profile_hint_nickname), // "Nickname"
                    placeholder = stringResource(R.string.profile_placeholder_nickname) // "e.g. Work Car"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.profile_label_plate), // "LICENSE PLATE"
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                LksTextField(
                    value = viewModel.newVehiclePlate,
                    onValueChange = { viewModel.newVehiclePlate = it },
                    label = stringResource(R.string.profile_hint_plate), // "License Plate"
                    placeholder = stringResource(R.string.profile_placeholder_plate) // "1234 ABC"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.profile_label_type), // "VEHICLE TYPE"
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.values().forEach { type ->
                        val isSelected = viewModel.selectedVehicleType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onVehicleTypeChange(type) },
                            label = {
                                // Esto formatea el Enum (ej: ELECTRIC_CAR -> Electric car)
                                Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LksOrange.copy(alpha = 0.2f),
                                selectedLabelColor = LksOrange
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onCloseDialog() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancel)) // "Cancel"
                    }

                    LksButton(
                        text = stringResource(R.string.profile_btn_save), // "Save Vehicle"
                        onClick = { viewModel.addVehicle() },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(viewModel: ProfileViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissDeleteDialog() },
        title = { Text(text = stringResource(R.string.profile_delete_title)) }, // "Confirm Delete"
        text = {
            Text(
                text = stringResource(
                    R.string.profile_delete_msg,
                    viewModel.vehicleToDelete?.name ?: ""
                ) // "Are you sure you want to remove %1$s?..."
            )
        },
        confirmButton = {
            TextButton(onClick = { viewModel.confirmDeleteVehicle() }) {
                Text(
                    text = stringResource(R.string.btn_delete), // "Delete"
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                Text(text = stringResource(R.string.btn_cancel), color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        onLogoutClick = {}
    )
}
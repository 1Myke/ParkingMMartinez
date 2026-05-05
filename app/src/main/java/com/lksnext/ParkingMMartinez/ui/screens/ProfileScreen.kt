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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.components.ProfileHeaderSection
import com.lksnext.ParkingMMartinez.ui.components.VehicleCard
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onAddVehicleClick: () -> Unit
){
    /*
    //DATOS DE PRUEBA
    val userName = "1Myke"
    val userRole = "Senior Operations Manager"
    val userEmail = "mikel@lksnext.com"
     */
    
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
            Spacer(modifier = Modifier.height(40.dp))

            ProfileHeaderSection(
                viewModel.userName,
                viewModel.userRole,
                viewModel.userEmail
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = Color(0xFFEEEEEE))

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
                    text = "My Vehicles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // LISTA DINÁMICA
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Iteramos sobre la lista del ViewModel
                viewModel.vehicles.forEach { vehicle ->
                    VehicleCard(
                        name = vehicle.name,
                        plate = vehicle.plate,
                        type = vehicle.type,
                        onDeleteClick = { viewModel.askDeleteVehicle(vehicle) }
                    )
                }
            }
        }

        if (viewModel.showAddVehicleDialog) {
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
                            text = "+ Add New Vehicle",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2C42)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("NICKNAME", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        LksTextField(
                            value = viewModel.newVehicleName,
                            onValueChange = { viewModel.newVehicleName = it },
                            label = "Nickname",
                            placeholder = "e.g. Work Car"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("LICENSE PLATE", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        LksTextField(
                            value = viewModel.newVehiclePlate,
                            onValueChange = { viewModel.newVehiclePlate = it },
                            label = "License Plate",
                            placeholder = "1234 ABC"
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "VEHICLE TYPE",
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
                                    label = { Text(type.name.replace("_", " ").lowercase().capitalize()) },
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
                                Text("Cancel", maxLines = 1)
                            }

                            LksButton(
                                text = "Save Vehicle",
                                onClick = { viewModel.addVehicle() },
                                modifier = Modifier.weight(1.3f) // Un poco más de peso para que no corte el texto
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.showDeleteConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteDialog() },
                title = { Text(text = "Confirm Delete") },
                text = {
                    Text("Are you sure you want to remove ${viewModel.vehicleToDelete?.name}? This action cannot be undone.")
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.confirmDeleteVehicle() }
                    ) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.dismissDeleteDialog() }
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        onAddVehicleClick = {}
    )
}
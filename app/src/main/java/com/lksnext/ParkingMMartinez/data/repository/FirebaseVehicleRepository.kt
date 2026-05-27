package com.lksnext.ParkingMMartinez.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import kotlinx.coroutines.tasks.await

class FirebaseVehicleRepository : VehicleRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("vehicles")

    override suspend fun getVehicles(userId: String): List<Vehicle> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.map { doc ->
                Vehicle(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    name = doc.getString("name") ?: "",
                    plate = doc.getString("plate") ?: "",
                    type = VehicleType.valueOf(doc.getString("type") ?: "STANDARD")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addVehicle(userId: String, vehicle: Vehicle) {
        val vehicleData = hashMapOf(
            "userId" to userId,
            "name" to vehicle.name,
            "plate" to vehicle.plate,
            "type" to vehicle.type.name
        )
        collection.document(vehicle.id).set(vehicleData).await()
    }

    override suspend fun deleteVehicle(userId: String, vehicle: Vehicle) {
        collection.document(vehicle.id).delete().await()
    }
}
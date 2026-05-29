package com.lksnext.ParkingMMartinez.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.ParkingZone
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import java.util.Date

class FirebaseBookingRepository() : BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("bookings")

    override suspend fun saveReservation(reservation: Reservation) {
        val data = hashMapOf(
            "id" to reservation.id,
            "spotNumber" to reservation.spotNumber,
            "vehicle" to reservation.vehicle,
            "zone" to reservation.zone,
            "date" to reservation.date,
            "startTime" to reservation.startTime.toString(),
            "endTime" to reservation.endTime.toString(),
            "isCheckedIn" to reservation.isCheckedIn
        )
        collection.document(reservation.id).set(data).await()
    }

    override suspend fun cancelReservation(reservationId: String) {
        collection.document(reservationId).delete().await()
    }

    private fun mapDocumentToReservation(doc: com.google.firebase.firestore.DocumentSnapshot): Reservation? {
        return try {
            val id = doc.getString("id") ?: ""
            val spotNumber = (doc.getLong("spotNumber") ?: 0).toInt()
            val date = doc.getDate("date") ?: Date()
            val isCheckedIn = doc.getBoolean("isCheckedIn") ?: false

            val startTime = java.time.LocalTime.parse(doc.getString("startTime") ?: "00:00")
            val endTime = java.time.LocalTime.parse(doc.getString("endTime") ?: "00:00")

            val vehicle = doc.getField<Vehicle>("vehicle") ?: return null
            val zone = doc.getField<ParkingZone>("zone") ?: return null

            Reservation(id, spotNumber, vehicle, zone, date, startTime, endTime, isCheckedIn)
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_READ_ERROR", "Error parseando documento: ${e.message}")
            null
        }
    }

    // Para simplificar codigo y que no salga codigo duplicado de sonar

    override suspend fun getAllReservations(): List<Reservation> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { mapDocumentToReservation(it) }
    }

    override suspend fun getUserReservations(userId: String): List<Reservation> {
        val snapshot = collection.whereEqualTo("vehicle.userId", userId).get().await()
        return snapshot.documents.mapNotNull { mapDocumentToReservation(it) }
    }
}
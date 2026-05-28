package com.lksnext.ParkingMMartinez.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.ParkingZone
import kotlinx.coroutines.tasks.await
import java.time.LocalTime
import java.util.Date

class FirebaseBookingRepository(private val db: FirebaseFirestore) : BookingRepository {

    private val collection = db.collection("bookings")

    override suspend fun getAllReservations(): List<Reservation> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                // Mapeo manual de campos para evitar conflictos de tipos
                val id = doc.getString("id") ?: ""
                val spotNumber = (doc.getLong("spotNumber") ?: 0).toInt()
                val date = doc.getDate("date") ?: Date()
                val isCheckedIn = doc.getBoolean("isCheckedIn") ?: false

                val startTime = LocalTime.parse(doc.getString("startTime") ?: "00:00")
                val endTime = LocalTime.parse(doc.getString("endTime") ?: "00:00")

                // Aquí Firebase reconstruye el objeto Vehicle automáticamente gracias a los valores por defecto
                val vehicle = doc.toObject(Vehicle::class.java)!!
                val zone = doc.toObject(ParkingZone::class.java)!!

                Reservation(id, spotNumber, vehicle, zone, date, startTime, endTime, isCheckedIn)
            } catch (e: Exception) { null }
        }
    }

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

    override suspend fun getUserReservations(userId: String): List<Reservation> {
        // Buscamos dentro del campo 'vehicle' el atributo 'userId'
        val snapshot = collection.whereEqualTo("vehicle.userId", userId).get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                val startTime = LocalTime.parse(doc.getString("startTime") ?: "00:00")
                val endTime = LocalTime.parse(doc.getString("endTime") ?: "00:00")

                Reservation(
                    id = doc.getString("id") ?: "",
                    spotNumber = (doc.getLong("spotNumber") ?: 0).toInt(),
                    vehicle = doc.toObject(Vehicle::class.java)!!,
                    zone = doc.toObject(ParkingZone::class.java)!!,
                    date = doc.getDate("date") ?: Date(),
                    startTime = startTime,
                    endTime = endTime,
                    isCheckedIn = doc.getBoolean("isCheckedIn") ?: false
                )
            } catch (e: Exception) { null }
        }
    }
}
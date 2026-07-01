package com.lksnext.ParkingMMartinez.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lksnext.ParkingMMartinez.model.NotificationItem

class FirebaseNotificationRepository : NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("notifications")

    override fun saveNotification(notification: NotificationItem) {
        val docRef = collection.document()
        val finalNotification = notification.copy(id = docRef.id)
        docRef.set(finalNotification)
    }

    override fun getNotificationsFlow(userId: String, onUpdate: (List<NotificationItem>) -> Unit) {
        collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot.toObjects(NotificationItem::class.java)

                val sortedItems = items.sortedByDescending { it.timestamp?.time ?: 0L }
                onUpdate(sortedItems)
            }
    }

    override fun markAllAsRead(userId: String) {
        collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }

    override fun deleteNotification(notificationId: String) {
        collection.document(notificationId).delete()
    }

    override fun deleteAllNotifications(userId: String) {
        collection.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }
}
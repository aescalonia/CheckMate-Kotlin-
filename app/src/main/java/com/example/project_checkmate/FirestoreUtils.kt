package com.project_checkmate

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtils {
    private val db = FirebaseFirestore.getInstance()

    fun addTask(task: Task) {
        db.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }
}
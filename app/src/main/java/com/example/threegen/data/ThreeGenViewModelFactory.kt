package com.example.threegen.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class ThreeGenViewModelFactory(
    private val dao: ThreeGenDao,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreeGenViewModel::class.java)) {
            return ThreeGenViewModel(dao, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

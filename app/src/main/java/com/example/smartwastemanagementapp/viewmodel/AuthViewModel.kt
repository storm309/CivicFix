package com.example.smartwastemanagementapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.smartwastemanagementapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoggedIn = mutableStateOf(auth.currentUser != null)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userProfile = mutableStateOf<User?>(null)
    val userProfile: State<User?> = _userProfile

    init {
        auth.currentUser?.uid?.let { fetchUserProfile(it) }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result.user?.uid ?: ""
                    fetchUserProfile(uid)
                    _isLoggedIn.value = true
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Login Failed"
                }
            }
    }

    fun signUp(
        name: String,
        email: String,
        age: String,
        phone: String,
        gender: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || age.isBlank() || phone.isBlank() || gender.isBlank() || pass.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result.user?.uid ?: ""
                    val newUser = User(uid, name, email, age, phone, gender)
                    usersCollection.document(uid).set(newUser)
                        .addOnCompleteListener { dbTask ->
                            _isLoading.value = false
                            if (dbTask.isSuccessful) {
                                _userProfile.value = newUser
                                _isLoggedIn.value = true
                                onSuccess()
                            } else {
                                _error.value = "Failed to save user data: ${dbTask.exception?.message}"
                            }
                        }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Signup Failed"
                }
            }
    }

    private fun fetchUserProfile(uid: String) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { snapshot ->
                _userProfile.value = snapshot.toObject(User::class.java)
            }
            .addOnFailureListener {
                // Profile fetch failure is non-fatal – user can still use the app
            }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _userProfile.value = null
    }
}

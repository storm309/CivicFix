package com.example.smartwastemanagementapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.smartwastemanagementapp.model.AuthRole
import com.example.smartwastemanagementapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase
        .getInstance("https://civicfix-92e86-default-rtdb.firebaseio.com")
        .getReference("users")

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isLoggedIn = mutableStateOf(auth.currentUser != null)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _isAdmin = mutableStateOf(false)
    val isAdmin: State<Boolean> = _isAdmin

    private val _userProfile = mutableStateOf<User?>(null)
    val userProfile: State<User?> = _userProfile

    private val adminEmails = setOf("admin@civicfix.com")

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
                    val firebaseUser = task.result.user
                    val uid = firebaseUser?.uid.orEmpty()
                    val provisionalRole = if (isAdminEmail(firebaseUser?.email)) AuthRole.ADMIN else AuthRole.USER
                    _userProfile.value = User(
                        uid = uid,
                        name = firebaseUser?.displayName.orEmpty(),
                        email = firebaseUser?.email.orEmpty(),
                        role = provisionalRole.dbValue,
                        authProvider = "email"
                    )
                    fetchUserProfile(uid)
                    _isLoggedIn.value = true
                    syncRoleFlags()
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
                    val newUser = User(
                        uid = uid,
                        name = name,
                        email = email,
                        age = age,
                        phoneNumber = phone,
                        gender = gender,
                        role = AuthRole.USER.dbValue,
                        authProvider = "email"
                    )
                    database.child(uid).setValue(newUser)
                        .addOnCompleteListener { dbTask ->
                            _isLoading.value = false
                            if (dbTask.isSuccessful) {
                                _userProfile.value = newUser
                                _isLoggedIn.value = true
                                syncRoleFlags()
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
        database.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val dbUser = snapshot.getValue(User::class.java)
                if (dbUser != null) {
                    _userProfile.value = dbUser
                } else {
                    val current = _userProfile.value
                    if (current != null && current.uid == uid) {
                        database.child(uid).setValue(current)
                    }
                }
                syncRoleFlags()
            }
            .addOnFailureListener {
                // Non-fatal – user can still navigate the app
            }
    }

    fun startOtp(phoneNumber: String) {
        _error.value = if (phoneNumber.isBlank()) {
            "Enter phone number first"
        } else {
            "OTP login is scaffolded. Add Firebase PhoneAuth callbacks to enable verification."
        }
    }

    fun verifyOtp(code: String) {
        _error.value = if (code.isBlank()) {
            "Enter OTP code"
        } else {
            "OTP verification is scaffolded. Connect verificationId + credential flow."
        }
    }

    fun signInWithGoogleToken(idToken: String, onSuccess: () -> Unit) {
        _error.value = if (idToken.isBlank()) {
            "Missing Google token"
        } else {
            "Google sign-in scaffold added. Connect Google credential sign-in to enable this button."
        }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _userProfile.value = null
        _isAdmin.value = false
    }

    private fun isAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return adminEmails.any { it.equals(email, ignoreCase = true) }
    }

    private fun syncRoleFlags() {
        val role = AuthRole.from(_userProfile.value?.role)
        _isAdmin.value = role == AuthRole.ADMIN || isAdminEmail(_userProfile.value?.email)
    }
}

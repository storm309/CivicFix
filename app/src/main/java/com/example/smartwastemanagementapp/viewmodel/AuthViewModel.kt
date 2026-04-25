package com.example.smartwastemanagementapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.smartwastemanagementapp.model.AuthRole
import com.example.smartwastemanagementapp.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

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

    private val _isProfileComplete = mutableStateOf(false)
    val isProfileComplete: State<Boolean> = _isProfileComplete

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
                    _isProfileComplete.value = dbUser.name.isNotBlank() && dbUser.age.isNotBlank()
                } else {
                    val current = _userProfile.value
                    if (current != null && current.uid == uid) {
                        database.child(uid).setValue(current)
                        _isProfileComplete.value = true
                    } else {
                        _isProfileComplete.value = false
                    }
                }
                syncRoleFlags()
            }
            .addOnFailureListener {
                _isProfileComplete.value = false
            }
    }

    fun updateProfile(name: String, age: String, gender: String, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val currentEmail = auth.currentUser?.email ?: ""
        val currentPhone = auth.currentUser?.phoneNumber ?: ""
        
        val updatedUser = User(
            uid = uid,
            name = name,
            email = currentEmail,
            age = age,
            phoneNumber = currentPhone,
            gender = gender,
            role = AuthRole.USER.dbValue,
            authProvider = if (currentPhone.isNotBlank()) "phone" else "email"
        )
        
        _isLoading.value = true
        database.child(uid).setValue(updatedUser)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _userProfile.value = updatedUser
                    _isProfileComplete.value = true
                    onSuccess()
                } else {
                    _error.value = task.exception?.message
                }
            }
    }

    private var verificationId: String? = null

    fun startOtp(phoneNumber: String, activity: android.app.Activity) {
        if (phoneNumber.isBlank()) {
            _error.value = "Enter phone number first"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isLoading.value = false
                    _error.value = e.message
                }

                override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _isLoading.value = false
                    verificationId = verId
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        if (code.isBlank() || verificationId == null) {
            _error.value = "Enter valid OTP"
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result.user?.uid ?: ""
                    fetchUserProfile(uid)
                    _isLoggedIn.value = true
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                }
            }
    }

    fun signInWithGoogleToken(idToken: String, onSuccess: () -> Unit) {
        if (idToken.isBlank()) {
            _error.value = "Google Sign-In failed: Empty Token"
            return
        }
        
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result.user
                    val uid = firebaseUser?.uid.orEmpty()
                    
                    // If user is new or profile missing, fetchUserProfile will set isProfileComplete to false
                    fetchUserProfile(uid)
                    _isLoggedIn.value = true
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Google Sign-In Failed"
                }
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

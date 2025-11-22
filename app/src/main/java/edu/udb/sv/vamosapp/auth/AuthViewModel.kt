package edu.udb.sv.vamosapp.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value =
                        task.exception?.message ?: "Error al registrarse"
                }
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value =
                        task.exception?.message ?: "Error al iniciar sesión"
                }
            }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.message ?: "Error al iniciar con Google"
                    _errorMessage.value = msg
                    onError(msg)
                }
            }
    }

    fun signInWithFacebook(
        accessToken: AccessToken,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.message ?: "Error al iniciar con Facebook"
                    _errorMessage.value = msg
                    onError(msg)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid


    fun getCurrentUserName(): String {
        val user = auth.currentUser ?: return "Usuario"

        val name = user.displayName
        val email = user.email

        return when {
            !name.isNullOrBlank() -> name
            !email.isNullOrBlank() -> email.substringBefore("@")
            else -> "Usuario"
        }
    }
}

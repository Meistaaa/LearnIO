package com.shazycode.learnio.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.shazycode.learnio.model.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel :ViewModel(){
    private val auth = FirebaseAuth.getInstance()

    val authRepository= AuthRepository()

    val currentUser= MutableStateFlow<FirebaseUser?>(null)
    val failureMessage= MutableStateFlow<String?>(null)
    val resetResponse= MutableStateFlow<Boolean?>(null)

    init {
        // Initialize currentUser when ViewModel is created
        currentUser.value = auth.currentUser
    }

    fun login(email:String,password:String){
        viewModelScope.launch {
            val result=authRepository.login(email,password)
            if (result.isSuccess){
                currentUser.value=result.getOrThrow()
            }else{
                failureMessage.value=result.exceptionOrNull()?.message
            }
        }
    }
    fun resetPassword(email:String){
        viewModelScope.launch {
            val result=authRepository.resetPassword(email)
            if (result.isSuccess){
                resetResponse.value=result.getOrThrow()
            }else{
                failureMessage.value=result.exceptionOrNull()?.message
            }
        }
    }
    fun signUp(email:String,password:String,name:String){
        viewModelScope.launch {
            val result=authRepository.signup(email,password,name)
            if (result.isSuccess){
                currentUser.value=result.getOrThrow()
            }else{
                failureMessage.value=result.exceptionOrNull()?.message
            }
        }
    }

    fun checkUser(){
        currentUser.value=authRepository.getCurrentUser()
    }

    fun updateProfileName(newName: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                user?.updateProfile(userProfileChangeRequest { displayName = newName })?.await()
                currentUser.value = user
            } catch (e: Exception) {
                failureMessage.value = e.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            if (result.isSuccess) {
                currentUser.value = null
            } else {
                failureMessage.value = result.exceptionOrNull()?.message
            }
        }

    }

}
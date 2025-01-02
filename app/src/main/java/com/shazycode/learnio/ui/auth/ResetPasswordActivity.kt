package com.shazycode.learnio.ui.auth


import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shazycode.learnio.databinding.ActivityResetPasswordBinding
import com.shazycode.learnio.ui.viewModels.AuthViewModel
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    lateinit var binding: ActivityResetPasswordBinding
    lateinit var viewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel= AuthViewModel()

        progressDialog=ProgressDialog(this)
        progressDialog.setMessage("Please wait while...")
        progressDialog.setCancelable(false)

        lifecycleScope.launch {
            viewModel.failureMessage.collect{
                progressDialog.dismiss()
                if (it!=null){
                    Toast.makeText(this@ResetPasswordActivity,it,Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.resetResponse.collect{
                progressDialog.dismiss()
                if (it!=null){
                    val builder=MaterialAlertDialogBuilder(this@ResetPasswordActivity)
                    builder.setMessage("We have sent you a password reset email, check your inbox and click the link to reset your password")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                        finish()
                    })
                    builder.show()
                }
            }
        }

        binding.signupTxt.setOnClickListener {
            finish()
        }

        binding.loginbtn.setOnClickListener {
            val email=binding.email.editText?.text.toString()

            if(!email.contains("@")){
                Toast.makeText(this,"Invalid Email",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressDialog.show()

            viewModel.resetPassword(email)

        }


    }
}
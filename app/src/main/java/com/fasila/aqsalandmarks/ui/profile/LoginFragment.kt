package com.fasila.aqsalandmarks.ui.profile

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.fasila.aqsalandmarks.R
import com.fasila.aqsalandmarks.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        //click sign in button
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            try {
                login(email, password)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        binding.resetPassword.setOnClickListener {
            binding.resetPassword.text = resources.getText(R.string.forget_password_text_focus)
            val email = binding.emailEditText.text.toString().trim()
            try {
                resetPassword(email)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth.currentUser?.let {
            if (it.isEmailVerified) {
                findNavController().navigate(R.id.action_loginFragment_to_stagesFragment)
            }
        }
    }

    private fun login(email: String, password: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInput.error = "من فضلك أدخل بريد إلكتروني صحيح"
            binding.emailTextInput.requestFocus()
        }
        if (password.isBlank() || password.length < 6) {
            binding.passwordTextInput.error = "لابد أن يتكون السري من 6 أرقام أو حروف على الأقل"
            binding.passwordTextInput.requestFocus()
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInWithEmail:success")
                        val  user = auth.currentUser!!
                        if (user.isEmailVerified) {
                            findNavController().navigate(R.id.action_loginFragment_to_stagesFragment)
                        } else {
                            Toast.makeText(context, "لم يتم تأكيد بريدك الإلكتروني", Toast.LENGTH_SHORT).show()
                            verifyEmail()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.w(task.exception, "signInWithEmail:failure")
                        Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun verifyEmail() {
        binding.verifyEmailButton.visibility = View.VISIBLE
        binding.verifyEmailButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                it.sendEmailVerification().addOnSuccessListener {
                    Toast.makeText(activity, "راجع بريدك الإلكتروني، تم إرسال رسالة تأكيد", Toast.LENGTH_SHORT).show()
                }
                    .addOnFailureListener{
                        Timber.d("failed to send email verification")
                    }
            }
        }
    }

    private fun resetPassword(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTextInput.error = "من فضلك أدخل بريد إلكتروني صحيح"
            binding.emailTextInput.requestFocus()
        } else {
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Password Reset Email sent.")
                        Toast.makeText(context, "راجع بريدك الإلكتروني، تم إرسال رابط لإعادة ضبط كلمة السر", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Timber.d("Password Reset Email fail to send.")
                        Toast.makeText(context, "تأكد من كتابة بريدك الإلكتروني بشكل صحيح", Toast.LENGTH_SHORT).show()
                    }
                    }
                }
        }
    }
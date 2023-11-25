package com.ftg.famasale.Screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.famasale.Models.LoginCred
import com.ftg.famasale.Models.LoginResponse
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant.BASE_URL
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentLoginPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class LoginPage : Fragment() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var bind: FragmentLoginPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentLoginPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())
        setActionListeners()
        return bind.root
    }

    private fun setActionListeners() {
        bind.login.setOnClickListener {
            val username = bind.username.text.toString()
            val password = bind.password.text.toString()
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter Login credentials",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        loadingDialog.startLoading()

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Server::class.java)

        val response = retrofit.login(LoginCred(password, username))
        response.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loadingDialog.stopLoading()
                if (response.code() == 200) {
                    val result = response.body()
                    if (result?.token.isNullOrBlank()) {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong, try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        sharedPrefManager.saveToken(result?.token)
                        sharedPrefManager.saveUserDetails(result?.data)
                        Toast.makeText(
                            requireContext(),
                            "Logged In successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.enter,
                                R.anim.exit,
                                R.anim.pop_enter,
                                R.anim.pop_exit
                            )
                            .replace(R.id.container, DashboardPage()).commit()
                    }
                } else {
                    val error =
                        Gson().fromJson(response.errorBody()?.string(), LoginResponse::class.java)
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "Something went wrong\nTry later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    login(username, password)
                } else if (call.isCanceled) {
                    login(username, password)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(
                        requireContext(),
                        t.localizedMessage ?: "Something went wrong\nTry later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        })
    }
}
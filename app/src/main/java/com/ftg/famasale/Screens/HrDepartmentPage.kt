package com.ftg.famasale.Screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.ftg.famasale.Models.GetAllVacanciesResponse
import com.ftg.famasale.Models.JobDetail
import com.ftg.famasale.Models.RegisterCandidateResponse
import com.ftg.famasale.R
import com.ftg.famasale.Utils.Constant
import com.ftg.famasale.Utils.LoadingDialog
import com.ftg.famasale.Utils.Server
import com.ftg.famasale.Utils.ServerCallInterceptor
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentHrDepartmentPageBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class HrDepartmentPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor

    private lateinit var bind: FragmentHrDepartmentPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server

    private var jobs = ArrayList<JobDetail>()
    private var jobNames = ArrayList<String>()
    private var genders = arrayListOf("Male", "Female")
    private var qualifications = arrayListOf("High-School", "Intermediate", "Graduate", "Post-Graduate")
    private var profilePictureFile: MultipartBody.Part? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentHrDepartmentPageBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireActivity())

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(Constant.BASE_URL)
            .build()
            .create(Server::class.java)

        getAllJobs()
        setActionListeners()

        return bind.root
    }

    private fun getAllJobs(){
        loadingDialog.startLoading()

        val response = server.getAllVacancies()
        response.enqueue(object : Callback<GetAllVacanciesResponse>{
            override fun onResponse(
                call: Call<GetAllVacanciesResponse>,
                response: Response<GetAllVacanciesResponse>
            ) {
                loadingDialog.startLoading()
                if(response.code()==200){
                    val result = response.body()?.data
                    if(!result.isNullOrEmpty()){
                        jobs = result as ArrayList<JobDetail>
                        jobNames = result.map { it.job_name ?: it.job_description.toString()} as ArrayList<String>
                    }
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_text_list_item, jobNames.toTypedArray())
                    bind.vecancy.setAdapter(adapter)
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), GetAllVacanciesResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetAllVacanciesResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    getAllJobs()
                } else if (call.isCanceled) {
                    getAllJobs()
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setActionListeners(){
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_text_list_item, genders.toTypedArray())
        bind.candidateGender.setAdapter(adapter)

        bind.candidateImage.setOnClickListener {
            checkPermissions()
        }

        bind.registerCandidateButton.setOnClickListener {
            val name = bind.candidateName.text.toString()
            val mobile = bind.candidateMobile.text.toString()
            val address = bind.candidateAddress.text.toString()
            val email = bind.candidateEmail.text.toString()
            val gender = bind.candidateGender.text.toString()
            val qualification = bind.candidateQualification.text.toString()
            val jobName = bind.vecancy.text.toString()

            var jobId = ""
            try{
                jobId = jobs.filter { it.job_name?.contains(jobName, true) == true || it.job_description?.toString()?.contains(jobName, true) == true }[0].job_id.toString()
            }catch (e: Exception){
                Log.d("XYZXYZ", e.localizedMessage?.toString() ?: "")
            }

            if(profilePictureFile==null)
                Toast.makeText(requireContext(), "Please select candidate photo", Toast.LENGTH_SHORT).show()
            else if(name.isBlank())
                Toast.makeText(requireContext(), "Please fill candidate name", Toast.LENGTH_SHORT).show()
            else if(mobile.isBlank())
                Toast.makeText(requireContext(), "Please fill candidate contact number", Toast.LENGTH_SHORT).show()
            else if(address.isBlank())
                Toast.makeText(requireContext(), "Please fill candidate address", Toast.LENGTH_SHORT).show()
            else if(email.isBlank())
                Toast.makeText(requireContext(), "Please fill candidate email address", Toast.LENGTH_SHORT).show()
            else if(!genders.contains(gender))
                Toast.makeText(requireContext(), "Please select Gender", Toast.LENGTH_SHORT).show()
            else if(!qualifications.contains(qualification))
                Toast.makeText(requireContext(), "Please select candidate qualification", Toast.LENGTH_SHORT).show()
            else if(!jobNames.contains(jobName))
                Toast.makeText(requireContext(), "Please select valid vacancy", Toast.LENGTH_SHORT).show()
            else{
                val nameBody = name.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val mobileBody = mobile.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val addressBody = address.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val jobBody = jobId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val genderBody = gender.lowercase().toRequestBody("multipart/form-data".toMediaTypeOrNull())
                val qualificationBody = qualification.lowercase().toRequestBody("multipart/form-data".toMediaTypeOrNull())
                registerCandidate(nameBody, mobileBody, addressBody, emailBody, genderBody, qualificationBody, jobBody)
            }
        }
    }

    private fun registerCandidate(
        nameBody: RequestBody,
        mobileBody: RequestBody,
        addressBody: RequestBody,
        emailBody: RequestBody,
        genderBody: RequestBody,
        qualificationBody: RequestBody,
        jobBody: RequestBody
    ) {
        loadingDialog.startLoading()
        val response = server.registerCandidateForJob(
            profilePictureFile!!,
            nameBody,
            addressBody,
            emailBody,
            mobileBody,
            genderBody,
            qualificationBody,
            jobBody
        )

        response.enqueue(object: Callback<RegisterCandidateResponse>{
            override fun onResponse(
                call: Call<RegisterCandidateResponse>,
                response: Response<RegisterCandidateResponse>
            ) {
                loadingDialog.stopLoading()
                if(response.code()==201){
                    Toast.makeText(requireContext(), "Candidate registered successfully", Toast.LENGTH_SHORT).show()
                    bind.vecancy.setText("")
                    bind.candidateName.setText("")
                    bind.candidateAddress.setText("")
                    bind.candidateMobile.setText("")
                    bind.candidateEmail.setText("")
                    bind.candidateGender.setText("")
                    bind.candidateQualification.setText("")
                }else{
                    val error = Gson().fromJson(response.errorBody()?.string(), RegisterCandidateResponse::class.java)
                    Toast.makeText(requireContext(), error.message ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterCandidateResponse>, t: Throwable) {
                if (t is SocketTimeoutException) {
                    registerCandidate(nameBody, mobileBody, addressBody, emailBody, genderBody, qualificationBody, jobBody)
                } else if (call.isCanceled) {
                    registerCandidate(nameBody, mobileBody, addressBody, emailBody, genderBody, qualificationBody, jobBody)
                } else {
                    loadingDialog.stopLoading()
                    Toast.makeText(requireContext(), t.localizedMessage ?: "Something went wrong\nTry later", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun checkPermissions(){
        val permission = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            Manifest.permission.READ_EXTERNAL_STORAGE
        }else{
            Manifest.permission.READ_MEDIA_IMAGES
        }

        if (requireActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(requireContext(), "Permissions not Granted\nPlease allow from device settings", Toast.LENGTH_LONG).show()
        }else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getImageFromGallery.launch(intent)
        }
    }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val file = File(requireActivity().applicationContext.filesDir, "profile.png")

                    val inputStream = requireActivity().contentResolver.openInputStream(result.data?.data!!)
                    val outputStream = FileOutputStream(file)
                    inputStream!!.copyTo(outputStream)

                    val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                    profilePictureFile = MultipartBody.Part.createFormData("profile", file.name, requestFile)
                    bind.candidateImage.setImageURI(result?.data?.data)
                    Toast.makeText(requireContext(), "Candidate Picture Selected", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.localizedMessage ?: "Something went wrong", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
}
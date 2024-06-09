package com.ftg.famasale.Screens

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.content.FileProvider
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
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.Buffer
import javax.inject.Inject

@AndroidEntryPoint
class HrDepartmentPage : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    @Inject
    lateinit var interceptor: ServerCallInterceptor
    private var idPhotoUri: Uri? = null
    private lateinit var bind: FragmentHrDepartmentPageBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var server: Server
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        val file = File(requireActivity().applicationContext.filesDir, "camera_photo.png")
        val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
        profilePictureFile = MultipartBody.Part.createFormData("profile", file.name, requestFile)
        bind.candidateImage.setImageURI(imageUri)  // Use imageUri here
        Toast.makeText(requireContext(), "Candidate Picture Captured", Toast.LENGTH_SHORT).show()
    }
    private var jobs = ArrayList<JobDetail>()
    private var jobNames = ArrayList<String>()
    private var genders = arrayListOf("Male", "Female")
    private var qualifications = arrayListOf("5","8","10","12", "graduation", "post-graduation")
    private var profilePictureFile: MultipartBody.Part? = null
    lateinit var imageUri : Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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

        val response = server.getAllVacancies()
        response.enqueue(object : Callback<GetAllVacanciesResponse>{
            override fun onResponse(
                call: Call<GetAllVacanciesResponse>,
                response: Response<GetAllVacanciesResponse>
            ) {

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
        val qualiAda = ArrayAdapter(requireContext(),R.layout.simple_text_list_item,qualifications.toTypedArray())
        bind.candidateQualification.setAdapter(qualiAda)
        bind.candidateGender.setAdapter(adapter)
        bind.candidateImage.setOnClickListener {
            showImagePickerOptions()
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

    private fun showToast(message: String) {
        Log.d("TOAST_MESSAGE", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        Log.d("API_REQUEST", "Name: ${nameBody.convertToString()}")
        Log.d("API_REQUEST", "Mobile: ${mobileBody.convertToString()}")
        Log.d("API_REQUEST", "Address: ${addressBody.convertToString()}")
        Log.d("API_REQUEST", "Email: ${emailBody.convertToString()}")
        Log.d("API_REQUEST", "Gender: ${genderBody.convertToString()}")
        Log.d("API_REQUEST", "Qualification: ${qualificationBody.convertToString()}")
        Log.d("API_REQUEST", "Job: ${jobBody.convertToString()}")

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

        response.enqueue(object : Callback<RegisterCandidateResponse> {
            override fun onResponse(
                call: Call<RegisterCandidateResponse>,
                response: Response<RegisterCandidateResponse>
            ) {
                loadingDialog.stopLoading()
                if (response.code() == 201) {
                    showToast("Candidate registered successfully")
                    bind.vecancy.setText("")
                    bind.candidateName.setText("")
                    bind.candidateAddress.setText("")
                    bind.candidateMobile.setText("")
                    bind.candidateEmail.setText("")
                    bind.candidateGender.setText("")
                    bind.candidateQualification.setText("")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("error", "Error Response Body: $errorBody")

                    val error = Gson().fromJson(errorBody, RegisterCandidateResponse::class.java)
                    showToast(error.message ?: "Something went wrong\nTry later")
                }
            }

            override fun onFailure(call: Call<RegisterCandidateResponse>, t: Throwable) {
                if (t is SocketTimeoutException || call.isCanceled) {
                    showToast("Request failed, retrying...")
                    registerCandidate(
                        nameBody, mobileBody, addressBody, emailBody, genderBody, qualificationBody, jobBody
                    )
                } else {
                    loadingDialog.stopLoading()
                    showToast(t.localizedMessage ?: "Something went wrong\nTry later")
                }
            }
        })
    }

    private fun showImagePickerOptions() {
        imageUri = createImageUri()!!
        val options = arrayOf("Choose from Gallery", "Take Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkPermissions()
                    1 -> contract.launch(imageUri)
                }
            }
            .show()
    }
    fun RequestBody.convertToString(): String {
        val buffer = okio.Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
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
    private fun createImageUri(): Uri? {
        val fileName = "camera_photo.png"
        val image = File(requireContext().applicationContext.filesDir, fileName)

        try {
            if (!image.exists()) {
                if (!image.createNewFile()) {
                    throw IOException("Failed to create the file: $fileName")
                }
            }
            idPhotoUri = FileProvider.getUriForFile(
                requireContext().applicationContext,
                "com.ftg.famasale.fileProvider",
                image
            )
            Log.d("ID_PHOTO_URI", "ID Photo URI: $idPhotoUri")
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return idPhotoUri
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
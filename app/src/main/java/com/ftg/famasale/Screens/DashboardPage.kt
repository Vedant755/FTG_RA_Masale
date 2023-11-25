package com.ftg.famasale.Screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.famasale.R
import com.ftg.famasale.Utils.SharedPrefManager
import com.ftg.famasale.databinding.FragmentDashboardPageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardPage : Fragment() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var bind: FragmentDashboardPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDashboardPageBinding.inflate(inflater, container, false)
        setActionListeners()
        return bind.root
    }

    private fun setActionListeners() {
        bind.employees.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.container, EmployeesPage()).addToBackStack(null).commit()
        }

        bind.dispatch.setOnClickListener{
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
//                .replace(R.id.container, DispatchPage()).addToBackStack(null).commit()
            Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        bind.hrDepartment.setOnClickListener{
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
//                .replace(R.id.container, HrDepartmentPage()).addToBackStack(null).commit()
            Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        bind.visitors.setOnClickListener{
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
//                .replace(R.id.container, VisitorsPage()).addToBackStack(null).commit()
            Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        bind.rawMaterial.setOnClickListener{
//            requireActivity().supportFragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
//                .replace(R.id.container, RawMaterialPage()).addToBackStack(null).commit()
            Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        bind.vehicles.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .replace(R.id.container, VehiclesPage()).addToBackStack(null).commit()
        }

        bind.logout.setOnClickListener {
            sharedPrefManager.saveToken(null)
            requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

}
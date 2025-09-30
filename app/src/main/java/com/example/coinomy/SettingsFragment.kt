package com.example.coinomy

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.coinomy.databinding.DialogEditProfileBinding
import com.example.coinomy.databinding.FragmentSettingsBinding
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("coinomy_prefs", Context.MODE_PRIVATE)

        // Set up preferences fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsPreferenceFragment())
            .commit()

        // Load user profile data
        loadUserProfile()

        // Set up edit profile button
        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun loadUserProfile() {
        val name = sharedPreferences.getString("user_name", "User Name") ?: "User Name"
        val email = sharedPreferences.getString("user_email", "email@example.com") ?: "email@example.com"
        val joinDate = sharedPreferences.getString("user_join_date", "January 2024") ?: "January 2024"

        binding.profileName.text = name
        binding.profileEmail.text = email
        binding.profileJoinDate.text = "Member since $joinDate"
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Pre-populate fields with existing data
        dialogBinding.nameInput.setText(sharedPreferences.getString("user_name", ""))
        dialogBinding.emailInput.setText(sharedPreferences.getString("user_email", ""))

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.saveButton.setOnClickListener {
            val name = dialogBinding.nameInput.text.toString().trim()
            val email = dialogBinding.emailInput.text.toString().trim()

            // Validate inputs
            var isValid = true

            if (name.isEmpty()) {
                dialogBinding.nameInputLayout.error = "Name cannot be empty"
                isValid = false
            } else {
                dialogBinding.nameInputLayout.error = null
            }

            if (email.isNotEmpty() && !isValidEmail(email)) {
                dialogBinding.emailInputLayout.error = "Please enter a valid email"
                isValid = false
            } else {
                dialogBinding.emailInputLayout.error = null
            }

            if (isValid) {
                // Save to SharedPreferences
                sharedPreferences.edit().apply {
                    putString("user_name", name)
                    putString("user_email", email)
                    // Only set join date if it's not already set
                    if (!sharedPreferences.contains("user_join_date")) {
                        val currentDate = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date())
                        putString("user_join_date", currentDate)
                    }
                    apply()
                }

                // Update UI
                loadUserProfile()

                dialog.dismiss()
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SettingsPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var sharedPreferences: SharedPreferences
        private val gson = Gson()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            sharedPreferences = requireActivity().getSharedPreferences("coinomy_prefs", Context.MODE_PRIVATE)

            // Set up preference click listeners
            setupBackupRestorePreferences()
            setupLogoutPreference()
        }

        private fun setupBackupRestorePreferences() {
            findPreference<Preference>("backup_data")?.setOnPreferenceClickListener {
                showBackupDialog()
                true
            }

            findPreference<Preference>("restore_data")?.setOnPreferenceClickListener {
                showRestoreDialog()
                true
            }
        }

        private fun showBackupDialog() {
            AlertDialog.Builder(requireContext())
                .setTitle("Backup Data")
                .setMessage("This will create a backup of all your data including transactions, settings, and preferences. Continue?")
                .setPositiveButton("Backup") { _, _ ->
                    backupUserData()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showRestoreDialog() {
            AlertDialog.Builder(requireContext())
                .setTitle("Restore Data")
                .setMessage("This will restore all your data from the last backup. Any current data will be replaced. Continue?")
                .setPositiveButton("Restore") { _, _ ->
                    restoreUserData()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun backupUserData() {
            try {
                val backupDir = File(requireContext().filesDir, "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val timestamp = System.currentTimeMillis()
                val backupFile = File(backupDir, "coinomy_backup_$timestamp.zip")

                // Create ZIP file
                FileOutputStream(backupFile).use { fos ->
                    ZipOutputStream(fos).use { zos ->
                        // Backup preferences
                        val prefsFile = File(requireContext().filesDir, "preferences.json")
                        val prefsData = mapOf(
                            "user_name" to sharedPreferences.getString("user_name", ""),
                            "user_email" to sharedPreferences.getString("user_email", ""),
                            "currency" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getString("currency", "LKR"),
                            "theme_mode" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getBoolean("theme_mode", false).toString(),
                            "notifications" to PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getBoolean("notifications", true).toString(),
                            "monthly_budget" to sharedPreferences.getFloat("monthly_budget", 2500f)
                        )

                        zos.putNextEntry(ZipEntry("preferences.json"))
                        zos.write(gson.toJson(prefsData).toByteArray())
                        zos.closeEntry()

                        // Backup transactions
                        val transactionsFile = File(requireContext().filesDir, "transactions.json")
                        if (transactionsFile.exists()) {
                            zos.putNextEntry(ZipEntry("transactions.json"))
                            FileInputStream(transactionsFile).use { fis ->
                                fis.copyTo(zos)
                            }
                            zos.closeEntry()
                        }
                    }
                }

                Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error creating backup: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        private fun restoreUserData() {
            try {
                val backupDir = File(requireContext().filesDir, "backups")
                if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
                    Toast.makeText(context, "No backup found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Get the most recent backup
                val backupFile = backupDir.listFiles()?.maxByOrNull { it.lastModified() }
                    ?: throw IOException("No backup file found")

                // Extract and restore data
                FileInputStream(backupFile).use { fis ->
                    ZipInputStream(fis).use { zis ->
                        var entry: ZipEntry?
                        while (zis.nextEntry.also { entry = it } != null) {
                            when (entry?.name) {
                                "preferences.json" -> {
                                    val json = String(zis.readBytes())
                                    val prefsData = gson.fromJson(json, Map::class.java)

                                    // Restore preferences
                                    sharedPreferences.edit().apply {
                                        putString("user_name", prefsData["user_name"] as? String)
                                        putString("user_email", prefsData["user_email"] as? String)
                                        putFloat("monthly_budget", (prefsData["monthly_budget"] as? Number)?.toFloat() ?: 2500f)
                                        apply()
                                    }

                                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().apply {
                                        putString("currency", prefsData["currency"] as? String ?: "LKR")
                                        putBoolean("theme_mode", (prefsData["theme_mode"] as? String)?.toBoolean() ?: false)
                                        putBoolean("notifications", (prefsData["notifications"] as? String)?.toBoolean() ?: true)
                                        apply()
                                    }
                                }
                                "transactions.json" -> {
                                    val transactionsFile = File(requireContext().filesDir, "transactions.json")
                                    FileOutputStream(transactionsFile).use { fos ->
                                        zis.copyTo(fos)
                                    }
                                }
                            }
                            zis.closeEntry()
                        }
                    }
                }

                // Update UI
                (parentFragment as? SettingsFragment)?.loadUserProfile()

                Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Error restoring data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        private fun setupLogoutPreference() {
            findPreference<Preference>("logout")?.setOnPreferenceClickListener {
                showLogoutDialog()
                true
            }
        }

        private fun showLogoutDialog() {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    // Clear user data
                    sharedPreferences.edit().clear().apply()
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().clear().apply()

                    // Clear transaction data
                    File(requireContext().filesDir, "transactions.json").delete()

                    // Navigate to login screen
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

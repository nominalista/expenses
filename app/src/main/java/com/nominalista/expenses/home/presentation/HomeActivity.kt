package com.nominalista.expenses.home.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nominalista.expenses.R
import com.nominalista.expenses.common.presentation.BaseActivity
import com.nominalista.expenses.onboarding.OnboardingActivity
import com.nominalista.expenses.settings.presentation.SettingsActivity
import com.nominalista.expenses.about.AboutActivity
import com.nominalista.expenses.util.extensions.application
import com.nominalista.expenses.util.extensions.plusAssign
import com.nominalista.expenses.util.extensions.startActivitySafely
import com.nominalista.expenses.util.isGranted
import com.nominalista.expenses.util.isPermissionGranted
import com.nominalista.expenses.util.runOnUiThread
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.include_drawer.*
import java.util.*

class HomeActivity : BaseActivity() {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var model: HomeActivityModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupModel()
        setupToolbar()
        setupHeaderLayout()
        setupItemLayouts()
        setupBannerLayout()
        bindModel()
    }

    private fun setupModel() {
        val factory = HomeActivityModel.Factory(applicationContext.application)
        model = ViewModelProviders.of(this, factory).get(HomeActivityModel::class.java)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
    }

    private fun setupHeaderLayout() {
        signUpOrSignInButton.setOnClickListener {
            runAfterDrawerClose { model.navigateToOnboardingRequested() }
        }
    }

    private fun setupItemLayouts() {
        importExpensesItemLayout.setOnClickListener {
            runAfterDrawerClose { model.importExpensesRequested() }
        }
        exportExpensesItemLayout.setOnClickListener {
            runAfterDrawerClose { model.exportExpensesRequested() }
        }
        settingsItemLayout.setOnClickListener {
            runAfterDrawerClose { model.navigateToSettingsRequested() }
        }
        supportItemLayout.setOnClickListener {
            runAfterDrawerClose { model.navigateToSupportRequested() }
        }
    }

    private fun setupBannerLayout() {
        bannerLayout.setOnClickListener {
            runAfterDrawerClose { model.performBannerActionRequested() }
        }
    }

    private fun runAfterDrawerClose(block: () -> Unit) {
        drawerLayout.closeDrawer(GravityCompat.START)
        runOnUiThread(DRAWER_CLOSE_DELAY) { block() }
    }

    private fun bindModel() {
        compositeDisposable += model.isUserSignedIn
            .subscribe { configureHeaderLayout(it) }
        compositeDisposable += model.userName
            .subscribe { configureUserNameTextView(it) }
        compositeDisposable += model.userEmail
            .subscribe { configureUserEmailTextView(it) }
        compositeDisposable += model.isBannerEnabled
            .subscribe { configureBannerLayout(it) }
        compositeDisposable += model.bannerTitle
            .subscribe { configureBannerTitle(it) }
        compositeDisposable += model.bannerSubtitle
            .subscribe { configureBannerSubtitle(it) }

        compositeDisposable += model.navigateToOnboarding
            .subscribe { navigateToOnboarding() }
        compositeDisposable += model.selectFileForImport
            .subscribe { selectFileForImport() }
        compositeDisposable += model.showExpenseImportFailureDialog
            .subscribe { showExpenseImportFailureDialog() }
        compositeDisposable += model.requestExportPermissions
            .subscribe { requestExportPermissions() }
        compositeDisposable += model.showExpenseExportFailureDialog
            .subscribe { showExpenseExportFailureDialog() }
        compositeDisposable += model.navigateToSettings
            .subscribe { navigateToSettings() }
        compositeDisposable += model.navigateToSupport
            .subscribe { navigateToSupport() }

        compositeDisposable += model.showMessage
            .subscribe { showMessage(it) }
        compositeDisposable += model.showActivity
            .subscribe { showActivity(it) }
        compositeDisposable += model.observeWorkInfo
            .subscribe { observeWorkInfo(it) }
    }

    private fun configureHeaderLayout(isUserSignedIn: Boolean) {
        userNameTextView.isVisible = isUserSignedIn
        userEmailTextView.isVisible = isUserSignedIn

        signUpOrSignInButton.isVisible = !isUserSignedIn
    }

    private fun configureUserNameTextView(userName: String) {
        userNameTextView.text = userName
    }

    private fun configureUserEmailTextView(userEmail: String) {
        userEmailTextView.text = userEmail
    }

    private fun configureBannerLayout(isBannerEnabled: Boolean) {
        bannerLayout.isVisible = isBannerEnabled
    }

    private fun configureBannerTitle(bannerTitle: String) {
        bannerTitleTextView.text = bannerTitle
    }

    private fun configureBannerSubtitle(bannerSubtitle: String) {
        bannerSubtitleTextView.text = bannerSubtitle
    }

    private fun navigateToOnboarding() {
        OnboardingActivity.start(this)
        finishAffinity()
    }

    private fun selectFileForImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(XLS_MIME_TYPE))
            type = "*/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE_FOR_IMPORT)
    }

    private fun showExpenseImportFailureDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.expense_import_failure_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setNeutralButton(R.string.download_template) { _, _ -> model.downloadTemplate() }
            .create()
            .show()
    }

    private fun requestExportPermissions() {
        if (isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            model.exportPermissionsGranted()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_FOR_EXPORT
                )
            }
        }
    }

    private fun showExpenseExportFailureDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.expense_export_failure_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .create()
            .show()
    }

    private fun navigateToSettings() {
        SettingsActivity.start(this)
    }

    private fun navigateToSupport() {
        AboutActivity.start(this)
    }

    private fun showMessage(messageId: Int) {
        Snackbar.make(containerLayout, messageId, Snackbar.LENGTH_LONG).show()
    }

    private fun showActivity(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivitySafely(intent)
    }

    private fun observeWorkInfo(id: UUID) {
        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(id)
            .observe(this, Observer { model.handleWorkInfo(it) })
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindModel()
    }

    private fun unbindModel() {
        compositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CODE_SELECT_FILE_FOR_IMPORT -> {
                data?.data?.let { model.fileForImportSelected(it) }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (isGranted(grantResults)) model.exportPermissionsGranted()
    }

    companion object {

        private const val DRAWER_CLOSE_DELAY = 300L
        private const val REQUEST_CODE_SELECT_FILE_FOR_IMPORT = 1
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_FOR_EXPORT = 2
        private const val XLS_MIME_TYPE = "application/vnd.ms-excel"

        fun start(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}
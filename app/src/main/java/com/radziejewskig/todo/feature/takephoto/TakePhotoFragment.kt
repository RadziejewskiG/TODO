package com.radziejewskig.todo.feature.takephoto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import com.radziejewskig.todo.R
import com.radziejewskig.todo.appComponent
import com.radziejewskig.todo.base.BaseFragment
import com.radziejewskig.todo.databinding.FragmentTakePhotoBinding
import com.radziejewskig.todo.extension.*
import com.radziejewskig.todo.feature.addedit.AddEditFragment.Companion.IMAGE_URL_ARG
import com.radziejewskig.todo.utils.FileUtils
import com.radziejewskig.todo.utils.viewBinding
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class TakePhotoFragment: BaseFragment<TakePhotoState, TakePhotoEvent>(R.layout.fragment_take_photo) {

    override val binding by viewBinding(FragmentTakePhotoBinding::bind)

    override val viewModel: TakePhotoFragmentViewModel by viewModels()

    @Inject
    lateinit var fileUtils: FileUtils

    override fun injectDaggerDependencies() {
        appComponent.inject(this)
    }

    override var isStatusBarLight = false

    private var imageUri: Uri? = null

    // ACTION_IMAGE_CAPTURE - no camera permissions needed
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { imageTaken: Boolean ->
        if(imageTaken) {
            tryGetBitmapAndAttachToImv()
            imageUri = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cropImageView.setAspectRatio(1, 1)
            cropImageView.setFixedAspectRatio(false)
            cropImageView.guidelines = CropImageView.Guidelines.ON_TOUCH
            cropImageView.cropShape = CropImageView.CropShape.RECTANGLE
            cropImageView.isAutoZoomEnabled = true

            btnAccept.isEnabled = false
            icRotate.setTint(R.color.white_alpha_40)
            icRotate.isEnabled = false

            icRotate.setOnClickListener {
                binding.cropImageView.rotateImage(90)
            }

            icTakePhoto.setOnClickListener {
                tryOpenCamera()
            }

            btnAccept.setOnClickListener {
                showLoadingDialog()
                viewModel.saveImage(binding.cropImageView.croppedImage)
            }

            viewModel.state.collectLatestWhenStartedAutoCancelling(viewLifecycleOwner) { state ->
                tvDesc.isVisible = state.currentPhotoPath.isEmpty()
            }
        }

        // Get the photo by path and assign to imv if it was already saved
        tryGetBitmapAndAttachToImv()
    }

    override fun handleSingleEvent(event: TakePhotoEvent?) {
        when(event) {
            is TakePhotoEvent.ImageSaved -> {
                popWithData(
                    IMAGE_URL_ARG,
                    event.imageUrl
                )
            }
        }
    }

    private fun tryGetBitmapAndAttachToImv() = launchLifecycleScopeWhenStarted {
        withIo {
            BitmapFactory.decodeFile(currentState.currentPhotoPath)?.also { bitmap ->
                val exif = ExifInterface(currentState.currentPhotoPath)
                withMain {
                    attachNewBitmapToImv(bitmap, exif)
                }
            }
        }
    }

    private fun tryOpenCamera() = try {
        fileUtils.createImageFile()?.also { file ->
            viewModel.assignNewPhotoPath(file.absolutePath)
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                CAPTURE_PHOTO_FILE_PROVIDER_URL,
                file
            )
            takePicture.launch(imageUri)
        }
    } catch (e: Exception) { }

    private fun attachNewBitmapToImv(bitmap: Bitmap?, exifInterface: ExifInterface?) {
        binding.btnAccept.isEnabled = bitmap != null

        binding.icRotate.isEnabled = bitmap != null
        binding.icRotate.setTint(if (bitmap == null) R.color.white_alpha_40 else R.color.white)

        binding.cropImageView.setImageBitmap(bitmap, exifInterface)
    }

    override fun windowInsetsChanged(navigationBarHeight: Int, statusBarHeight: Int) {
        super.windowInsetsChanged(navigationBarHeight, statusBarHeight)
        binding.run {
            container.updatePadding(bottom = navigationBarHeight)
            topView.updatePadding(top = statusBarHeight + resources.getDimensionPixelSize(R.dimen.dp16))
        }
    }

    companion object {
        const val CAPTURE_PHOTO_FILE_PROVIDER_URL: String = "com.radziejewskig.todo.fileprovider"
    }

}

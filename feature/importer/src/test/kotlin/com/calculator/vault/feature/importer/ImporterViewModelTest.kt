package com.calculator.vault.feature.importer

import app.cash.turbine.test
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.feature.importer.domain.usecase.LoadDeviceAlbumsUseCase
import com.calculator.vault.feature.importer.domain.usecase.MediaAccessUseCase
import com.calculator.vault.feature.importer.presentation.mvi.ImporterEffect
import com.calculator.vault.feature.importer.presentation.mvi.ImporterIntent
import com.calculator.vault.feature.importer.presentation.mvi.ImporterViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImporterViewModelTest {

    private val videoPermission = listOf("android.permission.READ_MEDIA_VIDEO")
    private var hasAccess = false

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    /** A device with no videos at all: every load comes back empty. */
    private fun viewModel(permissions: List<String> = videoPermission) = ImporterViewModel(
        type = MediaType.VIDEO,
        loadAlbums = mockk<LoadDeviceAlbumsUseCase> { coEvery { this@mockk.invoke(any()) } returns emptyList() },
        resolveDocuments = mockk(relaxed = true),
        mediaAccess = mockk<MediaAccessUseCase> {
            every { hasAccess(any()) } answers { hasAccess }
            every { permissions(any()) } returns permissions
        },
        hideMedia = mockk(relaxed = true),
    )

    @Test fun grantedButEmptyNeverAsks() = runTest {
        hasAccess = true
        val vm = viewModel()
        vm.effects.test { expectNoEvents() }
        with(vm.state.value) {
            assertFalse(needsPermission)
            assertFalse(loading)
            assertTrue(albums.isEmpty())
        }
    }

    @Test fun missingAccessAsksOnceThenOnlyFromTheButton() = runTest {
        val vm = viewModel()
        vm.effects.test {
            assertEquals(ImporterEffect.RequestPermission(videoPermission), awaitItem())
            vm.onIntent(ImporterIntent.PermissionResult(permanentlyDenied = false))
            expectNoEvents()
            vm.onIntent(ImporterIntent.AllowAccess)
            assertEquals(ImporterEffect.RequestPermission(videoPermission), awaitItem())
            vm.onIntent(ImporterIntent.PermissionResult(permanentlyDenied = true))
            expectNoEvents()
            vm.onIntent(ImporterIntent.AllowAccess)
            assertEquals(ImporterEffect.OpenAppSettings, awaitItem())
        }
        assertTrue(vm.state.value.needsPermission)
    }

    @Test fun documentsNeverLeaveForSettingsUnasked() = runTest {
        val vm = viewModel(permissions = emptyList())
        vm.effects.test {
            expectNoEvents()
            vm.onIntent(ImporterIntent.AllowAccess)
            assertEquals(ImporterEffect.OpenAllFilesSettings, awaitItem())
        }
    }

    @Test fun resumingWithNewAccessReloads() = runTest {
        val vm = viewModel()
        vm.effects.test {
            awaitItem()
            hasAccess = true
            vm.onIntent(ImporterIntent.Resumed)
            expectNoEvents()
        }
        assertFalse(vm.state.value.needsPermission)
    }
}

package com.lukasz.witkowski.training.planner.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultAuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.AndroidSecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.DefaultTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationClient
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AndroidAuthenticationRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var context: Context
    private lateinit var realSecureTokenStorage: AndroidSecureTokenStorage
    private lateinit var repository: DefaultAuthenticationRepository

    private val timeProvider = TestTimeProvider()

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        realSecureTokenStorage = AndroidSecureTokenStorage(context)
        realSecureTokenStorage.clear()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/").toString()
        val api = RetrofitAuthenticationClient.createAuthenticationApi(baseUrl = baseUrl)
        val remoteDataSource = RetrofitAuthenticationRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = Dispatchers.IO,
        )
        val tokenStorage = DefaultTokenStorage(realSecureTokenStorage)

        repository = DefaultAuthenticationRepository(
            tokenStorage = tokenStorage,
            remoteDataSource = remoteDataSource,
        )
    }

    @After
    fun tearDown() = runTest {
        realSecureTokenStorage.clear()
        mockWebServer.shutdown()
    }

    @Test
    fun signInPersistsEncryptedRefreshTokenInRealDataStore() = runTest {
        val jsonResponse = TestResourceReader.readJson("login_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = repository.signIn(SignInForm("test@example.com", "password123"))

        assertEquals(AuthenticationResult.Success, result)

        val persistedRefreshToken = realSecureTokenStorage.getRefreshToken()
        assertEquals("refresh_token_xyz", persistedRefreshToken)
    }

    @Test
    fun refreshAccessTokenRetrievesPersistedTokenFromRealDataStoreAndUpdatesIt() = runTest {
        realSecureTokenStorage.saveRefreshToken("old_refresh_token_xyz")

        val jsonResponse = TestResourceReader.readJson("refresh_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = repository.refreshAccessToken()

        assertEquals(AuthenticationResult.Success, result)

        val updatedRefreshToken = realSecureTokenStorage.getRefreshToken()
        assertEquals("new_refresh_token_999", updatedRefreshToken)
    }

    @Test
    fun logOutClearsRefreshTokenFromRealDataStore() = runTest {
        realSecureTokenStorage.saveRefreshToken("test_refresh_token")

        val result = repository.logOut()

        assertEquals(AuthenticationResult.Success, result)
        assertNull(realSecureTokenStorage.getRefreshToken())
    }

    @Test
    fun processDeathRecoveryRefreshesTokensUsingPersistedRefreshToken() = runTest {
        realSecureTokenStorage.saveRefreshToken("persisted_refresh_token")

        // Re-instantiate repository to simulate app restart with empty in-memory access token
        val newStorage = DefaultTokenStorage(realSecureTokenStorage)
        val baseUrl = mockWebServer.url("/").toString()
        val api = RetrofitAuthenticationClient.createAuthenticationApi(baseUrl = baseUrl)
        val remoteDataSource = RetrofitAuthenticationRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = Dispatchers.IO,
        )
        val newRepository = DefaultAuthenticationRepository(newStorage, remoteDataSource)

        val jsonResponse = TestResourceReader.readJson("refresh_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = newRepository.refreshAccessToken()

        assertEquals(AuthenticationResult.Success, result)
        assertEquals("new_refresh_token_999", realSecureTokenStorage.getRefreshToken())
    }
}

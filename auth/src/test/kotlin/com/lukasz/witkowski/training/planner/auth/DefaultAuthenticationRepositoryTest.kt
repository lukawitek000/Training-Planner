package com.lukasz.witkowski.training.planner.auth

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.DefaultAuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.DefaultTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.SecureTokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationClient
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.RetrofitAuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.time.TestTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthenticationRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private val mockSecureTokenStorage = mockk<SecureTokenStorage>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val timeProvider = TestTimeProvider()

    private lateinit var repository: DefaultAuthenticationRepository

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/").toString()
        val api = RetrofitAuthenticationClient.createAuthenticationApi(baseUrl = baseUrl)
        val remoteDataSource = RetrofitAuthenticationRemoteDataSource(
            api = api,
            timeProvider = timeProvider,
            ioDispatcher = testDispatcher,
        )
        val tokenStorage = DefaultTokenStorage(mockSecureTokenStorage)

        repository = DefaultAuthenticationRepository(
            tokenStorage = tokenStorage,
            remoteDataSource = remoteDataSource,
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `signIn returns Success when server returns 200 OK and saves refresh token`() = runTest {
        val jsonResponse = TestResourceReader.readJson("login_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        coEvery { mockSecureTokenStorage.saveRefreshToken(any()) } returns Unit

        val result = repository.signIn(SignInForm("test@example.com", "password123"))

        assertEquals(AuthenticationResult.Success, result)
        coVerify { mockSecureTokenStorage.saveRefreshToken("refresh_token_xyz") }

        val request = mockWebServer.takeRequest()
        assertEquals("/login", request.path)
    }

    @Test
    fun `signIn returns IncorrectPassword failure when server returns 401`() = runTest {
        val jsonResponse = TestResourceReader.readJson("error_incorrect_password.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(jsonResponse))

        val result = repository.signIn(SignInForm("test@example.com", "wrongpass"))

        assertEquals(AuthenticationResult.Failure(AuthenticationFailure.IncorrectPassword), result)
        coVerify(exactly = 0) { mockSecureTokenStorage.saveRefreshToken(any()) }
    }

    @Test
    fun `signIn returns UserNotFound failure when server returns 404`() = runTest {
        val jsonResponse = TestResourceReader.readJson("error_user_not_found.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(jsonResponse))

        val result = repository.signIn(SignInForm("nobody@example.com", "password123"))

        assertEquals(AuthenticationResult.Failure(AuthenticationFailure.UserNotFound), result)
    }

    @Test
    fun `signUp returns Success when server returns 200 OK`() = runTest {
        val jsonResponse = TestResourceReader.readJson("register_success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = repository.signUp(SignUpForm("NewUser", "newuser@example.com", "password123"))

        assertEquals(AuthenticationResult.Success, result)
        coVerify { mockSecureTokenStorage.saveRefreshToken("refresh_token_abc") }

        val request = mockWebServer.takeRequest()
        assertEquals("/register", request.path)
    }

    @Test
    fun `signUp returns UserAlreadyExists failure when server returns 409`() = runTest {
        val jsonResponse = TestResourceReader.readJson("error_user_already_exists.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(409).setBody(jsonResponse))

        val result = repository.signUp(SignUpForm("ExistingUser", "existing@example.com", "password123"))

        assertEquals(AuthenticationResult.Failure(AuthenticationFailure.UserAlreadyExists), result)
    }

    @Test
    fun `logOut clears token storage and returns Success`() = runTest {
        coEvery { mockSecureTokenStorage.clear() } returns Unit

        val result = repository.logOut()

        assertEquals(AuthenticationResult.Success, result)
        coVerify { mockSecureTokenStorage.clear() }
    }
}

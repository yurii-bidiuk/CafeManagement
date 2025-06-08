package com.cafe.management.android.data.api

import com.cafe.management.android.data.models.User
import com.cafe.management.android.data.models.auth.TokenResponse
import retrofit2.http.*

interface AuthApi {

    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/token")
    suspend fun login(
        @Path("realm") realm: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Path("realm") realm: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): TokenResponse

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): User

    @GET("api/v1/auth/validate")
    suspend fun validateToken()
}

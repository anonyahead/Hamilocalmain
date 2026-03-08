package com.example.hamilocalmain.data.model

/**
 * Geographic coordinates stored in Firestore for map features.
 */
data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Physical address for delivery and pickup.
 */
data class Address(
    val street: String = "",
    val city: String = "",
    val district: String = "",
    val province: String = ""
)

/**
 * Whether this user is selling (FARMER) or buying (CONSUMER).
 */
enum class UserType {
    FARMER,
    CONSUMER
}

/**
 * A registered user of Hami Local. Can be a FARMER or CONSUMER.
 *
 * @property id Unique identifier for the user.
 * @property name Full name of the user.
 * @property phone Contact phone number.
 * @property email Email address for account identification and communication.
 * @property userType Specifies if the user is a FARMER or a CONSUMER.
 * @property address Physical address details.
 * @property location Geographic coordinates for location-based services.
 * @property profileImage URL or path to the user's profile picture.
 * @property isVerified Indicates if the user account has been verified.
 * @property createdAt Timestamp of when the account was created.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val userType: UserType = UserType.CONSUMER,
    val address: Address = Address(),
    val location: GeoPoint = GeoPoint(),
    val profileImage: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

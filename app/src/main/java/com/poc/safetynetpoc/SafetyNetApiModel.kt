package com.poc.safetynetpoc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SafetyNetApiModel {
    @SerializedName("nonce")
    @Expose
    var nonce: String? = null

    @SerializedName("timestampMs")
    @Expose
    var timestampMs: Long? = null

    @SerializedName("apkPackageName")
    @Expose
    var apkPackageName: String? = null

    @SerializedName("apkCertificateDigestSha256")
    @Expose
    var apkCertificateDigestSha256: List<String>? = null

    @SerializedName("apkDigestSha256")
    @Expose
    var apkDigestSha256: String? = null

    @SerializedName("ctsProfileMatch")
    @Expose
    var ctsProfileMatch: Boolean? = null

    @SerializedName("basicIntegrity")
    @Expose
    var basicIntegrity: Boolean? = null


    override fun toString(): String {
        return "SafetyNetApiModel(nonce=$nonce, timestampMs=$timestampMs, apkPackageName=$apkPackageName, apkCertificateDigestSha256=$apkCertificateDigestSha256, apkDigestSha256=$apkDigestSha256, ctsProfileMatch=$ctsProfileMatch, basicIntegrity=$basicIntegrity)"
    }


}
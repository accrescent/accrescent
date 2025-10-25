// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.util

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.DecoderException

fun verifySignature(
    base64SignifyPublicKey: String,
    message: ByteArray,
    signifySignature: String
): Boolean {
    val base64SignifySignature = signifySignature.substringAfterLast(".pub").replace("\n", "")

    val publicKey: ByteArray
    val signature: ByteArray
    try {
        publicKey = Arrays.copyOfRange(Base64.decode(base64SignifyPublicKey), 10, 42)
        signature = Arrays.copyOfRange(Base64.decode(base64SignifySignature), 10, 74)
    } catch (e: DecoderException) {
        return false
    } catch (e: ArrayIndexOutOfBoundsException) {
        return false
    }

    val verifier = Ed25519Signer()
    verifier.init(false, Ed25519PublicKeyParameters(publicKey))
    verifier.update(message, 0, message.size)

    return verifier.verifySignature(signature)
}

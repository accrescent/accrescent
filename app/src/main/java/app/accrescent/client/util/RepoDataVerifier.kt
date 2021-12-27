package app.accrescent.client.util

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64

fun verifySignature(
    signifyPublicKey: String,
    message: ByteArray,
    signifySignature: String
): Boolean {
    val base64SignifyPublicKey =
        signifyPublicKey.replace("untrusted comment: signify public key", "").replace("\n", "")
    val base64SignifySignature = signifySignature.substringAfterLast(".pub").replace("\n", "")
    val publicKey = Arrays.copyOfRange(Base64.decode(base64SignifyPublicKey), 10, 42)
    val signature = Arrays.copyOfRange(Base64.decode(base64SignifySignature), 10, 74)

    val verifier = Ed25519Signer()
    verifier.init(false, Ed25519PublicKeyParameters(publicKey))
    verifier.update(message, 0, message.size)

    return verifier.verifySignature(signature)
}

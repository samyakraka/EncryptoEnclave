package com.example.keygen

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import java.util.Base64

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.keygen/keystore"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "generateKeys") {
                try {
                    val publicKey = generateKeysInTEE()
                    result.success(publicKey)
                } catch (e: Exception) {
                    result.error("KEY_GEN_ERROR", e.message, null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun generateKeysInTEE(): String {
        val keyStoreAlias = "MySecureKeyAlias"
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // Check if the key already exists
        if (!keyStore.containsAlias(keyStoreAlias)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        }

        // Retrieve public key
        val entry = keyStore.getEntry(keyStoreAlias, null) as KeyStore.PrivateKeyEntry
        val publicKey = entry.certificate.publicKey

        // Return Base64 encoded public key
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }
}

# :core:security

Cryptography for the vault: master keys, wrapping them with user secrets, Keystore-backed convenience keys, and the in-memory session.

| | |
|---|---|
| Gradle path | `:core:security` |
| Namespace | `com.calculator.vault.core.security` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin` |

## Key hierarchy

```
PIN / pattern / security answer
        │  PBKDF2-HMAC-SHA256, 120 000 iterations, 16-byte salt  (SecretWrapping)
        ▼
 Key-encryption key (256-bit)  ──AES-256-GCM wrap──▶  MasterKeys (serialized Tink keysets)
                                                        ├─ streaming: AES256-GCM-HKDF, 1 MB segments  → file content
                                                        ├─ aead:      AES256-GCM                      → metadata, thumbnails
                                                        └─ hybrid:    ECIES P-256                     → intruder photos (public key only while locked)
```

Because the key-encryption keys come from user secrets, not from the Android Keystore, the same PIN
re-opens the vault after a reinstall.

## Classes

| Package | Class | Purpose |
|---|---|---|
| `crypto` | `MasterKeys` | Generates and (de)serializes the three Tink keysets. Never stored in plaintext |
| `crypto` | `SecretWrapping` | PBKDF2 derivation, AES-GCM wrap/unwrap (`iv ‖ ciphertext+tag`), answer normalization, pattern encoding |
| `crypto` | `VaultCipher` | Ready-to-use, thread-safe primitives (including random-access decryption for players and the PDF renderer) |
| `crypto` | `PublicKeyEncryptor` | Encrypts *to* the vault without unlocking it (intruder selfies) |
| `crypto` | `TinkBootstrap` | Registers the Tink primitives once per process |
| `keystore` | `KeystoreKeys` | Android Keystore keys: a device key (email recovery on this install) and a biometric-bound key (fingerprint unlock, invalidated when a new fingerprint is enrolled) |
| `session` | `SessionKeys` | Holds the decrypted keys while unlocked. `withKeys {}` keeps them alive until a running job finishes, even if the user locks meanwhile. `require()` throws `VaultLockedException` when locked |

## Dependencies

`:core:common`, `:core:model`, Google Tink (`tink-android`).

# :core:network

Networking: a shared Ktor `HttpClient` (OkHttp engine, JSON content negotiation, debug-only logging), the Google Drive REST client, the OTP backend client, and the connectivity monitor.

| | |
|---|---|
| Gradle path | `:core:network` |
| Namespace | `com.calculator.vault.core.network` |
| Plugins | `calculatorvault.android.library`, `calculatorvault.koin`, `kotlin.serialization` |

## Contents

| Package | Class | Purpose |
|---|---|---|
| `drive` | `DriveApi` | Minimal Drive v3 REST client scoped to the hidden `appDataFolder`: list, resumable streamed upload (large videos never load into memory), download, delete, quota |
| `drive` | `DriveAuth` | Gets an OAuth access token for the `drive.appdata` scope through Play Services Authorization. Returns `DriveToken.NeedsConsent(intent)` the first time, and works from workers after that |
| `otp` | `OtpApi` | Client for the app's own email-OTP backend (see the contract below) |
| `monitor` | `ConnectivityNetworkMonitor` | `NetworkMonitor`: validated internet and unmetered state of the default network |
| `di` | `NetworkModule` | Provides the `HttpClient` |

## OTP backend contract

The base URL is `BuildConfig.OTP_BASE_URL`. Set it with `OTP_BASE_URL` in `local.properties`; the
default is `https://api.example.com/`.

```
POST {OTP_BASE_URL}otp/send     {"email": "user@example.com"}                  → 204
POST {OTP_BASE_URL}otp/verify   {"email": "user@example.com", "code": "123456"} → {"valid": true}
```

## Dependencies

`:core:common`, `:core:model`, `:core:domain`, Ktor client (core `api`, OkHttp, content
negotiation, logging, kotlinx JSON), Play Services Auth.

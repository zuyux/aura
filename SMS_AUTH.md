La integración correcta debe pasar siempre por tu backend. La app Android nunca debe contener las credenciales de Twilio ni llamar directamente a Twilio Verify.

```text
Android → API de AURA → Twilio Verify → SMS
Android → API de AURA → Twilio Verify Check → sesión autenticada
```

Twilio Verify ya gestiona generación, envío, expiración y comprobación del OTP. Su API v2 es la recomendada actualmente. [Documentación oficial de Twilio Verify](https://www.twilio.com/docs/verify/quickstarts)

## 1. Contrato del backend

Añadiría estos endpoints:

```http
POST /auth/sms/send
Content-Type: application/json

{
  "phoneNumber": "+51999999999"
}
```

Respuesta:

```json
{
  "sent": true,
  "retryAfterSeconds": 30
}
```

Y para verificar:

```http
POST /auth/sms/verify
Content-Type: application/json

{
  "phoneNumber": "+51999999999",
  "code": "123456"
}
```

Respuesta aprobada:

```json
{
  "verified": true,
  "accessToken": "...",
  "refreshToken": "..."
}
```

El backend debe imponer los 30 segundos. La cuenta regresiva Android solo mejora la experiencia, pero puede alterarse o evitarse.

## 2. Backend con Twilio Verify

Configura estas variables únicamente en el servidor:

```dotenv
TWILIO_ACCOUNT_SID=AC...
TWILIO_AUTH_TOKEN=...
TWILIO_VERIFY_SERVICE_SID=VA...
```

Ejemplo conceptual en Python:

```python
import os
from twilio.rest import Client

twilio = Client(
    os.environ["TWILIO_ACCOUNT_SID"],
    os.environ["TWILIO_AUTH_TOKEN"],
)

verify_service_sid = os.environ["TWILIO_VERIFY_SERVICE_SID"]


def send_verification(phone_number: str):
    verification = (
        twilio.verify.v2
        .services(verify_service_sid)
        .verifications
        .create(to=phone_number, channel="sms")
    )

    return verification.status


def check_verification(phone_number: str, code: str):
    check = (
        twilio.verify.v2
        .services(verify_service_sid)
        .verification_checks
        .create(to=phone_number, code=code)
    )

    return check.status == "approved"
```

No guardes el OTP en tu base de datos: Twilio Verify realiza la comparación. Cuando el estado sea `approved`, el backend debe marcar el teléfono como verificado y emitir una sesión propia de AURA.

## 3. Añadirlo a Retrofit

En `SyncApi.kt`, aunque sería preferible renombrarlo posteriormente a `AuraApi`, agrega:

```kotlin
@POST("auth/sms/send")
suspend fun sendSmsCode(
    @Body request: SendSmsCodeRequestDto,
): SendSmsCodeResponseDto

@POST("auth/sms/verify")
suspend fun verifySmsCode(
    @Body request: VerifySmsCodeRequestDto,
): VerifySmsCodeResponseDto
```

DTOs:

```kotlin
@Serializable
data class SendSmsCodeRequestDto(
    val phoneNumber: String,
)

@Serializable
data class SendSmsCodeResponseDto(
    val sent: Boolean,
    val retryAfterSeconds: Int,
)

@Serializable
data class VerifySmsCodeRequestDto(
    val phoneNumber: String,
    val code: String,
)

@Serializable
data class VerifySmsCodeResponseDto(
    val verified: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
)
```

## 4. Conectar el ViewModel

El `sendSmsCode()` actual debe sustituir la simulación por la llamada real:

```kotlin
fun sendSmsCode() {
    val phoneNumber = _uiState.value.phoneNumber

    viewModelScope.launch {
        _uiState.update {
            it.copy(isSendingSms = true, errorMessage = null)
        }

        runCatching {
            api.sendSmsCode(SendSmsCodeRequestDto(phoneNumber))
        }.onSuccess { response ->
            _uiState.update {
                it.copy(
                    isSendingSms = false,
                    smsCodeSent = response.sent,
                    smsResendSecondsRemaining = response.retryAfterSeconds,
                    successMessage = "Código SMS enviado.",
                )
            }
            startSmsResendTimer()
        }.onFailure {
            _uiState.update {
                it.copy(
                    isSendingSms = false,
                    errorMessage = "No se pudo enviar el código.",
                )
            }
        }
    }
}
```

`completeOnboarding()` debe verificar primero el OTP en el servidor. Solo después de recibir `verified = true` debería guardar el perfil:

```kotlin
val result = api.verifySmsCode(
    VerifySmsCodeRequestDto(
        phoneNumber = phoneNumber,
        code = smsCode,
    ),
)

if (!result.verified) {
    error("El código es incorrecto o expiró.")
}

// Guardar tokens de manera segura y después persistir el perfil.
userProfileRepository.saveProfile(...)
```

Los tokens no deberían guardarse en preferencias sin protección. Conviene utilizar Android Keystore o almacenamiento cifrado.

## 5. Autodetección sin permiso para leer SMS

Actualmente AURA solicita `READ_SMS`. Es preferible sustituirlo por SMS Retriever API, que puede detectar el OTP sin acceder a toda la bandeja ni solicitar permisos sensibles. Google recomienda iniciar el listener, solicitar el SMS al servidor y enviar luego el código recibido al backend. [Guía oficial de SMS Retriever](https://developer.android.com/identity/sms-retriever)

Twilio puede generar mensajes compatibles con SMS Retriever mediante el hash de la aplicación. [Integración Android de Twilio](https://www.twilio.com/docs/verify/app-verification)

## Seguridad necesaria

El backend debe implementar:

- Espera mínima de 30 segundos por número, además del temporizador Android.
- Límite por teléfono, IP y dispositivo.
- Máximo de intentos de código.
- Expiración del desafío.
- Validación y normalización E.164.
- TLS obligatorio.
- Respuestas que no revelen si un teléfono ya tiene cuenta.
- Credenciales de Twilio únicamente como secretos del servidor.
- Registro de intentos sin almacenar códigos OTP.
- Bloqueo temporal ante abuso.
- Emisión de tokens solamente después de `approved`.

Para este repositorio faltan todavía el endpoint backend, la inyección de la API en `ProfileViewModel` y el almacenamiento de sesión. La UI y el temporizador de 30 segundos ya están preparados para consumir `retryAfterSeconds`.
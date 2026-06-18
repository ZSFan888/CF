# CFApp V10 Fix 3

- Pushed commit: 2a5f7ade704afecf39de7c49c70460805f0df4d5.
- Replaced TokenStore with encrypted-storage fallback to plain SharedPreferences on initialization failure.
- Wrapped ConnectActivity startup in runCatching to avoid hard crash during login screen initialization.
- Intended to reduce launch crash risk on devices where EncryptedSharedPreferences or MasterKey setup fails.

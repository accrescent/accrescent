package app.accrescent.client.workers

object ErrorType {
    const val INVALID_APK_URL = 1
    const val INPUT_OUTPUT = 2
    const val FILE_OPEN_FAILED = 3
    const val SESSION_SEALED_OR_ABANDONED = 4
    const val UNSUCCESSFUL_RESPONSE_CODE = 5
    const val APP_INCOMPATIBLE = 6
    const val APP_NOT_FOUND = 7
    const val REQUEST_CANCELED = 8
    const val REQUEST_TIMEOUT = 9
    const val APP_STORE_SERVICE_UNAVAILABLE = 10
    const val UNRECOGNIZED_SERVER_RESPONSE = 11
    const val INSTALLATION_SERVICES_UNAVAILABLE = 12
    const val INSTALL_SESSION_PARAMS_UNSATISFIABLE = 13
    const val INSTALL_SESSION_PARAMS_INVALID = 14
    const val INSTALL_SESSION_INVALID_OR_NOT_OWNED = 15
    const val INTERNAL = 16
    const val NOT_ALREADY_INSTALLED = 17
    const val NO_MIN_VERSION_CODE = 18
    const val NO_SIGNER_INFO = 19
    const val MINIMUM_VERSION_NOT_MET = 20
    const val APP_HAS_MULTIPLE_SIGNERS = 21
    const val NOT_SIGNED_BY_REQUIRED_SIGNER = 22
    const val SESSION_COMMITTED_OR_ABANDONED = 23
    const val PACKAGE_PARSING_FAILED = 24
    const val SIGNING_INFO_NOT_PRESENT = 25
}

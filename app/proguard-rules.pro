# SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
#
# SPDX-License-Identifier: Apache-2.0

# protobuf-java lite (transitively via appstore-api)
# https://github.com/protocolbuffers/protobuf/blob/v32.1/java/lite.md
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

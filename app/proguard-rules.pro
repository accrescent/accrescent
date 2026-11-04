# SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
#
# SPDX-License-Identifier: Apache-2.0

# protobuf-java lite (transitively via appstore-api)
# https://github.com/protocolbuffers/protobuf/blob/v32.1/java/lite.md
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Workaround for https://github.com/grpc/grpc-java/issues/9611
-keepclassmembers,allowoptimization class io.grpc.okhttp.OkHttpChannelProvider {
    <init>();
}

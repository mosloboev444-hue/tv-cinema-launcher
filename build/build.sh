#!/usr/bin/env bash
# Rebuild CleanYourTV 3.4 + the "Karaoke!!!" fire-tile patch from work/cyt-fork.
#
# Run from Git Bash:   bash work/fork-build/build.sh
#   ONLINE=1           do not pass --offline to Gradle (default: --offline when the caches have it,
#                      automatic fallback online when an artifact is missing)
#   SKIP_SIGN_CHECK=1  do not fail when the signing certificate differs from the TV's
#
# The APK is copied to work/fork-build/app-release.apk; Gradle's own output stays at
# work/cyt-fork/launcher/app/build/outputs/apk/release/app-release.apk.
#
# Toolchain (override with CYTV=...):
#   JDK           %CYTV%\jdk\jdk-21.0.13+9        Temurin 21
#   Android SDK   %CYTV%\android-sdk              platforms;android-34, build-tools;34.0.0
#   Gradle cache  %CYTV%\gradle-home              gradle-8.9-bin + AGP 8.5.2 + AndroidX artifacts
# The release build type is signed with signingConfigs.debug. AGP reads the debug keystore from
# $user.home/.android/debug.keystore, so JAVA_TOOL_OPTIONS below points user.home at this folder and
# .android/debug.keystore here is a copy of the key the TV's installed 3.2 build was signed with.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Windows mixed form (D:/Projects/...) for values a Windows JVM/Gradle reads directly.
winpath() {
    if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else printf '%s' "$1"; fi
}
HERE_WIN="$(winpath "$HERE")"
CYTV="${CYTV:-$(cd "$HERE/../.." && pwd)/.cytv}"
FORK="$HERE/../cyt-fork/launcher"

JDK="${JDK_HOME:-$(ls -d "$CYTV"/jdk/jdk-* 2>/dev/null | sort -V | tail -n 1)}"
ANDROID_HOME="${ANDROID_HOME:-$CYTV/android-sdk}"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$CYTV/gradle-home}"
SDK_PROP="$(winpath "$ANDROID_HOME")"

[ -x "$JDK/bin/java" ] || { echo "ERROR: no JDK at $JDK (set JDK_HOME)" >&2; exit 1; }
[ -f "$ANDROID_HOME/platforms/android-34/android.jar" ] || { echo "ERROR: no platforms;android-34 in $ANDROID_HOME (set ANDROID_HOME)" >&2; exit 1; }
[ -d "$FORK" ] || { echo "ERROR: fork sources missing at $FORK" >&2; exit 1; }
[ -f "$HERE/.android/debug.keystore" ] || { echo "ERROR: $HERE/.android/debug.keystore missing (the TV's signing key)" >&2; exit 1; }

export JAVA_HOME="$JDK"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME
# user.home selects signingConfigs.debug's keystore; java.io.tmpdir keeps Gradle scratch files here.
export JAVA_TOOL_OPTIONS="-Duser.home=$HERE_WIN -Djava.io.tmpdir=$HERE_WIN/tmp"
export PATH="$JAVA_HOME/bin:$PATH"
mkdir -p "$HERE/tmp"

# local.properties is a Java properties file: forward slashes, backslash is an escape.
printf 'sdk.dir=%s\n' "$SDK_PROP" > "$FORK/local.properties"

echo "== Toolchain"
echo "   JAVA_HOME        = $JAVA_HOME"
echo "   ANDROID_HOME     = $ANDROID_HOME"
echo "   GRADLE_USER_HOME = $GRADLE_USER_HOME"
echo "   sdk.dir          = $SDK_PROP"
echo "   signing keystore = $HERE/.android/debug.keystore (user.home=$HERE_WIN)"
"$JAVA_HOME/bin/java" -version 2>&1 | sed 's/^/   /'

cd "$FORK"
GRADLE_ARGS=(assembleRelease --no-daemon --console=plain)
if [ "${ONLINE:-0}" != 1 ]; then
    echo "== gradle ${GRADLE_ARGS[*]} --offline"
    START=$(date +%s)
    set +e
    ./gradlew "${GRADLE_ARGS[@]}" --offline > "$HERE/build.log" 2>&1
    RC=$?
    set -e
    if [ $RC -ne 0 ]; then
        # A cache miss is the one thing an online re-run fixes; a compile error just fails twice.
        if grep -qE "No cached version|not available offline|Could not resolve all|download" "$HERE/build.log"; then
            echo "== offline build needs a download, retrying online"
            ONLINE=1
        else
            echo "ERROR: gradle build failed (log: $HERE/build.log)" >&2
            tail -n 40 "$HERE/build.log" >&2
            exit $RC
        fi
    fi
fi
if [ "${ONLINE:-0}" = 1 ]; then
    echo "== gradle ${GRADLE_ARGS[*]}"
    START=$(date +%s)
    ./gradlew "${GRADLE_ARGS[@]}" 2>&1 | tee "$HERE/build.log"
    RC=${PIPESTATUS[0]}
    [ $RC -eq 0 ] || { echo "ERROR: gradle build failed (log: $HERE/build.log)" >&2; exit $RC; }
fi
echo "== build took $(( $(date +%s) - START ))s"

APK_OUT="$FORK/app/build/outputs/apk/release/app-release.apk"
[ -f "$APK_OUT" ] || { echo "ERROR: gradle produced no $APK_OUT" >&2; exit 1; }
cp "$APK_OUT" "$HERE/app-release.apk"
sha256sum "$HERE/app-release.apk" | sed 's/^/   sha256 /'

# Version info from the APK itself, then the signing certificate.
AAPT2="$ANDROID_HOME/build-tools/34.0.0/aapt2.exe"
APKSIGNER="$ANDROID_HOME/build-tools/34.0.0/apksigner.bat"
echo "== version"
"$AAPT2" dump badging "$HERE/app-release.apk" 2>/dev/null | grep -E "^package:" | sed 's/^/   /'
echo "== signing certificate"
"$APKSIGNER" verify --print-certs "$HERE/app-release.apk" 2>&1 | sed 's/^/   /'

# The installed 3.2 build on the TV (and .cytv/home/.android/debug.keystore) use this certificate.
EXPECTED="df6a8483a1b16b21b732e42cfbfe398eaaa75ef50b89961e7118e655f951e840"
ACTUAL="$("$APKSIGNER" verify --print-certs "$HERE/app-release.apk" 2>/dev/null | sed -n 's/.*SHA-256 digest: //p' | tr -d '\r' | tr 'A-F' 'a-f' | head -n 1)"
if [ "$ACTUAL" = "$EXPECTED" ]; then
    echo "== CERT MATCH: $ACTUAL (same key as the installed com.gathin.tvhome 3.2 -> adb install -r works)"
else
    echo "== CERT MISMATCH: built $ACTUAL, installed $EXPECTED" >&2
    echo "   Installing over the current APK would fail with INSTALL_FAILED_UPDATE_INCOMPATIBLE." >&2
    [ "${SKIP_SIGN_CHECK:-0}" = 1 ] || exit 3
fi
echo "== OK: $HERE/app-release.apk"

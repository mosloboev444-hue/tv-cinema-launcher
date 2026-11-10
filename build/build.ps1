# Rebuild CleanYourTV 3.4 + the "Karaoke!!!" fire-tile patch from work\cyt-fork.
#
# Usage (from the repo root or anywhere):
#   powershell -ExecutionPolicy Bypass -File work\fork-build\build.ps1
#   powershell -ExecutionPolicy Bypass -File work\fork-build\build.ps1 -Online
#   -Online            let Gradle download missing artifacts (default: --offline, automatic fallback)
#   -SkipSignCheck     do not fail when the built APK's certificate is not the TV's
#
# The APK is copied to work\fork-build\app-release.apk; Gradle's own output stays at
# work\cyt-fork\launcher\app\build\outputs\apk\release\app-release.apk.
#
# Toolchain (override with CYTV, JDK_HOME, ANDROID_HOME, GRADLE_USER_HOME):
#   JDK           <CYTV>\jdk\jdk-21*             Temurin 21
#   Android SDK   <CYTV>\android-sdk             platforms;android-34, build-tools;34.0.0
#   Gradle cache  <CYTV>\gradle-home             gradle-8.9-bin + AGP 8.5.2 + AndroidX artifacts
# The release build type is signed with signingConfigs.debug. AGP reads the debug keystore from
# $user.home\.android\debug.keystore, so JAVA_TOOL_OPTIONS below points user.home at this folder and
# .android\debug.keystore here is a copy of the key the TV's installed 3.2 build was signed with.
param(
    [switch]$Online,
    [switch]$SkipSignCheck
)
$ErrorActionPreference = 'Stop'
$OutputEncoding = New-Object System.Text.UTF8Encoding $false

$Here = $PSScriptRoot
$Repo = (Resolve-Path (Join-Path $Here '..\..')).Path
$Cytv = if ($env:CYTV) { $env:CYTV } else { Join-Path $Repo '.cytv' }
$Fork = Join-Path $Here '..\cyt-fork\launcher'

$Jdk = if ($env:JDK_HOME) { $env:JDK_HOME } else {
    Get-ChildItem (Join-Path $Cytv 'jdk') -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName
}
$AndroidHome = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $Cytv 'android-sdk' }
$GradleHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $Cytv 'gradle-home' }

if (-not (Test-Path (Join-Path $Jdk 'bin\java.exe'))) { throw "ERROR: no JDK at $Jdk (set JDK_HOME)" }
if (-not (Test-Path (Join-Path $AndroidHome 'platforms\android-34\android.jar'))) { throw "ERROR: no platforms;android-34 in $AndroidHome (set ANDROID_HOME)" }
if (-not (Test-Path $Fork)) { throw "ERROR: fork sources missing at $Fork" }
$Keystore = Join-Path $Here '.android\debug.keystore'
if (-not (Test-Path $Keystore)) { throw "ERROR: $Keystore missing (the TV's signing key)" }

$env:JAVA_HOME = $Jdk
$env:ANDROID_HOME = $AndroidHome
$env:ANDROID_SDK_ROOT = $AndroidHome
$env:GRADLE_USER_HOME = $GradleHome
# user.home selects signingConfigs.debug's keystore; java.io.tmpdir keeps Gradle scratch files here.
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$Here -Djava.io.tmpdir=$Here\tmp"
$env:PATH = "$Jdk\bin;$env:PATH"
New-Item -ItemType Directory -Force (Join-Path $Here 'tmp') | Out-Null

# local.properties is a Java properties file: forward slashes, backslash is an escape.
"sdk.dir=$($AndroidHome -replace '\\', '/')" | Out-File -Encoding ascii (Join-Path $Fork 'local.properties')

Write-Host '== Toolchain'
Write-Host "   JAVA_HOME        = $Jdk"
Write-Host "   ANDROID_HOME     = $AndroidHome"
Write-Host "   GRADLE_USER_HOME = $GradleHome"
Write-Host "   signing keystore = $Keystore (user.home=$Here)"
(& (Join-Path $Jdk 'bin\java.exe') -version 2>&1) | ForEach-Object { Write-Host "   $_" }

$log = Join-Path $Here 'build.log'
$gradleArgs = @('assembleRelease', '--no-daemon', '--console=plain')
function Invoke-Gradle([string[]] $Arguments) {
    Push-Location $Fork
    try {
        & .\gradlew.bat @Arguments 2>&1 | Tee-Object -FilePath $log | ForEach-Object { Write-Host $_ }
        return $LASTEXITCODE
    } finally { Pop-Location }
}

$start = Get-Date
$built = $false
if (-not $Online) {
    Write-Host "== gradle $($gradleArgs -join ' ') --offline"
    $rc = Invoke-Gradle ($gradleArgs + '--offline')
    if ($rc -eq 0) { $built = $true }
    elseif (Select-String -Path $log -Pattern 'No cached version|not available offline|Could not resolve all' -Quiet) {
        Write-Host '== offline build needs a download, retrying online'
        $Online = $true
    } else {
        throw "ERROR: gradle build failed (exit $rc, log: $log)"
    }
}
if (-not $built) {
    Write-Host "== gradle $($gradleArgs -join ' ')"
    $rc = Invoke-Gradle $gradleArgs
    if ($rc -ne 0) { throw "ERROR: gradle build failed (exit $rc, log: $log)" }
}
Write-Host "== build took $([int]((Get-Date) - $start).TotalSeconds)s"

$apkOut = Join-Path $Fork 'app\build\outputs\apk\release\app-release.apk'
if (-not (Test-Path $apkOut)) { throw "ERROR: gradle produced no $apkOut" }
$apk = Join-Path $Here 'app-release.apk'
Copy-Item $apkOut $apk -Force
$hash = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLower()
Write-Host "   sha256 $hash"

$aapt2 = Join-Path $AndroidHome 'build-tools\34.0.0\aapt2.exe'
$apksigner = Join-Path $AndroidHome 'build-tools\34.0.0\apksigner.bat'
Write-Host '== version'
(& $aapt2 dump badging $apk 2>$null) | Select-String '^package:' | ForEach-Object { Write-Host "   $_" }
Write-Host '== signing certificate'
$certs = (& $apksigner verify --print-certs $apk 2>&1) -join "`n"
Write-Host ($certs -replace '(?m)^', '   ')

# The installed 3.2 build on the TV (and .cytv\home\.android\debug.keystore) use this certificate.
$expected = 'df6a8483a1b16b21b732e42cfbfe398eaaa75ef50b89961e7118e655f951e840'
$m = [regex]::Match($certs, 'SHA-256 digest:\s*([0-9a-fA-F]+)')
$actual = if ($m.Success) { $m.Groups[1].Value.ToLower() } else { '' }
if ($actual -eq $expected) {
    Write-Host "== CERT MATCH: $actual (same key as the installed com.gathin.tvhome 3.2 -> adb install -r works)"
} else {
    Write-Warning "CERT MISMATCH: built $actual, installed $expected - installing over the current APK would fail with INSTALL_FAILED_UPDATE_INCOMPATIBLE."
    if (-not $SkipSignCheck) { exit 3 }
}
Write-Host "== OK: $apk"

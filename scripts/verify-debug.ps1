[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$mobileRoot = Split-Path -Parent $PSScriptRoot
$requiresAsciiAlias = $mobileRoot.ToCharArray() | Where-Object { [int]$_ -gt 127 } | Select-Object -First 1

function Invoke-AndroidVerification {
    & .\gradlew.bat testDebugUnitTest assembleDebug --no-configuration-cache
    if ($LASTEXITCODE -ne 0) {
        throw "Android doğrulaması başarısız oldu (çıkış kodu: $LASTEXITCODE)."
    }
}

if (-not $requiresAsciiAlias) {
    Push-Location -LiteralPath $mobileRoot
    try {
        Invoke-AndroidVerification
    } finally {
        Pop-Location
    }
    exit 0
}

$driveLetter = @('Z', 'Y', 'X', 'W', 'V', 'U', 'T') |
    Where-Object { -not (Test-Path -LiteralPath "${_}:\") } |
    Select-Object -First 1

if (-not $driveLetter) {
    throw 'Android doğrulaması için boş bir geçici sürücü harfi bulunamadı.'
}

$drive = "${driveLetter}:"
& subst.exe $drive $mobileRoot
if ($LASTEXITCODE -ne 0) {
    throw "Geçici $drive sürücü eşlemesi oluşturulamadı."
}

try {
    Push-Location -LiteralPath "$drive\"
    try {
        Invoke-AndroidVerification
    } finally {
        Pop-Location
    }
} finally {
    & subst.exe $drive /d
}


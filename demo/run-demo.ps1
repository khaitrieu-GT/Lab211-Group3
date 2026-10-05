# Chay tu dong toan bo kich ban demo (docs/DEMO_SCRIPT.md) de tap duot / kiem tra.
# Cach chay (tu thu muc goc du an):  powershell -ExecutionPolicy Bypass -File demo\run-demo.ps1
# Du lieu demo nam trong demo\output\ (khong dung toi data\ that). Log tung man: demo\output\logs\.

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$work = Join-Path $PSScriptRoot 'output'
$logs = Join-Path $work 'logs'
$classes = Join-Path $root 'build\classes'
$holdMinutes = 1

Set-Location $root
Write-Host '== Bien dich...'
if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }
New-Item -ItemType Directory -Force $classes | Out-Null
$sources = Get-ChildItem -Recurse -Filter *.java (Join-Path $root 'src') | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Bien dich that bai' }

if (Test-Path $work) { Remove-Item -Recurse -Force $work }
New-Item -ItemType Directory -Force $logs | Out-Null
Set-Location $work   # chuong trinh doc/ghi data\ tai thu muc lam viec -> du lieu demo rieng

function Get-Qr([string]$eTicketId) {
    $row = Import-Csv (Join-Path $work 'data\etickets.csv') | Where-Object { $_.id -eq $eTicketId }
    if (-not $row) { throw "Chua co ve $eTicketId" }
    return $row.qrCode
}

$holdStartedAt = $null
foreach ($act in Get-ChildItem (Join-Path $PSScriptRoot 'acts') -Filter *.txt | Sort-Object Name) {
    if ($act.Name -like '08-*' -and $holdStartedAt) {
        $wait = [int](($holdMinutes * 60 + 5) - ((Get-Date) - $holdStartedAt).TotalSeconds)
        if ($wait -gt 0) {
            Write-Host "== Cho $wait giay de phien giu ghe cua 'khoa' het han..."
            Start-Sleep -Seconds $wait
        }
    }

    $text = Get-Content $act.FullName -Raw
    foreach ($m in [regex]::Matches($text, '\{QR_(ET\d+)\}')) {
        $text = $text.Replace($m.Value, (Get-Qr $m.Groups[1].Value))
    }
    $log = Join-Path $logs ($act.BaseName + '.log')
    Write-Host "== $($act.BaseName)"
    $inFile = Join-Path $logs ($act.BaseName + '.input.txt')
    [System.IO.File]::WriteAllText($inFile, $text, (New-Object System.Text.UTF8Encoding $false))
    cmd /c "java -Dhold.minutes=$holdMinutes -cp `"$classes`" Main < `"$inFile`" > `"$log`" 2>&1"

    if ($act.Name -like '04-*') { $holdStartedAt = Get-Date }

    $errors = Select-String -Path $log -Pattern '^!! |!! ' | ForEach-Object { $_.Line.Trim() }
    foreach ($e in $errors) { Write-Host "   $e" -ForegroundColor DarkYellow }
}

Set-Location $root
Write-Host "== Xong. Log: $logs"
Write-Host '   Cac dong mau vang la loi DU KIEN (kich ban co chu y kiem tra truong hop sai).'

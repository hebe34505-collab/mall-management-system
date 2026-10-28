$envFile = Join-Path $PSScriptRoot "..\backend\.env"
if (-not (Test-Path $envFile)) {
  Write-Error "Copy backend/.env.example to backend/.env, then fill DB_PASSWORD and JWT_SECRET."
  exit 1
}
Get-Content -Encoding UTF8 $envFile | ForEach-Object {
  if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
  $name, $value = $_.Split('=', 2)
  Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}
$mvn = "C:\Users\dell\tools\apache-maven-3.9.11\bin\mvn.cmd"
if (-not (Test-Path $mvn)) { $mvn = "mvn" }
& $mvn -f (Join-Path $PSScriptRoot "..\backend\pom.xml") spring-boot:run

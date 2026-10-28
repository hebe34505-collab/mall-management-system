param(
  [string]$Mysql = "E:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
)
if (-not (Test-Path $Mysql)) {
  Write-Error "找不到 mysql.exe，请把脚本里的路径改成你的安装位置。"
  exit 1
}
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path -replace '\\', '/'
Write-Host "即将导入 schema.sql，请输入 MySQL root 密码。"
& $Mysql -u root -p --default-character-set=utf8mb4 -e "source $root/database/schema.sql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "即将导入 seed.sql，请再次输入 MySQL root 密码。"
& $Mysql -u root -p --default-character-set=utf8mb4 -e "source $root/database/seed.sql"
exit $LASTEXITCODE

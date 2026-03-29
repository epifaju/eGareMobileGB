# Demarrage API en dev (profil Spring dev).
# Flyway : meme URL que l'app (variables DB_* comme application.yml).
# Les migrations s'appliquent aussi au demarrage Spring ; ce script affiche
# l'etat (info) et peut executer migrate avant la JVM.
#
# Usage (depuis backend/ ou n'importe ou) :
#   .\run-dev.ps1
#   .\run-dev.ps1 -SkipFlyway
#   .\run-dev.ps1 -FlywayMigrate
#
# PowerShell : les arguments -D Maven avec des points doivent etre entre guillemets droits ".

param(
  [switch] $SkipFlyway,
  [switch] $FlywayMigrate
)

Set-Location $PSScriptRoot

$DbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5433" }
$DbName = if ($env:DB_NAME) { $env:DB_NAME } else { "garemobilegb" }
$DbUser = if ($env:DB_USER) { $env:DB_USER } else { "gare" }
$DbPass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "gare_dev_secret" }
$FlywayUrl = "jdbc:postgresql://${DbHost}:${DbPort}/${DbName}"

if (-not $SkipFlyway) {
  Write-Host "Flyway - etat des migrations ($FlywayUrl)..." -ForegroundColor Cyan
  mvn flyway:info "-Dflyway.url=$FlywayUrl" "-Dflyway.user=$DbUser" "-Dflyway.password=$DbPass"
  if ($LASTEXITCODE -ne 0) {
    Write-Host "Flyway info a echoue (PostgreSQL sur ${DbHost}:${DbPort} ? docker compose up -d postgres). -SkipFlyway pour lancer quand meme." -ForegroundColor Yellow
    exit $LASTEXITCODE
  }

  if ($FlywayMigrate) {
    Write-Host "Flyway - application des migrations (migrate)..." -ForegroundColor Cyan
    mvn flyway:migrate "-Dflyway.url=$FlywayUrl" "-Dflyway.user=$DbUser" "-Dflyway.password=$DbPass"
    if ($LASTEXITCODE -ne 0) {
      exit $LASTEXITCODE
    }
  }
}

Write-Host "Spring Boot - profil dev..." -ForegroundColor Green
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

param(
    [long]$EventId,
    [string]$EnvFile = "config/.env.dev"
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Msg)
    Write-Host "[NORMALIZE-EVENT] $Msg"
}

function Read-EnvMap {
    param([string]$Path)
    $map = @{}
    if (-not (Test-Path $Path)) {
        throw "Env file not found: $Path"
    }
    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) { return }
        $idx = $line.IndexOf("=")
        if ($idx -lt 1) { return }
        $map[$line.Substring(0, $idx).Trim()] = $line.Substring($idx + 1).Trim()
    }
    return $map
}

function Resolve-DbConfig {
    param([hashtable]$EnvMap)
    $dbUrl = $EnvMap["DB_URL"]
    if (-not $dbUrl) {
        throw "DB_URL missing in env file"
    }
    if ($dbUrl -match "://mysql:") {
        $dbUrl = $dbUrl -replace "://mysql:", "://localhost:"
    }
    $dbUser = $EnvMap["DB_USERNAME"]
    if (-not $dbUser) { $dbUser = $EnvMap["DB_USER"] }
    if (-not $dbUser) { throw "DB_USERNAME/DB_USER missing in env file" }
    $dbPassword = $EnvMap["DB_PASSWORD"]
    if ($null -eq $dbPassword) { $dbPassword = "" }
    return @{
        url = $dbUrl
        user = $dbUser
        password = $dbPassword
    }
}

function Resolve-ConnectorJar {
    $candidates = @(
        "D:\CODE\mvn_repository\com\mysql\mysql-connector-j",
        (Join-Path $env:USERPROFILE ".m2\repository\com\mysql\mysql-connector-j")
    )
    foreach ($base in $candidates) {
        if (-not (Test-Path $base)) { continue }
        $jar = Get-ChildItem -Path $base -Recurse -Filter "mysql-connector-j-*.jar" -File -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1 -ExpandProperty FullName
        if ($jar) { return $jar }
    }
    throw "Unable to locate mysql-connector-j jar"
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$envMap = Read-EnvMap -Path (Join-Path $root $EnvFile)
$db = Resolve-DbConfig -EnvMap $envMap
$connectorJar = Resolve-ConnectorJar

$src = @"
import java.sql.*;
import java.time.*;
public class NormalizeRegressionEvent {
  public static void main(String[] args) throws Exception {
    long eventId = Long.parseLong(args[0]);
    String dbUrl = args[1];
    String dbUser = args[2];
    String dbPassword = args[3];
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
      OffsetDateTime now = OffsetDateTime.now();
      Timestamp regStart = Timestamp.from(now.minusDays(1).toInstant());
      Timestamp regEnd = Timestamp.from(now.plusDays(1).toInstant());
      Timestamp start = Timestamp.from(now.plusDays(2).toInstant());
      Timestamp end = Timestamp.from(now.plusDays(3).toInstant());
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE event SET status='OPEN', reg_start_time=?, reg_deadline=?, start_time=?, end_time=?, update_time=NOW() WHERE id=?")) {
        ps.setTimestamp(1, regStart);
        ps.setTimestamp(2, regEnd);
        ps.setTimestamp(3, start);
        ps.setTimestamp(4, end);
        ps.setLong(5, eventId);
        int updated = ps.executeUpdate();
        if (updated != 1) {
          throw new RuntimeException("event not found: " + eventId);
        }
      }
      try (PreparedStatement qs = c.prepareStatement(
          "SELECT id, name, status, reg_start_time, reg_deadline, start_time, end_time FROM event WHERE id=?")) {
        qs.setLong(1, eventId);
        try (ResultSet rs = qs.executeQuery()) {
          if (rs.next()) {
            System.out.println("EVENT_ID=" + rs.getLong("id"));
            System.out.println("EVENT_NAME=" + rs.getString("name"));
            System.out.println("EVENT_STATUS=" + rs.getString("status"));
            System.out.println("REG_START=" + rs.getTimestamp("reg_start_time"));
            System.out.println("REG_END=" + rs.getTimestamp("reg_deadline"));
            System.out.println("EVENT_START=" + rs.getTimestamp("start_time"));
            System.out.println("EVENT_END=" + rs.getTimestamp("end_time"));
          }
        }
      }
    }
  }
}
"@

$tmp = Join-Path $env:TEMP "NormalizeRegressionEvent.java"
Set-Content -Path $tmp -Value $src -Encoding ASCII

Write-Step "Normalize eventId=$EventId using $connectorJar"
java -cp "$connectorJar;$env:TEMP" $tmp $EventId $db.url $db.user $db.password
if ($LASTEXITCODE -ne 0) {
    throw "normalize-regression-event failed with exit code $LASTEXITCODE"
}

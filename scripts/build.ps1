param([string]$OutputJar = "")
$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $OutputJar) { $OutputJar = Join-Path $Root 'build/ATM0-Arcane-Library-0.2.2.jar' }
$OutDir = Split-Path $OutputJar -Parent
New-Item -Force -ItemType Directory (Join-Path $Root 'build/patcher'), (Join-Path $Root 'build/hooks'), $OutDir | Out-Null
$Exports = @('--add-exports','java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED','--add-exports','java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED','--add-exports','java.base/jdk.internal.org.objectweb.asm.tree.analysis=ALL-UNNAMED')
& javac --release 8 -d (Join-Path $Root 'build/hooks') (Join-Path $Root 'src/main/java/dev/frostey/arcanelibrary/v2/V2Hooks.java') (Join-Path $Root 'src/main/java/dev/frostey/arcanelibrary/v2/V21BlockHooks.java') (Join-Path $Root 'src/main/java/dev/frostey/arcanelibrary/v2/V22OutputHooks.java') (Join-Path $Root 'src/main/java/dev/frostey/arcanelibrary/v2/V22GuiHooks.java')
if ($LASTEXITCODE -ne 0) { throw 'Hook compile failed' }
& javac @Exports -d (Join-Path $Root 'build/patcher') (Join-Path $Root 'tools/PatchJar.java') (Join-Path $Root 'tools/VerifyJar.java')
if ($LASTEXITCODE -ne 0) { throw 'Patcher compile failed' }
& java @Exports -cp (Join-Path $Root 'build/patcher') PatchJar (Join-Path $Root 'vendor/ATM0-Arcane-Library-0.1.2-exp3-listui.jar') $OutputJar (Join-Path $Root 'build/hooks') (Join-Path $Root 'src/main/resources')
if ($LASTEXITCODE -ne 0) { throw 'Patch failed' }
& java @Exports -cp (Join-Path $Root 'build/patcher') VerifyJar $OutputJar
if ($LASTEXITCODE -ne 0) { throw 'Verify failed' }
Write-Host "Built $OutputJar"

<#
.SYNOPSIS
  一键构建并部署前端到服务器
.DESCRIPTION
  1. 在 guanghe-studio 目录执行 npm run build
  2. 将 dist 目录打包为 tar.gz
  3. 通过 scp 上传到服务器
  4. 在服务器上解压并替换 /home/kunpeng/web 目录
  5. 清理临时文件
.NOTES
  前提：已配置 SSH 免密登录（ssh-copy-id ubuntu@101.43.66.196）
  如未配置，脚本会提示输入密码
#>

$ErrorActionPreference = "Stop"

# ===== 服务器配置 =====
$SERVER_USER = "ubuntu"
$SERVER_IP   = "101.43.66.196"
$SERVER_DIR  = "/home/kunpeng/web"
$SSH_TARGET  = "${SERVER_USER}@${SERVER_IP}"

# ===== 路径配置 =====
$SCRIPT_DIR  = Split-Path -Parent $MyInvocation.MyCommand.Path
$DIST_DIR    = Join-Path $SCRIPT_DIR "dist"
$TAR_FILE    = Join-Path $SCRIPT_DIR "dist.tar.gz"
$REMOTE_TAR  = "/tmp/dist.tar.gz"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  前端一键部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "服务器: $SSH_TARGET" -ForegroundColor Yellow
Write-Host "目标路径: $SERVER_DIR" -ForegroundColor Yellow
Write-Host ""

# ===== Step 1: 构建前端 =====
Write-Host "[1/5] 构建前端 (npm run build)..." -ForegroundColor Green
Set-Location $SCRIPT_DIR
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "构建失败！请检查错误信息。" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $DIST_DIR)) {
    Write-Host "构建后未找到 dist 目录: $DIST_DIR" -ForegroundColor Red
    exit 1
}
Write-Host "构建成功，dist 目录已生成。" -ForegroundColor Green
Write-Host ""

# ===== Step 2: 打包 dist 目录 =====
Write-Host "[2/5] 打包 dist 目录为 tar.gz..." -ForegroundColor Green
if (Test-Path $TAR_FILE) { Remove-Item $TAR_FILE -Force }
tar -czf $TAR_FILE -C $DIST_DIR .
if ($LASTEXITCODE -ne 0) {
    Write-Host "打包失败！" -ForegroundColor Red
    exit 1
}
$tarSize = [math]::Round((Get-Item $TAR_FILE).Length / 1MB, 2)
Write-Host "打包成功，文件大小: ${tarSize} MB" -ForegroundColor Green
Write-Host ""

# ===== Step 3: 上传到服务器 =====
Write-Host "[3/5] 上传到服务器..." -ForegroundColor Green
scp -O $TAR_FILE "${SSH_TARGET}:${REMOTE_TAR}"
if ($LASTEXITCODE -ne 0) {
    Write-Host "上传失败！请检查网络连接和 SSH 配置。" -ForegroundColor Red
    exit 1
}
Write-Host "上传成功。" -ForegroundColor Green
Write-Host ""

# ===== Step 4: 服务器端替换 =====
Write-Host "[4/5] 服务器端解压并替换..." -ForegroundColor Green
$remoteCmd = "rm -rf ${SERVER_DIR} && mkdir -p ${SERVER_DIR} && tar -xzf ${REMOTE_TAR} -C ${SERVER_DIR} && rm -f ${REMOTE_TAR} && echo 'DEPLOY_SUCCESS'"
ssh $SSH_TARGET $remoteCmd
if ($LASTEXITCODE -ne 0) {
    Write-Host "服务器端替换失败！" -ForegroundColor Red
    exit 1
}
Write-Host "服务器端替换成功。" -ForegroundColor Green
Write-Host ""

# ===== Step 5: 清理本地临时文件 =====
Write-Host "[5/5] 清理本地临时文件..." -ForegroundColor Green
Remove-Item $TAR_FILE -Force
Write-Host "清理完成。" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "访问地址: http://${SERVER_IP}" -ForegroundColor Yellow
Write-Host ""
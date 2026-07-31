#!/usr/bin/env bash
# One-time EC2 bootstrap for the credit-card platform (Amazon Linux 2023 / Ubuntu).
set -euo pipefail

echo "==> Installing Docker"
if command -v apt-get >/dev/null 2>&1; then
  sudo apt-get update -y
  sudo apt-get install -y ca-certificates curl gnupg
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update -y
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
elif command -v dnf >/dev/null 2>&1; then
  sudo dnf install -y docker
  sudo systemctl enable --now docker
  sudo mkdir -p /usr/local/lib/docker/cli-plugins
  sudo curl -SL "https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
  sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
fi

sudo usermod -aG docker "$USER" || true
sudo mkdir -p /opt/credit-card-platform
sudo chown "$USER":"$USER" /opt/credit-card-platform

echo "==> Place .env in /opt/credit-card-platform (DB_PASSWORD, OAUTH2_*, GHCR credentials via CI)"
echo "==> CI will copy docker-compose.prod.yml and pull images from GHCR"
echo "Done. Log out/in so docker group membership applies."

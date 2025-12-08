#!/bin/bash

# EC2 초기 설정 스크립트
# EC2 인스턴스에서 실행: curl -sSL https://raw.githubusercontent.com/your-repo/setup-ec2.sh | bash
# 또는 이 스크립트를 EC2에 업로드하여 실행

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}EC2 초기 설정 시작${NC}"
echo -e "${GREEN}========================================${NC}"

# 1. 시스템 업데이트
echo -e "\n${YELLOW}[1/6] 시스템 업데이트 중...${NC}"
sudo yum update -y

# 2. Docker 설치
echo -e "\n${YELLOW}[2/6] Docker 설치 중...${NC}"
if ! command -v docker &> /dev/null; then
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    echo -e "${GREEN}Docker 설치 완료. 로그아웃 후 다시 로그인하면 docker 명령을 sudo 없이 사용할 수 있습니다.${NC}"
else
    echo -e "${GREEN}Docker가 이미 설치되어 있습니다.${NC}"
fi

# 3. Docker Compose 설치
echo -e "\n${YELLOW}[3/6] Docker Compose 설치 중...${NC}"
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}Docker Compose 설치 완료${NC}"
else
    echo -e "${GREEN}Docker Compose가 이미 설치되어 있습니다.${NC}"
fi

# 4. Java 21 설치 (Docker를 사용하지 않는 경우 대비)
echo -e "\n${YELLOW}[4/6] Java 21 설치 확인 중...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}Java가 설치되어 있지 않습니다. Docker를 사용하므로 선택 사항입니다.${NC}"
else
    echo -e "${GREEN}Java가 이미 설치되어 있습니다.${NC}"
fi

# 5. 작업 디렉토리 생성
echo -e "\n${YELLOW}[5/6] 작업 디렉토리 생성 중...${NC}"
mkdir -p ~/commuknit
cd ~/commuknit

# 6. 방화벽 설정 안내
echo -e "\n${YELLOW}[6/6] 보안 그룹 설정 안내${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}EC2 보안 그룹에서 다음 포트를 열어주세요:${NC}"
echo -e "  - 포트 8080 (HTTP)"
echo -e "  - 포트 443 (HTTPS, 선택사항)"
echo -e "  - 포트 22 (SSH)"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${GREEN}초기 설정 완료!${NC}"
echo -e "${YELLOW}다음 단계:${NC}"
echo -e "  1. .env 파일을 ~/commuknit/ 디렉토리에 생성하세요"
echo -e "  2. docker-compose.yml 파일을 업로드하세요"
echo -e "  3. 배포 스크립트를 실행하세요"

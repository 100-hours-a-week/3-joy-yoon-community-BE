#!/bin/bash

# EC2 배포 스크립트
# 사용법: ./deploy.sh [EC2_USER@EC2_HOST]

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 변수 설정
EC2_USER_HOST=${1:-"ec2-user@your-ec2-ip"}
REMOTE_DIR="/home/ec2-user/commuknit"
LOCAL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Community 백엔드 EC2 배포 시작${NC}"
echo -e "${GREEN}========================================${NC}"

# 1. 로컬에서 Docker 이미지 빌드
echo -e "\n${YELLOW}[1/5] Docker 이미지 빌드 중...${NC}"
docker build -t community-backend:v1.1 .

# 2. 이미지 압축 및 전송
echo -e "\n${YELLOW}[2/5] Docker 이미지를 EC2로 전송 중...${NC}"
docker save community-backend:v1.1 | gzip | ssh "$EC2_USER_HOST" "gunzip | docker load"

# 3. 필요한 파일들을 EC2로 전송
echo -e "\n${YELLOW}[3/5] 설정 파일 전송 중...${NC}"
ssh "$EC2_USER_HOST" "mkdir -p $REMOTE_DIR"

# docker-compose.prod.yml이 있으면 사용, 없으면 docker-compose.yml 사용
if [ -f "docker-compose.prod.yml" ]; then
    scp docker-compose.prod.yml "$EC2_USER_HOST:$REMOTE_DIR/docker-compose.yml"
else
    scp docker-compose.yml "$EC2_USER_HOST:$REMOTE_DIR/"
fi

# .env 파일이 있으면 전송, 없으면 경고만 표시
if [ -f ".env" ]; then
    scp .env "$EC2_USER_HOST:$REMOTE_DIR/"
else
    echo -e "${YELLOW}경고: .env 파일을 찾을 수 없습니다. EC2에서 수동으로 설정해주세요.${NC}"
fi

# 4. EC2에서 기존 컨테이너 중지 및 제거
echo -e "\n${YELLOW}[4/5] 기존 컨테이너 중지 및 제거 중...${NC}"
ssh "$EC2_USER_HOST" "cd $REMOTE_DIR && docker-compose down || true"

# 5. 새 컨테이너 시작
echo -e "\n${YELLOW}[5/5] 새 컨테이너 시작 중...${NC}"
ssh "$EC2_USER_HOST" "cd $REMOTE_DIR && docker-compose up -d"

# 6. 로그 확인
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}배포 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n컨테이너 상태 확인 중..."
ssh "$EC2_USER_HOST" "cd $REMOTE_DIR && docker-compose ps"

echo -e "\n${YELLOW}로그 확인: ssh $EC2_USER_HOST 'cd $REMOTE_DIR && docker-compose logs -f'${NC}"

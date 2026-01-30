#!/bin/bash

# 快速部署脚本
# 使用方法：./deploy.sh [deploy|restart|stop|logs|status]

set -e

PROJECT_DIR="/root/what2eat"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

case "$1" in
  deploy)
    echo -e "${GREEN}=== 开始部署 ===${NC}"
    cd $PROJECT_DIR
    docker compose down 2>/dev/null || true
    docker compose up -d
    echo -e "${GREEN}=== 部署完成 ===${NC}"
    echo -e "应用地址: ${YELLOW}http://47.242.74.112:8883/api${NC}"
    echo -e "Swagger文档: ${YELLOW}http://47.242.74.112:8883/api/swagger-ui.html${NC}"
    ;;

  restart)
    echo -e "${YELLOW}=== 重启服务 ===${NC}"
    cd $PROJECT_DIR
    docker compose restart
    echo -e "${GREEN}=== 重启完成 ===${NC}"
    ;;

  stop)
    echo -e "${RED}=== 停止服务 ===${NC}"
    cd $PROJECT_DIR
    docker compose down
    echo -e "${GREEN}=== 已停止 ===${NC}"
    ;;

  logs)
    echo -e "${YELLOW}=== 查看日志 (Ctrl+C退出) ===${NC}"
    cd $PROJECT_DIR
    docker compose logs -f
    ;;

  logs-app)
    echo -e "${YELLOW}=== 查看应用日志 (Ctrl+C退出) ===${NC}"
    cd $PROJECT_DIR
    docker compose logs -f app
    ;;

  logs-mysql)
    echo -e "${YELLOW}=== 查看MySQL日志 (Ctrl+C退出) ===${NC}"
    cd $PROJECT_DIR
    docker compose logs -f mysql
    ;;

  status)
    echo -e "${GREEN}=== 服务状态 ===${NC}"
    cd $PROJECT_DIR
    docker compose ps
    ;;

  *)
    echo "用法: $0 {deploy|restart|stop|logs|logs-app|logs-mysql|status}"
    echo ""
    echo "命令说明:"
    echo "  deploy      - 首次部署或更新后启动"
    echo "  restart     - 重启所有服务"
    echo "  stop        - 停止所有服务"
    echo "  logs        - 查看所有日志"
    echo "  logs-app    - 查看应用日志"
    echo "  logs-mysql  - 查看MySQL日志"
    echo "  status      - 查看服务状态"
    exit 1
    ;;
esac

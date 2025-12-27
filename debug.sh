#!/bin/bash

# 充电订单系统调试工具脚本
# Quick Debug Script for Charging Order System

echo "🔧 充电订单系统调试工具"
echo "=========================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 函数：运行所有测试
function run_all_tests() {
    echo -e "${GREEN}📦 运行所有测试...${NC}"
    mvn clean test
}

# 函数：运行单个测试
function run_single_test() {
    echo -e "${GREEN}📦 运行单个测试: $1${NC}"
    mvn test -Dtest=$1
}

# 函数：调试模式运行测试
function debug_test() {
    echo -e "${YELLOW}🐛 调试模式运行测试: $1${NC}"
    mvn test -Dtest=$1 -Dmaven.surefire.debug
    echo -e "${YELLOW}等待调试器连接到端口 5005...${NC}"
}

# 函数：查看最新日志
function view_logs() {
    if [ -f "logs/charging-order-system.log" ]; then
        echo -e "${GREEN}📄 查看最新日志...${NC}"
        tail -n 50 logs/charging-order-system.log
    else
        echo -e "${RED}❌ 日志文件不存在${NC}"
    fi
}

# 函数：实时监控日志
function tail_logs() {
    if [ -f "logs/charging-order-system.log" ]; then
        echo -e "${GREEN}📄 实时监控日志 (Ctrl+C 退出)...${NC}"
        tail -f logs/charging-order-system.log
    else
        echo -e "${RED}❌ 日志文件不存在${NC}"
    fi
}

# 函数：搜索日志
function search_logs() {
    if [ -f "logs/charging-order-system.log" ]; then
        echo -e "${GREEN}🔍 搜索日志关键词: $1${NC}"
        grep --color=always "$1" logs/charging-order-system.log
    else
        echo -e "${RED}❌ 日志文件不存在${NC}"
    fi
}

# 函数：清理日志
function clean_logs() {
    echo -e "${YELLOW}🗑️  清理日志文件...${NC}"
    rm -rf logs/*.log
    echo -e "${GREEN}✅ 日志已清理${NC}"
}

# 函数：生成测试覆盖率报告
function coverage_report() {
    echo -e "${GREEN}📊 生成测试覆盖率报告...${NC}"
    mvn clean test jacoco:report
    echo -e "${GREEN}✅ 报告位置: target/site/jacoco/index.html${NC}"
}

# 主菜单
function main_menu() {
    echo ""
    echo "请选择操作："
    echo "1) 运行所有测试"
    echo "2) 运行 PrePaid 生命周期测试"
    echo "3) 运行 PostPaid 生命周期测试"
    echo "4) 运行取消订单测试"
    echo "5) 运行跨策略验证测试"
    echo "6) 调试模式运行测试"
    echo "7) 查看最新日志 (最后50行)"
    echo "8) 实时监控日志"
    echo "9) 搜索日志"
    echo "10) 清理日志"
    echo "11) 生成测试覆盖率报告"
    echo "0) 退出"
    echo ""
    read -p "请输入选项 [0-11]: " choice

    case $choice in
        1)
            run_all_tests
            ;;
        2)
            run_single_test "ChargingOrderServiceTest#testPrePaidOrderLifecycle"
            ;;
        3)
            run_single_test "ChargingOrderServiceTest#testPostPaidOrderLifecycle"
            ;;
        4)
            run_single_test "ChargingOrderServiceTest#testCancelPrePaidOrder"
            ;;
        5)
            run_single_test "ChargingOrderServiceTest#testCrossStrategyValidation"
            ;;
        6)
            read -p "输入测试方法名 (例如: ChargingOrderServiceTest#testPrePaidOrderLifecycle): " test_name
            debug_test "$test_name"
            ;;
        7)
            view_logs
            ;;
        8)
            tail_logs
            ;;
        9)
            read -p "输入搜索关键词: " keyword
            search_logs "$keyword"
            ;;
        10)
            clean_logs
            ;;
        11)
            coverage_report
            ;;
        0)
            echo -e "${GREEN}👋 再见！${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ 无效选项${NC}"
            ;;
    esac
    
    # 继续显示菜单
    main_menu
}

# 启动菜单
main_menu

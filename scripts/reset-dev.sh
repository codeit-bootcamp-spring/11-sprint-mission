#!/bin/bash

set -e

cd "$(dirname "$0")/.."

read -p "정말 로컬 DB와 data, .logs를 초기화하시겠습니까? (Y/N): " confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo "취소되었습니다."
  exit 0
fi

echo "==> [1/3] 로컬 파일 정리"
rm -rf data/*
rm -rf .logs/*

echo "==> [2/3] PostgreSQL 데이터 초기화"
psql -U discodeit_user -d discodeit -f scripts/reset.sql

echo "==> [3/3] 완료"
echo "로컬 파일과 DB 데이터가 초기화되었습니다."
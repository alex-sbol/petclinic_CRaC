#!/usr/bin/env bash
set -e
CHECKPOINT_DIR=${PWD}/checkpoints
IMAGE=petclinic-crac

measure_startup() {
  local label=$1
  local entry=$2
  echo "== Measuring $label startup =="

  start=$(date +%s%3N)
  docker run --rm \
    --cap-add=CHECKPOINT_RESTORE \
    -v ${CHECKPOINT_DIR}:/checkpoints \
    -p 8080:8080 \
    ${IMAGE} ${entry} >/dev/null 2>&1 &
  pid=$!

  until curl -sf http://localhost:8080/actuator/health > /dev/null; do
    sleep 0.1
  done

  end=$(date +%s%3N)
  duration=$((end - start))
  echo "$label startup: ${duration} ms"
  docker kill $(docker ps -q --filter ancestor=${IMAGE}) >/dev/null 2>&1 || true
}

# Cold start
measure_startup "Cold" ""

# Create snapshot
docker run --rm \
  --cap-add=CHECKPOINT_RESTORE \
  -v ${CHECKPOINT_DIR}:/checkpoints \
  -p 8080:8080 \
  ${IMAGE} /app/create_snapshot.sh || true

# Restored start
measure_startup "Restored" ""

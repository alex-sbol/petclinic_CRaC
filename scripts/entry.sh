#!/usr/bin/env bash
set -e

CHECKPOINT_DIR="/checkpoints"
APP_JAR="/app/app.jar"

if [ -d "${CHECKPOINT_DIR}/jvm" ]; then
  echo "[ENTRYPOINT] Detected existing checkpoint at ${CHECKPOINT_DIR}"
  echo "[ENTRYPOINT] Restoring JVM from snapshot..."
  exec java -XX:CRaCRestoreFrom=${CHECKPOINT_DIR} -jar ${APP_JAR}
else
  echo "[ENTRYPOINT] No checkpoint found. Starting cold boot..."
  exec java -jar ${APP_JAR}
fi

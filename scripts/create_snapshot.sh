#!/usr/bin/env bash
set -e
echo "[SNAPSHOT] Triggering snapshot..."
curl -X POST http://localhost:8080/snapshot/create
echo "[SNAPSHOT] Snapshot triggered. JVM will exit after checkpoint."

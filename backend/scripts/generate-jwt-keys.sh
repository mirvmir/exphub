#!/usr/bin/env sh
set -eu

mkdir -p data/secrets
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out data/secrets/private.pem
openssl rsa -pubout -in data/secrets/private.pem -out data/secrets/public.pem
chmod 600 data/secrets/private.pem
chmod 644 data/secrets/public.pem

echo "JWT keys generated in data/secrets"

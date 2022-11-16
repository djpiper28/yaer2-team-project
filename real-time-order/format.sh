#!/bin/bash
# Foramt python code
black .
# Format go code
gofmt -l -w .
git commit -a -m "format code in ci" && git push
git push

true

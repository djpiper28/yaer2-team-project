#!/bin/bash
# Format java code
find . | grep "\\.java\$" | xargs -d"\n" astyle --mode=java --style=java
# Format sql code
find . | grep "\\.sql$$" | xargs -d "\n" astyle
# Foramt python code
black .
git commit -a -m "format code in ci" && git push

true

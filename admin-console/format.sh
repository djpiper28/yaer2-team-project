#!/bin/bash
# Format cxx code
find . | grep "\\.c\$" | xargs -d"\n" astyle --style=kr
find . | grep "\\.cpp\$" | xargs -d"\n" astyle --style=kr
find . | grep "\\.h\$" | xargs -d"\n" astyle --style=kr
# Format sql code
find . | grep "\\.sql$$" | xargs -d "\n" astyle
# Foramt python code
black .
git commit -a -m "format code in ci" && git push

true

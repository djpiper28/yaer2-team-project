out = "012456789-_=+"
for i in range(26):
    out += chr(ord("a") + i)
    out += chr(ord("A") + i)

print(out)

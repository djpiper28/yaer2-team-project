#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <unistd.h>

void printHelp() {
    printf("Password generator help.\n");
    printf("\tUsage: passwordgen <length>\n");
    printf("\t- Version 1.0\n");
    printf("\t- Created by Danny Piper\n");
    printf("\t- https://gist.github.com/djpiper28/5f0b1e6dc9c4e2e4647644143a3fe2eb\n");
    printf("\t- Distributed without licence and warranty\n");
}

int main (int argc, char *argv[]) {
    if (argc != 2) {
        printHelp();
        return -2;
    } else {
        int length = atoi(argv[1]);
        if (length <= 0) {
            printf("Password length must be an integer greater than 0.\n");
            return -1;
        }
        
        struct timeval seedTime;
        gettimeofday(&seedTime, NULL);
        srand(seedTime.tv_usec);
        
        char validChars[] = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        int validCharsLength = sizeof(validChars);
        
        for (int i = 0; i < length; i++) {
            int t = rand();
            putc(validChars[t % validCharsLength], stdout);
        }        
        
        return 0;
    }
}

#include <iostream>
#include <stdio.h>
#include <string.h>
#include "utils.h"

//TODO: Make this work on windoze
#include <sys/socket.h>
#include <errno.h>
#include <netdb.h>
#include <arpa/inet.h>

std::string resolveHostName(std::string host)
{
    int sockfd;
    struct addrinfo hints, *servinfo, *p;
    struct sockaddr_in *h;
    int rv;
    std::string ret;

    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC; // use AF_INET6 to force IPv6
    hints.ai_socktype = SOCK_STREAM;

    if ((rv = getaddrinfo(host.c_str(), "http", &hints, &servinfo)) != 0) {
        std::cerr << "resolveHostName failed: "
                  << gai_strerror(rv)
                  << std::endl;
        return ret;
    }

    // loop through all the results and connect to the first we can
    for(p = servinfo; p != NULL; p = p->ai_next) {
        h = (struct sockaddr_in *) p->ai_addr;
        ret = std::string(inet_ntoa( h->sin_addr));
    }

    freeaddrinfo(servinfo);
    return ret;
}

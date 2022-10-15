FROM openjdk:8-bullseye

# Install Build Essentials
RUN apt-get update

#Install ant to build netbeans project
RUN apt-get install ant -y

#Install go and github-release app
ENV PATH="/root/go/bin:$PATH"
RUN apt-get install -y golang
RUN go get github.com/github-release/github-release

CMD ["/bin/bash"]

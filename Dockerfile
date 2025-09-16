# Use an official Gradle image as a base image (or openjdk if you prefer)
FROM gradle:8.14.3-jdk24-corretto-al2023 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the entire Gradle project, including the gradle wrapper
COPY . .

# Run the Gradle build
RUN gradle clean assemble --info --no-daemon

# Remove the 'src' directory to save space
RUN rm -rf src




WORKDIR /app
FROM openjdk:24-slim

ENV DEBIAN_FRONTEND=noninteractive
MAINTAINER Simon Wögerbauer

RUN apt update && apt install -y --no-install-recommends \
    wget \
    ca-certificates \
    texlive-latex-base \
    texlive-latex-recommended \
    texlive-latex-extra \
    texlive-fonts-recommended \
    texlive-fonts-extra \
    texlive-xetex \
    texlive-luatex \
    texlive-science \
    texlive-pictures \
    latexmk \
    lmodern \
    fontconfig \
    ghostscript \
    firejail \
    poppler-utils \
&& apt-get clean && rm -rf /var/lib/apt/lists/*



# Copy the build artifacts (e.g., JAR or WAR) from the previous build stage
COPY --from=build /app/build/libs/bot.jar /app/bot.jar
COPY --from=build /app/icon.png /app/icon.png

# Run the application (if it’s a JAR file)
CMD ["java", "-jar", "/app/bot.jar"]




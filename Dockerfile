# Use an official Gradle image as a base image (or openjdk if you prefer)
FROM gradle:7.5-jdk17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the entire Gradle project, including the gradle wrapper
COPY . .

# Run the Gradle build (this will use gradlew wrapper)
RUN ./gradlew build --no-daemon

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
    poppler-utils \
    && apt-get clean && rm -rf /var/lib/apt/lists/*



# Copy the build artifacts (e.g., JAR or WAR) from the previous build stage
COPY --from=build /app/build/libs/bot.jar /app/bot.jar

# Run the application (if it’s a JAR file)
CMD ["java", "-jar", "/app/bot.jar"]




FROM gradle:7.4.0-jdk11-alpine AS build
COPY --chown=gradle:gradle . /app
RUN apk --no-cache add curl
WORKDIR /app
ENTRYPOINT ["gradle", "bootRun"]

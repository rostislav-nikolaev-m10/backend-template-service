# the first stage of our build will extract the layers
ARG FROM_IMAGE=651626103078.dkr.ecr.eu-central-1.amazonaws.com/amzncoretto:17.0.2

FROM ${FROM_IMAGE} as builder
WORKDIR app
COPY . .


ARG MAVEN_VERSION=3.8.6
ARG GH_TOKEN
ARG GH_USERNAME
ENV GITHUB_TOKEN=${GH_TOKEN}
ENV GITHUB_USERNAME=${GH_USERNAME}

RUN  mvn clean package -s settings.xml -Dskip.unit.tests=true && \
   version=$(xmllint --xpath "/*[name()='project']/*[name()='version']/text()" pom.xml) && \
   name=$(xmllint --xpath "/*[name()='project']/*[name()='name']/text()" pom.xml) && \
   echo $name:$version && \
   mv target/$name-$version.jar target/app.jar

FROM amazoncorretto:17.0.2-alpine
COPY --from=builder app/target/*.jar ./
COPY docker/resources/logback-json.xml ./

EXPOSE 8080
ENTRYPOINT ["java","-Dlogging.config=/logback-json.xml","-jar","/app.jar"]


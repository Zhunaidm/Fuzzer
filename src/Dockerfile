FROM anapsix/alpine-java:8_jdk
COPY . usr/fuzz/
WORKDIR usr/fuzz
RUN ./compile.sh DB_test.java
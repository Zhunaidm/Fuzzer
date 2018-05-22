FROM anapsix/alpine-java:8_jdk
COPY ./*.* usr/fuzz/
COPY lib usr/fuzz/lib/
WORKDIR usr/fuzz
RUN ./compile.sh DB_test.java
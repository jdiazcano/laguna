FROM ekidd/rust-musl-builder:stable as builder
RUN USER=root cargo new --bin laguna
WORKDIR ./laguna
COPY ./Cargo.lock ./Cargo.lock
COPY ./Cargo.toml ./Cargo.toml
RUN cargo build --release
RUN rm src/*.rs

ADD . ./

RUN rm ./target/x86_64-unknown-linux-musl/release/deps/laguna*
RUN cargo build --release

FROM alpine:latest
RUN mkdir /app
WORKDIR /app
RUN apk update && apk add --no-cache openssh libgit2-dev && rm -rf /var/cache/apk/*
COPY --from=builder /home/rust/src/laguna/target/x86_64-unknown-linux-musl/release/laguna /usr/local/bin/laguna
ENTRYPOINT ["laguna"]
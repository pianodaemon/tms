# Building stage
FROM golang:1.23.2-alpine AS builder

# Install required packages, including ca-certificates
RUN apk add --no-cache ca-certificates && \
    update-ca-certificates

WORKDIR /go/src/immortalcrabcorp.com/cfdi-engine

COPY go.mod ./
COPY go.sum ./

RUN go mod download

COPY pkg pkg
COPY internal internal
COPY cmd cmd

RUN CGO_ENABLED=0 \
    GOOS=linux    \
    GOARCH=amd64  \
    go build -ldflags="-w -s" -o /sidecar-cfdi-engine cmd/http/run.go

# Final image
FROM debian:bookworm

# Install CA certificates in the runtime image
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

ENV APP_DIR=/
COPY --from=builder /sidecar-cfdi-engine $APP_DIR
COPY scripts/run.sh $APP_DIR
WORKDIR $APP_DIR

RUN chmod +x $APP_DIR/run.sh

EXPOSE 9080

CMD ["/run.sh"]

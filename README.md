# Credentials Config Service

The Credentials Config Service manages and provides information about services and the credentials they are using. It 
returns the scope to be requested from the wallet per service and the credentials and issuers that are considered to be 
trusted for a certain service. 

[![FIWARE Security](https://nexus.lab.fiware.org/repository/raw/public/badges/chapters/security.svg)](https://www.fiware.org/developers/catalogue/)
[![License badge](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Container Repository on Quay](https://img.shields.io/badge/quay.io-fiware%2Fcredentials--config--service-grey?logo=red%20hat&labelColor=EE0000)](https://quay.io/repository/fiware/credentials-config-service)
[![Coverage Status](https://coveralls.io/repos/github/FIWARE/credentials-config-service/badge.svg?branch=main)](https://coveralls.io/github/FIWARE/credentials-config-service?branch=main)
[![Test](https://github.com/FIWARE/credentials-config-service/actions/workflows/test.yml/badge.svg)](https://github.com/FIWARE/credentials-config-service/actions/workflows/test.yml)
[![Release](https://github.com/FIWARE/credentials-config-service/actions/workflows/release.yml/badge.svg)](https://github.com/FIWARE/credentials-config-service/actions/workflows/release.yml)

## Background

In an DSBA-compliant framework, a [Verifier](https://github.com/FIWARE/VCVerifier)  is responsible to communicate with wallets
and verify the credentials they provide. To get this done, it needs information about:
- the credentials to be requested from a wallet
- the credentials and claims an issuer is allowed to issue

To do so, it requires a service that provides such information, e.g. the Credentials Config Service. See the following diagram 
on how the service integrates into the framework.

![overview-setup](doc/overview.png)

## Installation

### Container

The Credentials-Config-Service Service is provided as a container at [quay.io](https://quay.io/repository/fiware/credentials-config-service).
To store information about the services, a database has to be provided. In a local setup, you can for example use:
```shell
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_USER=user -e MYSQL_PASSWORD=password -e MYSQL_DATABASE=db mysql
```
and the start the service:
```shell
docker run --network host quay.io/fiware/credentials-config-service:0.0.1
```
After that, its accessible at ```localhost:8080```.

### Configuration

Configurations can be provided with the standard mechanisms of the [Micronaut-Framework](https://micronaut.io/), e.g. [environment variables or appliction.yaml file](https://docs.micronaut.io/3.1.3/guide/index.html#configurationProperties).
The following table concentrates on the most important configuration parameters:

| Property                              | Env-Var                                 | Description                                                           | Default                              |
|---------------------------------------|-----------------------------------------|-----------------------------------------------------------------------|--------------------------------------|
| `micronaut.server.port`               | `MICRONAUT_SERVER_PORT`                 | Server port to be used for the notfication proxy.                     | 8080                                 |
| `micronaut.metrics.enabled`           | `MICRONAUT_METRICS_ENABLED`             | Enable the metrics gathering                                          | true                                 |
| `datasources.default.url`             | `DATASOURCES_DEFAULT_URL`               | JDBC connection string to the database.                               | ```jdbc:mysql://localhost:3306/db``` |
| `datasources.default.driverClassName` | `DATASOURCES_DEFAULT_DRIVER_CLASS_NAME` | Driver to be used for the database connection.                        | ```com.mysql.cj.jdbc.Driver```       |
| `datasources.default.username`        | `DATASOURCES_DEFAULT_USERNAME`          | Username to authenticate at the database.                             | ```user```                           |
| `datasources.default.password`        | `DATASOURCES_DEFAULT_PASSWORD`          | Password to authenticate at the database.                             | ```password```                       |
| `datasources.default.dialect`         | `DATASOURCES_DEFAULT_DIALECT`           | Dialect to be used with the DB. Currently MYSQL and H2 are supported. | ```MYSQL```                          |

### Database

Credentials-Config-Service requires an SQL database. It currently supports MySql-compatible DBs and H2 (as an In-Memory DB for dev/test purposes).
Migrations are applied via [flyway](https://flywaydb.org/), see the [migration-scripts](./src/main/resources/db/migration) for the schema.

## Usage

The service provides the following API:
- [Credentials-Config-Service API](./api/credentials-config-service.yaml) 

It is used to manage the service-related entries and provides endpoints to retrieve the required information.


### Example

To have information about a service available, it first needs to be created.
An example request would look like:
```shell
curl -X 'POST' \
  'http://localhost:8080/service' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "packet-delivery-service",
  "credentials": [
    {
      "type": "VerifiableCredential",
      "trustedParticipantsLists": [
        "https://tir-pdc.ebsi.fiware.dev"
      ],
      "trustedIssuersLists": [
        "https://til-pdc.ebsi.fiware.dev"
      ]
    }
  ]
}'
```
Such configuration will define that the requested scope for authentication-requests to ```packet-delivery-service``` is 
```VerifiableCredential``` and that the issuer needs to be listed as a trusted-participant at 
```https://tir-pdc.ebsi.fiware.dev```  and that the information about the trusted-issuers should be retrieved from ```https://til-pdc.ebsi.fiware.dev```.

The verifier can access that information via:

```shell
curl --location 'localhost:8080/service/packet-delivery-service'
```

and receive:
```shell
{
  "id": "packet-delivery-service",
  "credentials": [
    {
      "type": "VerifiableCredential",
      "trustedParticipantsLists": [
        {
          "type": "ebsi",
          "url": "https://tir-pdc.ebsi.fiware.dev"
        }
      ],
      "trustedIssuersLists": [
        "https://til-pdc.ebsi.fiware.dev"
      ]
    }
  ]
}
```

The config service also supports GAIA-X Registries as participants list(even mixed configurations):
```shell
curl -X 'POST' \
  'http://localhost:8080/service' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "packet-delivery-service",
  "credentials": [
    {
      "type": "VerifiableCredential",
      "trustedParticipantsLists": [
        {
          "type": "ebsi",
          "url": "https://tir-pdc.ebsi.fiware.dev"
        },
        {
          "type": "gaia-x",
          "url": "https://registry.lab.gaia-x.eu"
        }
      ],
      "trustedIssuersLists": [
        "https://til-pdc.ebsi.fiware.dev"
      ]
    }
  ]
}'
```

and receive:
```shell
{
  "id": "packet-delivery-service",
  "credentials": [
    {
      "type": "VerifiableCredential",
      "trustedParticipantsLists": [
        {
          "type": "ebsi",
          "url": "https://tir-pdc.ebsi.fiware.dev"
        },
        {
          "type": "gaia-x",
          "url": "https://registry.lab.gaia-x.eu"
        }
      ],
      "trustedIssuersLists": [
        "https://til-pdc.ebsi.fiware.dev"
      ]
    }
  ]
}
```

Besides that, it's also possible to get just the scope to be requested:

```shell
curl --location 'localhost:8080/service/packet-delivery-service/scope'
```

and receive: 
```shell
[
  "VerifiableCredential"
]
```

## License

Credentials-Config-Service is licensed under the Apache License, Version 2.0. See LICENSE for the full license text.

© 2023 FIWARE Foundation e.V.

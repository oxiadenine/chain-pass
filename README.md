# Chain Pass

A project built with [JetBrains Compose Multiplatform](https://www.jetbrains.com/lp/compose-mpp/)
to save and manage passwords.

## Deployment

This project uses [Docker](https://www.docker.com/) for deployment.

To manage services run the following commands:

- Build and start services with `docker-compose -f ./docker-compose.yml up -d`
- Stop and terminate services with `docker-compose -f ./docker-compose.yml down`

## Distribution

To package applications run the following commands with Gradle:

- Create Desktop binaries with `desktop:packageDeb` or `desktop:packageExe`
- Create Android APKs with `android:build`
- Create Service binaries with `service:packageDeb` or `service:packageExe`

**Packaging requires JDK 17 to be installed.**

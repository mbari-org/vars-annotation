# vars-annotation

![MBARI logo](assets/images/mbari-logo.png)

## Overview

[![DOI](https://zenodo.org/badge/90881605.svg)](https://zenodo.org/badge/latestdoi/90881605)

VARS Annotation is [MBARI](https://www.mbari.org)'s desktop application for creating and editing video annotations from ROV and AUV deployments. It is part of MBARI's [VARS](https://github.com/mbari-org) (Video Annotation and Reference System) software stack.

**VARS Annotation is not a standalone application.** It requires a set of backend microservices to function. See [Getting Started](#getting-started) below.

![VARS Annotation](assets/images/vars-annotation.png)

### Video Playback

VARS can annotate video in several modes:

- **Real-time** — annotate live video as it is being captured
- **Video tape** — annotate from tape decks that support RS-422
- **Video files** — annotate pre-recorded files via [Sharktopoda](https://github.com/mbari-org/Sharktopoda), an external macOS video player that supports both playback and direct bounding-box drawing on video for spatial annotations

## Getting Started

VARS requires a running backend stack of microservices. The easiest way to get everything up and running is [vars-quickstart-public](https://github.com/mbari-org/vars-quickstart-public) — a Docker-based orchestrator that brings up all required services together.

### Prerequisites

- [Docker Engine 20.10+](https://docs.docker.com/get-docker/) with Docker Compose V2
- Bash 4.0+
- 8 GB RAM minimum (16 GB recommended)

### Launch the backend services

```bash
git clone https://github.com/mbari-org/vars-quickstart-public.git
cd vars-quickstart-public
./varsq configure etc/env/localhost.env   # select your target environment
./varsq mkcert                            # generate SSL certificates
./varsq start                             # start all services
./varsq status                            # verify everything is running
```

The stack brings up the following services:

| Service | Port | Purpose |
|---------|------|---------|
| annosaurus | 8082 | Annotation storage and retrieval |
| vampire-squid | 8084 | Video asset and sequence management |
| oni | 8083 | Knowledge base and taxonomy |
| panoptes | 8085 | Framegrab and image management |
| raziel | 8400 | API gateway and authentication |
| charybdis | 8086 | Cross-service query aggregation |
| beholder | 8088 | Image capture cache |
| skimmer | 8089 | Image processing pipeline |
| nginx | 80 / 443 | Reverse proxy with SSL termination |

### Install and connect VARS Annotation

Once the backend is running:

1. [Download VARS Annotation](https://github.com/mbari-org/vars-annotation/releases)
2. Launch it and open **Settings** (gear icon in the toolbar)
3. Enter the URL of your Raziel configuration server and your credentials
4. Click **Test** to verify the connection, then **OK**

See the [setup guide](setup.md) for detailed configuration instructions, including video player setup.

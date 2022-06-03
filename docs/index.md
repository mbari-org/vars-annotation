# vars-annotation

![MBARI logo](assets/images/mbari-logo.png)

## Overview

[![Build Status](https://travis-ci.org/mbari-media-management/vars-annotation.svg?branch=master)](https://travis-ci.org/mbari-media-management/vars-annotation)  [![DOI](https://zenodo.org/badge/90881605.svg)](https://zenodo.org/badge/latestdoi/90881605)

MBARI's Video Annotation and Reference System's user interface for creating and editing video annotations. This version is a complete rewrite of the older [VARS](https://hohonuuli.github.io/vars/) system. It is targeted at more modern video workflows and is part of [MBARI's Media Management](https://mbari-media-management.github.io/) software stack. This is NOT a standalone application. There are a number of external services that need to be deployed in order for this application to function.

This system is the main annotation application used at [MBARI](https://www.mbari.org).

![VARS Annotation](assets/images/vars-annotation.png)

### Video Playback

VARS can be used to annotate video:

- in real-time
- from video tape, if your deck supports RS422
- from video files via external video players such as [Sharktopoda](https://github.com/mbari-media-management/Sharktopoda), [jsharktopoda](https://github.com/mbari-media-management/jsharktopoda), or [Cthulhu](https://github.com/mbari-media-management/cthulhu).

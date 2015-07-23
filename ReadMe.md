#Itylos.io

[![Join the chat at https://gitter.im/itylos-io/core](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/itylos-io/core?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/itylos-io/core.svg?branch=master)](https://travis-ci.org/itylos-io/core)

## Introduction

[Itulos](https://itylos.io) is a low-budget DIY home security alarm targeted mainly for the Raspberry Pi, but it can be
   installed on any platform. Itylos.io is a platform from which the user can manage an alarm system. E.g. add sensors,
   add zones, trigger external events etc..
   
## Core
Itylos.io core is the heart of the alarm system. Core is the rest API where all interactions with Itylos.io are handled. 
We have a detailed rest API [description](https://itylos.io/docs). Core is written in scala (spray & akka) and uses
mongo db for persisted storage.

# ReportPortal. TFS Integration

---
Mac OS users need to add proxy resolve for docker at first in order to build docker container.

To do so you need to follow this steps:
1. Go to <code>System Preferences/Network/Wi-Fi (or Ethernet)/click Advanced.../Proxies</code>
2. Check <b>Web Proxy (HTTP)</b> checkbox and add <code>localhost:2375</code> to Web Proxy Server
3. Command line <code>brew install socat</code> (Homebrew should be pre-installed)

remember to uncheck <b>Web Proxy (HTTP)</b> checkbox <b>after</b> you build an image.

---

Ubuntu users:
0. Install jdk <code>sudo apt-get install openjdk-8-jdk</code>
1. Install socat <code>sudo apt-get install socat</code>
---

### Build your docker container

 * In new terminal tab run <code>socat -d TCP-LISTEN:2375,range=127.0.0.1/32,reuseaddr,fork UNIX:/var/run/docker.sock</code>
 
 * <code>gradle buildDocker</code>
 
 * Terminate socat process

---

[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![stackoverflow](https://img.shields.io/badge/reportportal-stackoverflow-orange.svg?style=flat)](http://stackoverflow.com/questions/tagged/reportportal)

[![Build Status](https://travis-ci.org/reportportal/service-tfs.svg?branch=master)](https://travis-ci.org/reportportal/service-tfs)


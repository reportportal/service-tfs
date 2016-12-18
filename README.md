# ReportPortal. TFS Integration

---
#### mac users need to add proxy resolve for docker at first in order to build docker container.

##### To do so you need to follow this steps:
1. Go to <code>System Preferences/Network/Wi-Fi (or Ethernet)/click <button>Advanced...</button>/Proxies</code>
2. Check <b>Web Proxy (HTTP)</b> checkbox and add <code>localhost:2375</code> to Web Proxy Server
3. Command line <code>brew install socat</code> (Homebrew should be pre-installed)
4. <code>socat -d TCP-LISTEN:2375,range=127.0.0.1/32,reuseaddr,fork UNIX:/var/run/docker.sock</code>

remember to uncheck <b>Web Proxy (HTTP)</b> checkbox <b>after</b> you build an image.

---

### Build your docker container
 
 * <code>gradle buildDocker</code>

---

[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![stackoverflow](https://img.shields.io/badge/reportportal-stackoverflow-orange.svg?style=flat)](http://stackoverflow.com/questions/tagged/reportportal)

[![Build Status](https://travis-ci.org/reportportal/service-tfs.svg?branch=master)](https://travis-ci.org/reportportal/service-tfs)


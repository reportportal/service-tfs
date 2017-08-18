# ReportPortal. TFS Integration

#### Mac OS users need to add proxy resolve for docker at first in order to build docker container:
* Go to <code>System Preferences/Network/Wi-Fi (or Ethernet)/click Advanced.../Proxies</code>
* Check <b>Web Proxy (HTTP)</b> checkbox and add <code>localhost:2375</code> to Web Proxy Server
* Command line <code>brew install socat</code> (Homebrew should be pre-installed)

remember to uncheck <b>Web Proxy (HTTP)</b> checkbox <b>after</b> you build an image.

---

#### Ubuntu users:
* Install jdk <code>sudo apt-get install openjdk-8-jdk</code>
* Install socat <code>sudo apt-get install socat</code>

---

### Build your docker container
#### Ubuntu and Mac users:

 * In new terminal tab run `socat -d TCP-LISTEN:2375,range=127.0.0.1/32,reuseaddr,fork UNIX:/var/run/docker.sock`
 
 * `gradle buildDocker`
 
 * Terminate `socat` process
  
#### Windows users:

 * [before] If you are using latest Docker and getting error `org.apache.http.conn.HttpHostConnectException: Connect to localhost:2375` - right click on Docker tray icon / Settings / General. Select checkbox `Expose daemon on tcp://localhost:2375 without TLS`

 * `gradle buildDocker`

---

#### Notes
Service contains `TFS SDK` from Microsoft. You may <a href="https://github.com/Microsoft/team-explorer-everywhere/releases" target="_blank">download</a> latest SDK and update JAR in <code>libs</code> folder. Then update `build.gradle` according to your changes

---

[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![stackoverflow](https://img.shields.io/badge/reportportal-stackoverflow-orange.svg?style=flat)](http://stackoverflow.com/questions/tagged/reportportal)

[![Build Status](https://travis-ci.org/reportportal/service-tfs.svg?branch=master)](https://travis-ci.org/reportportal/service-tfs)


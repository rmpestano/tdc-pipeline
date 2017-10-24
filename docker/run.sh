#!/bin/sh
docker run -it --name tdc-pipeline -p 8080:8080 -v ~/db:/opt/jboss/db tdc-pipeline

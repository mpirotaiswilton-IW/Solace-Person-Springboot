FROM python:3-alpine

COPY solace-init.py /tmp/solace-init.py

# WORKDIR /tmp

USER root
RUN python -m pip install --upgrade pip
RUN python -m pip install requests
USER 1001

ENTRYPOINT [ "python", "/tmp/solace-init.py" ]
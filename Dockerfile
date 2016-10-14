
FROM scratch

LABEL Name="jboss-datavirt-6/jdv-extensions" \
      Version="latest" \
      Release="latest" \
      Architecture="x86_64" 

COPY deployments /injected/deployments

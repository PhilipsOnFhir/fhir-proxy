# FHIR Proxy
## Introduction
The FHIRProxy project implements a FHIR Proxy. It it used to implement and 
test FHIR operations that are not commonly supported by the commonly available
FHIR Servers.
As the name states, it is a FHIR Server Proxy, meaning that it sits in between
A FHIR Server and a FHIR client. In order to do so, it presents a FHIR Server
interface. 
All requests that are not related to the implemented functionality are forwarded
to the FHIR Server. The requests related to the implemented functionality is 
handled by the FHIR Proxy.

## Supported FHIR versions
FHIR Proxy has been implemented for the following FHIR versions:
 * STU3
 * R4


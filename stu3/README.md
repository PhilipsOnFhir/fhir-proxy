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

## Supported implementation guides
The following operations are supported:
 * Bulk Data
    * [baseUrl]/$export
    * [baseUrl]/Patient/$export
    * [baseUrl]/Group/$export
    * [baseUrl]/$import
 * ClinicalReasoning
    * [baseUrl]/ActivityDefinition/&apply
    * [baseUrl]/PlanDefinition/&apply
    * [baseUrl]/StructureMap/&transform
    * [baseUrl]/Questionnaire/$populate

## Compile and run
The following steps will allow you to run the server:
 1. Download the repository from github
 2. Run a (HAPI) FHIR server at localhost:9404 (required for running the tests)
 3. Edit the src/main/resources/application.properties and set proxy.fhirserver.url
    to the url of the FHIR server you want to use.
 4. Run the proxy mvn spring-boot:run 
 
## Security
Only unprotected severs are currently supported.

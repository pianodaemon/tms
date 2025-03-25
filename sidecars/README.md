### Elements which compose the DATA OPERATIONAL SIDECARS (DOS)

Sidecars that handle the day-to-day operations (not optimized for complex queries and analytics). 

| Project | Artifact | Brief description |
| ------ | ------ | ------ |
| `Fiscal engine` | cfdi-engine     | Sidecar that gears up cfdi workloads as messages |
| `Fiscal engine` | cfdi-dozer      | Autonomous module tracking which workloads have reached their time to be processed |
| `Fiscal engine` | cfdi-processor  | Autonomous module adapting workloads to the PAC's specifications to issue/cancel cfdi documents |
| `Fiscal engine` | cfdi-uploader   | Autonomous module transfering cfdi documents from PAC's infrastructure to our cloud |
| `Fiscal engine` | cfdi-fruitless  | Autonomous module that fetches cfdi documents and manage a few PAC's configurations |

# Jenkins CI Pipeline

`Jenkinsfile` runs the configured TestNG suite against the isolated Docker AUT, publishes test and Allure reports, and reconciles the RTM. The GitHub Actions trigger workflow dispatches a build through authenticated Jenkins API access on pushes to `main` or manual dispatch; it does not run against pull-request branches or wait for the Jenkins build to finish. The GitHub Actions smoke/API workflow remains the pull-request test gate.

## Jenkins prerequisites

- Java 21, Maven 3.9+, Docker Compose, and the configured Playwright browser dependencies on the agent.
- Jenkins Pipeline, Credentials Binding, JUnit, AnsiColor, and Allure plugins.
- The Jenkins agent container must join the shared Docker bridge `docker_qa-network`. The AUT and SQL Server are then reachable as `http://nopcommerce_web` and `nopcommerce_database:1433`; host-run tests use `http://127.0.0.1:5000` and `127.0.0.1:1433`.

## Store test credentials in Jenkins

Create these four **Secret text** credentials in the Jenkins Credentials Store. The IDs must match the `withCredentials` bindings in `Jenkinsfile`:

| Credential ID | Value |
|---|---|
| `nop-admin-email` | Admin email for the seeded test store |
| `nop-admin-password` | Admin password for the seeded test store |
| `nop-db-user` | SQL Server login, usually `sa` for the isolated container |
| `nop-db-password` | The same SQL password supplied to the Docker stack |

Do not use a setup script that creates credentials or generates tokens. If the previously checked-in setup script was used, revoke the tokens it created and replace any affected credentials.

## Configure GitHub-to-Jenkins dispatch

Add these GitHub repository secrets when Jenkins dispatch is desired:

- `JENKINS_URL`: Jenkins base URL, without a trailing slash.
- `JENKINS_USER`: Dedicated Jenkins service account with only the access needed to read and build this job.
- `JENKINS_API_TOKEN`: API token for that service account, stored only in GitHub Secrets.

The trigger workflow skips when `JENKINS_URL` is absent. If a URL is set but its username or API token is missing, or Jenkins rejects the request, the workflow fails. There is no job-level shared trigger token in the repository. Revoke and replace any token that was previously configured from the checked-in `authToken` value.

## Pipeline reports

Jenkins archives Surefire/TestNG results, Allure output, RTM documents, and failure evidence. Configure the pipeline's `SUITE` parameter to select smoke, API, UI, DB, accessibility, NFR, or full regression coverage. The dispatch workflow passes the selected suite and browser to Jenkins.

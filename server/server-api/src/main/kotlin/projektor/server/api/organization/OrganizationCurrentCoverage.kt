package projektor.server.api.organization

import projektor.server.api.repository.coverage.RepositoryCurrentCoverage

data class OrganizationCurrentCoverage(val repositories: List<RepositoryCurrentCoverage>)

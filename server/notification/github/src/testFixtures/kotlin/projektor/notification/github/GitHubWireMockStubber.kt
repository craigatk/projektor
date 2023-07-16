package projektor.notification.github

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.patch
import com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class GitHubWireMockStubber(private val wireMockServer: WireMockServer) {
    fun stubRepositoryRequests(orgName: String, repoName: String) {
        val installationId = 1
        val accessToken = "v1.1f699f1069f60xxx"
        val repositoryId = 2
        stubApp()
        stubGetRepoInstallation(orgName, repoName, installationId)
        stubCreateInstallationAccessToken(installationId, accessToken, repositoryId)

        stubGetRepository(orgName, repoName, repositoryId)
        stubCreateInstallationAccessToken(repositoryId, accessToken, repositoryId)
    }

    fun stubApp() {
        wireMockServer.stubFor(
            get(urlEqualTo("/app"))
                .willReturn(
                    aResponse().withBody(
                        """
            {
              "id": 1,
              "slug": "octoapp",
              "node_id": "MDExOkludGVncmF0aW9uMQ==",
              "owner": {
                "login": "craigatk",
                "id": 1,
                "node_id": "MDEyOk9yZ2FuaXphdGlvbjE=",
                "url": "https://api.github.com/orgs/github",
                "gravatar_id": "",
                "type": "User",
                "site_admin": true
              },
              "name": "Octocat App",
              "description": "",
              "external_url": "https://example.com",
              "html_url": "https://github.com/apps/octoapp",
              "created_at": "2017-07-08T16:18:44-04:00",
              "updated_at": "2017-07-08T16:18:44-04:00",
              "permissions": {
                "metadata": "read",
                "contents": "read",
                "issues": "write",
                "single_file": "write"
              },
              "events": [
                "push",
                "pull_request"
              ]
            }
                        """.trimIndent()
                    )
                )
        )
    }

    fun stubGetRepoInstallation(orgName: String, repoName: String, installationId: Int) {
        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/installation"))
                .willReturn(
                    aResponse().withBody(
                        """
{
    "id": $installationId,
    "account": {
      "login": "$orgName",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "type": "User",
      "site_admin": false
    },
    "access_tokens_url": "https://api.github.com/installations/1/access_tokens",
    "repositories_url": "https://api.github.com/installation/repositories",
    "html_url": "https://github.com/organizations/github/settings/installations/1",
    "app_id": 1,
    "target_id": 1,
    "target_type": "Organization",
    "permissions": {
      "checks": "write",
      "metadata": "read",
      "contents": "read"
    },
    "events": [
      "push",
      "pull_request"
    ],
    "single_file_name": "config.yaml",
    "has_multiple_single_files": true,
    "single_file_paths": [
      "config.yml",
      ".github/issue_TEMPLATE.md"
    ],
    "repository_selection": "selected",
    "created_at": "2017-07-08T16:18:44-04:00",
    "updated_at": "2017-07-08T16:18:44-04:00",
    "app_slug": "github-actions"
  }
"""
                    )
                )
        )
    }

    fun stubGetRepoInstallationNotFound(orgName: String, repoName: String) {
        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/installation"))
                .willReturn(
                    aResponse().withStatus(404)
                )
        )
    }

    fun stubCreateInstallationAccessToken(installationId: Int, token: String, repositoryId: Int) {
        wireMockServer.stubFor(
            post(urlEqualTo("/app/installations/$installationId/access_tokens"))
                .willReturn(
                    aResponse().withBody(
                        """
{
  "token": "$token",
  "expires_at": "2016-07-11T22:14:10Z",
  "permissions": {
    "issues": "write",
    "contents": "read"
  },
  "repository_selection": "selected",
  "repositories": [
    {
      "id": $repositoryId,
      "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
      "name": "Hello-World",
      "full_name": "octocat/Hello-World",
      "owner": {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "type": "User",
        "site_admin": false
      },
      "private": false,
      "html_url": "https://github.com/octocat/Hello-World",
      "description": "This your first repo!",
      "fork": false,
      "url": "https://api.github.com/repos/octocat/Hello-World",
      "homepage": "https://github.com",
      "language": null,
      "forks_count": 9,
      "stargazers_count": 80,
      "watchers_count": 80,
      "size": 108,
      "default_branch": "master",
      "open_issues_count": 0,
      "is_template": true,
      "topics": [
        "octocat",
        "atom",
        "electron",
        "api"
      ],
      "has_issues": true,
      "has_projects": true,
      "has_wiki": true,
      "has_pages": false,
      "has_downloads": true,
      "archived": false,
      "disabled": false,
      "visibility": "public",
      "pushed_at": "2011-01-26T19:06:43Z",
      "created_at": "2011-01-26T19:01:12Z",
      "updated_at": "2011-01-26T19:14:43Z",
      "permissions": {
        "admin": false,
        "push": false,
        "pull": true
      },
      "allow_rebase_merge": true,
      "template_repository": null,
      "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
      "allow_squash_merge": true,
      "delete_branch_on_merge": true,
      "allow_merge_commit": true,
      "subscribers_count": 42,
      "network_count": 0,
      "license": {
        "key": "mit",
        "name": "MIT License",
        "url": "https://api.github.com/licenses/mit",
        "spdx_id": "MIT",
        "node_id": "MDc6TGljZW5zZW1pdA==",
        "html_url": "https://github.com/licenses/mit"
      },
      "forks": 1,
      "open_issues": 1,
      "watchers": 1
    }
  ]
}
"""
                    )
                )
        )
    }

    fun stubGetRepository(orgName: String, repoName: String, repositoryId: Int) {
        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName"))
                .willReturn(
                    aResponse().withBody(
                        """
{
  "id": $repositoryId,
  "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
  "name": "$repoName",
  "full_name": "$orgName/$repoName",
  "owner": {
    "login": "$orgName",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "type": "User",
    "site_admin": false
  },
  "private": false,
  "html_url": "https://github.com/octocat/Hello-World",
  "description": "This your first repo!",
  "fork": false,
  "url": "https://api.github.com/repos/$orgName/$repoName",
  "homepage": "https://github.com",
  "language": null,
  "forks_count": 9,
  "forks": 9,
  "stargazers_count": 80,
  "watchers_count": 80,
  "watchers": 80,
  "size": 108,
  "default_branch": "master",
  "open_issues_count": 0,
  "open_issues": 0,
  "is_template": true,
  "topics": [
    "octocat",
    "atom",
    "electron",
    "api"
  ],
  "has_issues": true,
  "has_projects": true,
  "has_wiki": true,
  "has_pages": false,
  "has_downloads": true,
  "archived": false,
  "disabled": false,
  "visibility": "public",
  "pushed_at": "2011-01-26T19:06:43Z",
  "created_at": "2011-01-26T19:01:12Z",
  "updated_at": "2011-01-26T19:14:43Z",
  "allow_rebase_merge": true,
  "template_repository": null,
  "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
  "allow_squash_merge": true,
  "delete_branch_on_merge": true,
  "allow_merge_commit": true,
  "subscribers_count": 42,
  "network_count": 0,
  "license": {
    "key": "mit",
    "name": "MIT License",
    "spdx_id": "MIT",
    "url": "https://api.github.com/licenses/mit",
    "node_id": "MDc6TGljZW5zZW1pdA=="
  },
  "organization": {
    "login": "$orgName",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "type": "Organization",
    "site_admin": false
  },
  "parent": {
    "id": 1296269,
    "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
    "name": "Hello-World",
    "full_name": "$orgName/$repoName",
    "owner": {
      "login": "$orgName",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "type": "User",
      "site_admin": false
    },
    "private": false,
    "html_url": "https://github.com/octocat/Hello-World",
    "description": "This your first repo!",
    "fork": false,
    "url": "https://api.github.com/repos/octocat/Hello-World",
    "homepage": "https://github.com",
    "language": null,
    "forks_count": 9,
    "stargazers_count": 80,
    "watchers_count": 80,
    "size": 108,
    "default_branch": "master",
    "open_issues_count": 0,
    "is_template": true,
    "topics": [
      "octocat",
      "atom",
      "electron",
      "api"
    ],
    "has_issues": true,
    "has_projects": true,
    "has_wiki": true,
    "has_pages": false,
    "has_downloads": true,
    "archived": false,
    "disabled": false,
    "visibility": "public",
    "pushed_at": "2011-01-26T19:06:43Z",
    "created_at": "2011-01-26T19:01:12Z",
    "updated_at": "2011-01-26T19:14:43Z",
    "permissions": {
      "admin": false,
      "push": false,
      "pull": true
    },
    "allow_rebase_merge": true,
    "template_repository": null,
    "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
    "allow_squash_merge": true,
    "delete_branch_on_merge": true,
    "allow_merge_commit": true,
    "subscribers_count": 42,
    "network_count": 0,
    "license": {
      "key": "mit",
      "name": "MIT License",
      "url": "https://api.github.com/licenses/mit",
      "spdx_id": "MIT",
      "node_id": "MDc6TGljZW5zZW1pdA==",
      "html_url": "https://api.github.com/licenses/mit"
    },
    "forks": 1,
    "open_issues": 1,
    "watchers": 1
  },
  "source": {
    "id": 1296269,
    "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
    "name": "Hello-World",
    "full_name": "$orgName/$repoName",
    "owner": {
      "login": "$orgName",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "html_url": "https://github.com/octocat",
      "type": "User",
      "site_admin": false
    },
    "private": false,
    "html_url": "https://github.com/octocat/Hello-World",
    "description": "This your first repo!",
    "fork": false,
    "url": "https://api.github.com/repos/octocat/Hello-World",
    "homepage": "https://github.com",
    "language": null,
    "forks_count": 9,
    "stargazers_count": 80,
    "watchers_count": 80,
    "size": 108,
    "default_branch": "master",
    "open_issues_count": 0,
    "is_template": true,
    "topics": [
      "octocat",
      "atom",
      "electron",
      "api"
    ],
    "has_issues": true,
    "has_projects": true,
    "has_wiki": true,
    "has_pages": false,
    "has_downloads": true,
    "archived": false,
    "disabled": false,
    "visibility": "public",
    "pushed_at": "2011-01-26T19:06:43Z",
    "created_at": "2011-01-26T19:01:12Z",
    "updated_at": "2011-01-26T19:14:43Z",
    "permissions": {
      "admin": false,
      "push": false,
      "pull": true
    },
    "allow_rebase_merge": true,
    "template_repository": null,
    "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
    "allow_squash_merge": true,
    "delete_branch_on_merge": true,
    "allow_merge_commit": true,
    "subscribers_count": 42,
    "network_count": 0,
    "license": {
      "key": "mit",
      "name": "MIT License",
      "url": "https://api.github.com/licenses/mit",
      "spdx_id": "MIT",
      "node_id": "MDc6TGljZW5zZW1pdA==",
      "html_url": "https://api.github.com/licenses/mit"
    },
    "forks": 1,
    "open_issues": 1,
    "watchers": 1
  }
}
"""
                    )
                )
        )
    }

    fun stubGetIssue(orgName: String, repoName: String, issueId: Int) {
        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/issues/$issueId"))
                .willReturn(
                    aResponse().withBody(
                        """
{
  "id": $issueId,
  "node_id": "MDU6SXNzdWUx",
  "url": "https://api.github.com/repos/octocat/Hello-World/issues/1347",
  "repository_url": "https://api.github.com/repos/octocat/Hello-World",
  "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/1347/labels{/name}",
  "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/1347/comments",
  "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/1347/events",
  "html_url": "https://github.com/octocat/Hello-World/issues/1347",
  "number": $issueId,
  "state": "open",
  "title": "Found a bug",
  "body": "I'm having a problem with this.",
  "user": {
    "login": "$orgName",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "type": "User",
    "site_admin": false
  },
  "labels": [
    {
      "id": 208045946,
      "node_id": "MDU6TGFiZWwyMDgwNDU5NDY=",
      "url": "https://api.github.com/repos/octocat/Hello-World/labels/bug",
      "name": "bug",
      "description": "Something isn't working",
      "color": "f29513",
      "default": true
    }
  ],
  "assignee": {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "site_admin": false
  },
  "assignees": [
    {
      "login": "octocat",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "html_url": "https://github.com/octocat",
      "type": "User",
      "site_admin": false
    }
  ],
  "milestone": {
    "url": "https://api.github.com/repos/octocat/Hello-World/milestones/1",
    "html_url": "https://github.com/octocat/Hello-World/milestones/v1.0",
    "labels_url": "https://api.github.com/repos/octocat/Hello-World/milestones/1/labels",
    "id": 1002604,
    "node_id": "MDk6TWlsZXN0b25lMTAwMjYwNA==",
    "number": 1,
    "state": "open",
    "title": "v1.0",
    "description": "Tracking milestone for version 1.0",
    "creator": {
      "login": "octocat",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "html_url": "https://github.com/octocat",
      "type": "User",
      "site_admin": false
    },
    "open_issues": 4,
    "closed_issues": 8,
    "created_at": "2011-04-10T20:09:31Z",
    "updated_at": "2014-03-03T18:58:10Z",
    "closed_at": "2013-02-12T13:22:01Z",
    "due_on": "2012-10-09T23:39:01Z"
  },
  "locked": true,
  "active_lock_reason": "too heated",
  "comments": 0,
  "pull_request": {
    "url": "https://api.github.com/repos/octocat/Hello-World/pulls/1347",
    "html_url": "https://github.com/octocat/Hello-World/pull/1347",
    "diff_url": "https://github.com/octocat/Hello-World/pull/1347.diff",
    "patch_url": "https://github.com/octocat/Hello-World/pull/1347.patch"
  },
  "closed_at": null,
  "created_at": "2011-04-22T13:33:48Z",
  "updated_at": "2011-04-22T13:33:48Z",
  "closed_by": {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "type": "User",
    "site_admin": false
  },
  "author_association": "collaborator"
}
"""
                    )
                )
        )
    }

    fun stubAddComment(orgName: String, repoName: String, issueId: Int) {
        wireMockServer.stubFor(
            post(urlEqualTo("/repos/$orgName/$repoName/issues/$issueId/comments"))
                .willReturn(
                    aResponse().withBody(
                        """
{
  "id": $issueId,
  "node_id": "MDEyOklzc3VlQ29tbWVudDE=",
  "url": "https://api.github.com/repos/octocat/Hello-World/issues/comments/1",
  "html_url": "https://github.com/octocat/Hello-World/issues/1347#issuecomment-1",
  "body": "Me too",
  "user": {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "type": "User",
    "site_admin": false
  },
  "created_at": "2011-04-14T16:00:49Z",
  "updated_at": "2011-04-14T16:00:49Z",
  "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/1347",
  "author_association": "collaborator"
}
"""
                    )
                )
        )
    }

    fun stubListPullRequests(orgName: String, repoName: String, branchNames: List<String>, commitShas: List<String> = listOf()) {
        val shas = if (commitShas.isEmpty()) branchNames.map { "$it-sha" } else commitShas

        val responseBody = branchNames.mapIndexed { idx, branchName ->
            """
                {
    "url": "https://api.github.com/repos/$orgName/$repoName/pulls/${idx + 1}",
    "id": 1,
    "node_id": "MDExOlB1bGxSZXF1ZXN0MQ==",
    "number": ${idx + 1},
    "state": "open",
    "locked": true,
    "title": "Amazing new feature",
    "user": {
      "login": "octocat",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "type": "User",
      "site_admin": false
    },
    "body": "Please pull these awesome changes in!",
    "labels": [
      {
        "id": 208045946,
        "node_id": "MDU6TGFiZWwyMDgwNDU5NDY=",
        "url": "https://api.github.com/repos/octocat/Hello-World/labels/bug",
        "name": "bug",
        "description": "Something isn't working",
        "color": "f29513",
        "default": true
      }
    ],
    "milestone": {
      "url": "https://api.github.com/repos/octocat/Hello-World/milestones/1",
      "html_url": "https://github.com/octocat/Hello-World/milestones/v1.0",
      "labels_url": "https://api.github.com/repos/octocat/Hello-World/milestones/1/labels",
      "id": 1002604,
      "node_id": "MDk6TWlsZXN0b25lMTAwMjYwNA==",
      "number": 1,
      "state": "open",
      "title": "v1.0",
      "description": "Tracking milestone for version 1.0",
      "creator": {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "type": "User",
        "site_admin": false
      },
      "open_issues": 4,
      "closed_issues": 8,
      "created_at": "2011-04-10T20:09:31Z",
      "updated_at": "2014-03-03T18:58:10Z",
      "closed_at": "2013-02-12T13:22:01Z",
      "due_on": "2012-10-09T23:39:01Z"
    },
    "active_lock_reason": "too heated",
    "created_at": "2011-01-26T19:01:12Z",
    "updated_at": "2011-01-26T19:01:12Z",
    "closed_at": "2011-01-26T19:01:12Z",
    "merged_at": "2011-01-26T19:01:12Z",
    "merge_commit_sha": "e5bd3914e2e596debea16f433f57875b5b90bcd6",
    "assignee": {
      "login": "octocat",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "type": "User",
      "site_admin": false
    },
    "assignees": [
      {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "type": "User",
        "site_admin": false
      },
      {
        "login": "hubot",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/hubot_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/hubot",
        "type": "User",
        "site_admin": true
      }
    ],
    "requested_reviewers": [
      {
        "login": "other_user",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/other_user_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/other_user",
        "type": "User",
        "site_admin": false
      }
    ],
    "requested_teams": [
      {
        "id": 1,
        "node_id": "MDQ6VGVhbTE=",
        "url": "https://api.github.com/teams/1",
        "html_url": "https://api.github.com/teams/justice-league",
        "name": "Justice League",
        "slug": "justice-league",
        "description": "A great team.",
        "privacy": "closed",
        "permission": "admin",
        "members_url": "https://api.github.com/teams/1/members{/member}",
        "repositories_url": "https://api.github.com/teams/1/repos"
      }
    ],
    "head": {
      "label": "$orgName:$branchName",
      "ref": "$branchName",
      "sha": "${shas[idx]}",
      "user": {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
        "gravatar_id": "",
        "url": "https://api.github.com/users/octocat",
        "type": "User",
        "site_admin": false
      },
      "repo": {
        "id": 1296269,
        "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
        "name": "Hello-World",
        "full_name": "octocat/Hello-World",
        "owner": {
          "login": "octocat",
          "id": 1,
          "node_id": "MDQ6VXNlcjE=",
          "type": "User",
          "site_admin": false
        },
        "private": false,
        "html_url": "https://github.com/octocat/Hello-World",
        "description": "This your first repo!",
        "fork": false,
        "homepage": "https://github.com",
        "language": null,
        "forks_count": 9,
        "stargazers_count": 80,
        "watchers_count": 80,
        "size": 108,
        "default_branch": "master",
        "open_issues_count": 0,
        "is_template": true,
        "topics": [
          "octocat",
          "atom",
          "electron",
          "api"
        ],
        "has_issues": true,
        "has_projects": true,
        "has_wiki": true,
        "has_pages": false,
        "has_downloads": true,
        "archived": false,
        "disabled": false,
        "visibility": "public",
        "pushed_at": "2011-01-26T19:06:43Z",
        "created_at": "2011-01-26T19:01:12Z",
        "updated_at": "2011-01-26T19:14:43Z",
        "permissions": {
          "admin": false,
          "push": false,
          "pull": true
        },
        "allow_rebase_merge": true,
        "template_repository": null,
        "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
        "allow_squash_merge": true,
        "delete_branch_on_merge": true,
        "allow_merge_commit": true,
        "subscribers_count": 42,
        "network_count": 0,
        "license": {
          "key": "mit",
          "name": "MIT License",
          "url": "https://api.github.com/licenses/mit",
          "spdx_id": "MIT",
          "node_id": "MDc6TGljZW5zZW1pdA==",
          "html_url": "https://github.com/licenses/mit"
        },
        "forks": 1,
        "open_issues": 1,
        "watchers": 1
      }
    },
    "base": {
      "label": "octocat:master",
      "ref": "master",
      "sha": "6dcb09b5b57875f334f61aebed695e2e4193db5e",
      "user": {
        "login": "octocat",
        "id": 1,
        "node_id": "MDQ6VXNlcjE=",
        "type": "User",
        "site_admin": false
      },
      "repo": {
        "id": 1296269,
        "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
        "name": "Hello-World",
        "full_name": "octocat/Hello-World",
        "owner": {
          "login": "octocat",
          "id": 1,
          "node_id": "MDQ6VXNlcjE=",
          "type": "User",
          "site_admin": false
        },
        "private": false,
        "html_url": "https://github.com/octocat/Hello-World",
        "description": "This your first repo!",
        "fork": false,
        "homepage": "https://github.com",
        "language": null,
        "forks_count": 9,
        "stargazers_count": 80,
        "watchers_count": 80,
        "size": 108,
        "default_branch": "master",
        "open_issues_count": 0,
        "is_template": true,
        "topics": [
          "octocat",
          "atom",
          "electron",
          "api"
        ],
        "has_issues": true,
        "has_projects": true,
        "has_wiki": true,
        "has_pages": false,
        "has_downloads": true,
        "archived": false,
        "disabled": false,
        "visibility": "public",
        "pushed_at": "2011-01-26T19:06:43Z",
        "created_at": "2011-01-26T19:01:12Z",
        "updated_at": "2011-01-26T19:14:43Z",
        "permissions": {
          "admin": false,
          "push": false,
          "pull": true
        },
        "allow_rebase_merge": true,
        "template_repository": null,
        "temp_clone_token": "ABTLWHOULUVAXGTRYU7OC2876QJ2O",
        "allow_squash_merge": true,
        "delete_branch_on_merge": true,
        "allow_merge_commit": true,
        "subscribers_count": 42,
        "network_count": 0,
        "license": {
          "key": "mit",
          "name": "MIT License",
          "url": "https://api.github.com/licenses/mit",
          "spdx_id": "MIT",
          "node_id": "MDc6TGljZW5zZW1pdA==",
          "html_url": "https://github.com/licenses/mit"
        },
        "forks": 1,
        "open_issues": 1,
        "watchers": 1
      }
    },
    "_links": {
      "self": {
        "href": "https://api.github.com/repos/octocat/Hello-World/pulls/1347"
      },
      "html": {
        "href": "https://github.com/octocat/Hello-World/pull/1347"
      },
      "issue": {
        "href": "https://api.github.com/repos/octocat/Hello-World/issues/1347"
      },
      "comments": {
        "href": "https://api.github.com/repos/octocat/Hello-World/issues/1347/comments"
      },
      "review_comments": {
        "href": "https://api.github.com/repos/octocat/Hello-World/pulls/1347/comments"
      },
      "review_comment": {
        "href": "https://api.github.com/repos/octocat/Hello-World/pulls/comments{/number}"
      },
      "commits": {
        "href": "https://api.github.com/repos/octocat/Hello-World/pulls/1347/commits"
      },
      "statuses": {
        "href": "https://api.github.com/repos/octocat/Hello-World/statuses/6dcb09b5b57875f334f61aebed695e2e4193db5e"
      }
    },
    "author_association": "OWNER",
    "draft": false
  }
            """.trimIndent()
        }.joinToString(",")

        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/pulls?state=open"))
                .willReturn(
                    aResponse().withBody("[$responseBody]")
                )
        )
    }

    fun stubListComments(orgName: String, repoName: String, issueId: Int, commentBodies: List<String>) {
        val responseBodies = commentBodies.mapIndexed { idx, commentBody ->
"""
    {
    "id": ${idx + 1},
    "node_id": "MDEyOklzc3VlQ29tbWVudDE=",
    "url": "https://api.github.com/repos/$orgName/$repoName/issues/comments/${idx + 1}",
    "html_url": "https://github.com/$orgName/$repoName/issues/$issueId#issuecomment-${idx + 1}",
    "body": "$commentBody",
    "user": {
      "login": "octocat",
      "id": 1,
      "node_id": "MDQ6VXNlcjE=",
      "avatar_url": "https://github.com/images/error/octocat_happy.gif",
      "gravatar_id": "",
      "url": "https://api.github.com/users/octocat",
      "type": "User",
      "site_admin": false
    },
    "created_at": "2011-04-14T16:00:49Z",
    "updated_at": "2011-04-14T16:00:49Z",
    "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/1347",
    "author_association": "collaborator"
  }
            """.trimIndent()
        }

        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/issues/$issueId/comments"))
                .willReturn(
                    aResponse().withBody("""[${responseBodies.joinToString(",")}]""")
                )
        )
    }

    fun stubGetComment(orgName: String, repoName: String, issueId: Int, commentId: Int, commentBody: String) {
        val responseBody = """
            {
              "id": $commentId,
              "node_id": "MDEyOklzc3VlQ29tbWVudDE=",
              "url": "https://api.github.com/repos/$orgName/$repoName/issues/comments/$commentId",
              "html_url": "https://github.com/$orgName/$repoName/issues/$issueId#issuecomment-$commentId",
              "body": "$commentBody",
              "user": {
                "login": "octocat",
                "id": 1,
                "node_id": "MDQ6VXNlcjE=",
                "avatar_url": "https://github.com/images/error/octocat_happy.gif",
                "gravatar_id": "",
                "type": "User",
                "site_admin": false
              },
              "created_at": "2011-04-14T16:00:49Z",
              "updated_at": "2011-04-14T16:00:49Z",
              "issue_url": "https://api.github.com/repos/$orgName/$repoName/issues/$issueId",
              "author_association": "collaborator"
            }
        """.trimIndent()

        wireMockServer.stubFor(
            get(urlEqualTo("/repos/$orgName/$repoName/issues/comments/$commentId"))
                .willReturn(
                    aResponse().withBody(responseBody)
                )
        )
    }

    fun stubUpdateComment(orgName: String, repoName: String, issueId: Int, commentId: Int) {
        val responseBody = """
            {
              "id": 1,
              "node_id": "MDEyOklzc3VlQ29tbWVudDE=",
              "url": "https://api.github.com/repos/octocat/Hello-World/issues/comments/1",
              "html_url": "https://github.com/octocat/Hello-World/issues/1347#issuecomment-1",
              "body": "Me too",
              "user": {
                "login": "octocat",
                "id": 1,
                "node_id": "MDQ6VXNlcjE=",
                "avatar_url": "https://github.com/images/error/octocat_happy.gif",
                "gravatar_id": "",
                "type": "User",
                "site_admin": false
              },
              "created_at": "2011-04-14T16:00:49Z",
              "updated_at": "2011-04-14T16:00:49Z",
              "issue_url": "https://api.github.com/repos/$orgName/$repoName/issues/$issueId",
              "author_association": "collaborator"
            }
        """.trimIndent()

        wireMockServer.stubFor(
            patch(urlEqualTo("/repos/$orgName/$repoName/issues/comments/$commentId"))
                .willReturn(
                    aResponse().withBody(responseBody)
                )
        )
    }

    fun findAddCommentRequestBodies(orgName: String, repoName: String, issueId: Int) =
        wireMockServer.findRequestsMatching(
            postRequestedFor(urlEqualTo("/repos/$orgName/$repoName/issues/$issueId/comments")).build()
        ).requests.map { it.bodyAsString }

    fun findUpdateCommentRequestBodies(orgName: String, repoName: String, commentId: Int) =
        wireMockServer.findRequestsMatching(
            patchRequestedFor(urlEqualTo("/repos/$orgName/$repoName/issues/comments/$commentId")).build()
        ).requests.map { it.bodyAsString }
}

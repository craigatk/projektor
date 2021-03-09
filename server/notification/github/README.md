## GitHub API client

Supports using the GitHub API to perform a variety of operations, such as:

* Create a comment on a pull request
* List all comments on a pull request
* Update a pull request comment
* List pull requests in a repo

Uses the [GitHub API for Java](https://github-api.kohsuke.org/) for interactions with the GitHub API.

## Code

There are a couple main entry points available in the library.

First, a high-level `GitHubCommentService` that wraps all the capabilities
needed to upsert a PR comment - that is, create a comment if one doesn't already exist or update any existing comment.

There is also more fine-grained `GitHubCommentClient` with individual methods such as `addComment`, `findCommentWithText`, `updateComment`, `findOpenPullRequests`, etc. 

## Testing

Uses [WireMock](http://wiremock.org/) to stub interactions with the GitHub API during testing.
The WireMock stubs are in the `src/testFixtures/kotlin` directory.

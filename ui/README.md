# Projektor UI

[![Code coverage percentage](https://projektorlive.herokuapp.com/repo/craigatk/projektor/project/ui-jest/badge/coverage)](https://projektorlive.herokuapp.com/repository/craigatk/projektor/project/ui-jest/coverage)

The Projektor UI is a [React](https://reactjs.org/) app written with Typescript/Javascript,
built by [Parcel](https://parceljs.org/) with [Yarn](https://yarnpkg.com)

## Development

To start the dev server:

1. Install Yarn if you don't already have it 
2. Download and install the project's dependencies with `yarn install`
3. Start the dev server with `yarn start`

This will start the server on `http://localhost:1234`

By default, when running locally the API backend points to `http://localhost:8080`
which is the default port of the server. Start up the server separately using Gradle.

You can point to an alternative API backend by creating an `.env.local` file looking like:
```
API_BASE_URL=https://projektor.my.company.com/
```

## Testing

The UI is tested by a combination of React Testing Library unit tests
and Cypress browser tests.

To run the unit tests execute `yarn test`

And to run the Cypress tests either `cy:run` to run them headless or `cy:open` 
to start the Cypress server and run the tests in Chrome.

## Formatting

The app uses [prettier](https://prettier.io/) for easy auto formatting.
To run the formatter execute `yarn format`

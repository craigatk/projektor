# Projektor UI

The Projektor UI is a [React](https://reactjs.org/) app written with Typescript/Javascript,
built by [Parcel](https://parceljs.org/) with [Yarn](https://yarnpkg.com)

## Development

To start the dev server, run `yarn start`

This will start the server on `http://localhost:1234`

By default, when running locally the API backend points to `http://localhost:8080`
which is the default port of the server. Start up the server separately using Gradle.

## Testing

The UI is tested by a combination of React Testing Library unit tests
and Cypress browser tests.

To run the unit tests execute `yarn test`

And to run the Cypress tests either `cy:run` to run them headless or `cy:open` 
to start the Cypress server and run the tests in Chrome.

## Formatting

The app uses [prettier](https://prettier.io/) for easy auto formatting.
To run the formatter execute `yarn format`
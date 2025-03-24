# What is this?

This project contains tests for a TODO application (an image can be found in the `docker` directory).  
The code was written based on the provided task.

# What is the task?

The task is described [here](task.md).

Additionally, here is a message from HR that may be useful:

> Key points to check in the test task:<br><br>
> - README file presence<br>
> - Logical project structure<br>
> - Minimal hardcoding, proper usage of constants<br>
> - Code scalability and maintainability<br>
> - Test atomicity and independence<br>
> - Code reusability<br><br>
> The primary focus is on the quality of automated tests and project structure. <br>
> The load test is an additional task, and its absence will not be considered a disadvantage in the assessment.

# How to run the tests?

1. Ensure Docker is installed and running.
2. Clone this repository.
3. In the project directory, execute the following command:

         ./gradlew resetAndDeployApp
   
   - Then if you would like to run functional tests only
        
         ./gradlew clean test --tests "functional.*" --continue allureServe
   - if load test only
   
         ./gradlew clean test --tests "load.*" --continue allureServe
4. Wait for all gradle tasks to complete (except allureServe: you will have to stop it manually).
5. The Allure Report should now be opened in your browser.

## Troubleshooting

If you have error with `resetAndDeployApp` - task execution like this one:

```
java.io.IOException: com.sun.jna.LastErrorException: [13] Permission denied
```

then [this SO Answer](https://stackoverflow.com/a/63630477) might help you


# What about test cases?

I created more than five tests for each route (**hoping that's okay**).  
The Allure Report will be available after the tests complete. [Here’s how to view it](#how-to-run-the-tests).

## What additional test cases would you add?

My inner perfectionist suggests adding cases for:

1) Sending GET and DELETE requests with incorrect body content.
2) Sending requests with unusual headers.
3) Using admin authentication headers on routes where they are not required (GET, POST, PUT).
4) Adding query parameters to routes that don’t typically use them and testing additional parameters on routes that already do.

However, I believe the most important cases are **already** implemented.

# Are there any issues with the app's current functionality?

*Deep sigh...*

First, we need to agree on the requirements.  
Here’s my list of requirements based on the [task description](task.md) and common sense.

## Requirements (constraints)

### General requirements

TODO entities must be stored as a set of objects.  
Each object must consist of the following three fields:

| Field name  |     Data Type      | Required | Limits                                                                           | Unique |
|:-----------:|:------------------:|:--------:|----------------------------------------------------------------------------------|--------|
|    `id`     |  `unsigned long`   |   Yes    | 0 (inclusive) to 2^64 (exclusive)                                                | Yes    |
|   `text`    | `text` or `string` |   Yes    | Must contain at least one non-space character.<br/>Character limit is undefined. | Yes    |
| `completed` |     `boolean`      |   Yes    | Either `true` or `false`                                                         | No     |

**A couple of important notes**:

1. No sorting algorithm is applied. Any changes to the set do not affect the order of objects.
2. Request bodies are represented as JSON nodes (either JSON objects or JSON arrays).

### `POST /todos`

1) Creates a TODO entity.
2) The request body must comply with the TODO entity structure and its constraints.
3) Basic authentication is not required.

### `GET /todos`

1) Retrieves a list of TODO entities.
2) Supports two query parameters: `offset` and `limit`.
   1) Both parameters must be unsigned long values.
   2) `offset` skips N elements from the beginning of the dataset.
   3) `limit` sets the maximum number of elements to return.
   4) If `offset` is not provided, it defaults to 0.
   5) If `limit` is not provided, it defaults to 2^64 - 1.
3) Basic authentication is not required.

### `PUT /todos/:id`

1) Updates a TODO entity.
2) `id` must be an unsigned long value.
3) The request body must comply with the TODO entity structure and its constraints.
4) Basic authentication is not required.

### `DELETE /todos/:id`

1) Deletes a TODO entity.
2) `id` must be an unsigned long value.
3) Basic authentication is required.
   > The request must include an Authorization header  
   > with `admin:admin` credentials in the Basic authentication scheme.

### `/ws` (WebSocket)

1) Each received message must follow this structure:

   | Field name | Data Type | Description                                                                             |
   |------------|-----------|-----------------------------------------------------------------------------------------|
   | `data`     | `TODO`    | A nested object containing TODO entity data.                                            |
   | `type`     | `string`  | Message type. Cannot be absent. Currently, only `new_todo` is supported.                |

2) The only implemented message type relates to new TODO creation.

## Found issues (summary)

Here are some discrepancies between the expected and actual behavior:

1) `POST /todos`
   1) Blank `text` is allowed, but it shouldn't be (a blank string contains only space symbols).
   2) Duplicate `text` values are allowed, but they shouldn't be.
2) `PUT /todos/:id`
   - If authentication is missing and the request body is invalid, `401 Unauthorized` is returned. This is incorrect; `PUT` should not require authentication.
   - If `id` is not an unsigned long, `404 Not Found` is returned instead of `400 Bad Request`.
   - Duplicate `id` values are allowed, but they shouldn’t be.
   - Duplicate `text` values are allowed, but they shouldn’t be.
3) `DELETE /todos/:id`
   - If `id` is not an unsigned long, `404 Not Found` is returned instead of `400 Bad Request`.

Fixing these issues would make all tests pass.

## Anything else?

If I were a developer I would
- restrict users from updating the `id`, even if there are no duplicates.
- either make each endpoint require to have auth or vice versa

# How could this project be improved?

With more time, I would:
1) Improve the `utils` package structure.
2) Add more comments (especially in `utils`).
3) Unify configuration variables (`host`, `port`, etc.).
4) Add linting.
5) Improve load test and find out more information about measurements and summary because the test is pretty poor
6) Make a better Git commiting policy (i.e. make atomic and more descriptive)
7) Make gradle task accept property responsible for enabling verbose container logs 
8) Beautify AssertK error output (add `Shazamcrest` - tool though it's outdated)
9) Add more Allure - steps
10) Make more readable test names so they become simpler in Allure Report

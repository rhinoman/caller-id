# caller-id

Caller ID task solution.  Contains an embedded Jetty server.


## Usage
if using leiningen: `lein run --port <portnum>`

Run an uberjar with `java -jar caller_id.jar --port <portnum>`

Defaults to port 8001 if none is specified.

API is located at `http://localhost:<port>/api`

A swagger interface is provided for testing; point your browser at 
`http://localhost:<port>`

### Run the tests

`lein test`

### Notes

Uses an in-memory H2 database to store data.  Ingesting the seed data may take a minute.
I've batched up the inserts, hopefully that helps.

The seed data appears to contain a few (I could 4) records which violate 
the <number,context> unique constraint specified in the instructions. 
I'm using a UNIQUE constraint in the H2 database to enforce this.
You'll see a few warnings for this in the log, these records are skipped and not loaded into the DB.


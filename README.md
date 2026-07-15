# cbs2sql

**cbs2sql** is a command-line utility that simplifies the migration of Couchbase data by providing tools to:

* Export documents from a Couchbase cluster to JSON files.
* Import exported JSON documents into another Couchbase cluster.
* Generate relational SQL table schemas from Couchbase JSON documents.

The tool is intended to support data migration, database modernization, schema discovery, and ETL workflows.

---

## Features

* Export Couchbase documents without modification.
* Import exported documents into another Couchbase instance.
* Infer SQL table schemas from JSON documents.
* Support multiple datasets in a single execution.
* Optionally flatten eligible nested JSON objects into parent tables.
* Simple command-line interface.

---

## Available Commands

| Command      | Description                                            |
| ------------ | ------------------------------------------------------ |
| `cb-export`  | Export Couchbase documents to local JSON files         |
| `cb-import`  | Import JSON files into a Couchbase database            |
| `gen-schema` | Generate SQL table schema from exported JSON documents |

To see the available commands:

```sh
sh cbs2sql
```

To view the usage of a specific command:

```
<cbs2sql> 
###################################################################################
#                                                                                 #
#                         Couchbase To SQL Migration                              #
#                                                                                 #
# Commands:                                                                       #
#                                                                                 #
#  gen-schema          Generate the sql schema from couchbase doc                 #
#  cb-export           Export the document and dump it to a local file            #
#  cb-import           Import the documents to destination couchbase db           #
#                                                                                 #
###################################################################################

Type "help" to see the available commands
Use "command help" for usage of "command". [Example: <command> help]

```

Example 1:

```sh
cb-export help
```

```
<cbs2sql> cb-export help

Description                        : Export the documents of the remote couchbase db, as-is
Usage                              : cb-export [OPTIONS] ...
Example                            : cb-export -c cluster_1 -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource -s travel -l booking -d dataset_1,dataset_2 -n type -o /tmp

The options are:

-c [--cluster] <cluster_name>            Couchbase cluster name
-h [--host] <host_name>                  Couchbase host [default: localhost]
-p [--port] <port>                       Couchbase server port [default: 8091]
-u [--user] <username>                   Couchbase db user name
-w [--password] <password>               Couchbase db password
-b [--bucket] <bucket_name>              Couchbase bucket to be queried
-s [--scope] <scope_name>                Couchbase scope within a bucket to be queried [default: _default]
-l [--collection] <collection_name>      Couchbase collection within a scope to be queried [default: _default]
-d [--data-set] <dataset_name>           Comma separated datasets, or a filename with newline separated dataset
-n [--index-column] <idx_column>         The secondary index column that differentiates the documents within a single bucket
-o [--out-dir] <dir_name>                Directory name where the files will be dumped
-v [--verbose] [Y | N]                   Whether verbose logging will be enabled [Defau;t: N]

```

Example 2:

```sh
cb-import help
```

```
<cbs2sql> cb-import help

Description                        : Export the documents of the remote couchbase db, as-is
Usage                              : cb-import [OPTIONS] ...
Example                            : cb-import -c cluster_1 -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource -s travel -l booking -i /tmp

The options are:

-c [--cluster] <cluster_name>            Couchbase cluster name
-h [--host] <host_name>                  Couchbase host [default: localhost]
-p [--port] <port>                       Couchbase server port [default: 8091]
-u [--user] <username>                   Couchbase db user name
-w [--password] <password>               Couchbase db password
-b [--bucket] <bucket_name>              Couchbase bucket where the data to be inserted
-s [--scope] <scope_name>                Couchbase scope within a bucket data to be inserted [default: _default]
-l [--collection] <collection_name>      Couchbase collection within a scope data to be inserted [default: _default]
-i [--in-dir] <dir_name>                 Directory name from where the document files will be read
-f [--file-name] <file_name>             Specific json file to be imported. If this option is null, then all the files from the --in-dir will be imported
-v [--verbose] [Y | N]                   Whether verbose logging will be enabled [Defau;t: N]

```

Example 2:

```sh
gen-schema help
```

```
<cbs2sql> gen-schema help

Description                        : Generate the sql schema from couchbase doc
Usage                              : gen-schema [OPTIONS] ...
Example                            : gen-schema -c <...> -h slc05mkt.org.com -p 8091 -u sudip -w s83@! -b resource

The options are:

-d [--data-set] <dataset_name>           Name of the data set. If not provided, it will be derived from filename
-i [--in-dir] <directory_name>           Directory where the json file(s) are kept
-f [--file-name] <filename>              Json filename. If directory name is not specified, this should be a fully qualified name. Otherwise, it can be a simple name or extension (*.json)
-l [--flatten-child] [Y|N]               If Y, eligible nested child objects are flattened into the parent table
-v [--verbose] [Y | N]                   Whether verbose logging will be enabled [Defau;t: N]

```

---

## Export Documents

Exports Couchbase documents **as-is** to the local filesystem.

### Usage

```bash
cb-export [OPTIONS]
```

### Example

```bash
cb-export \
  -c cluster_1 \
  -h slc05mkt.org.com \
  -p 8091 \
  -u sudip \
  -w ******** \
  -b resource \
  -s travel \
  -l booking \
  -d dataset_1,dataset_2 \
  -n type \
  -o /tmp/export
```

### Options

| Option                 | Description                                               | Default     |
| ---------------------- | --------------------------------------------------------- | ----------- |
| `-c`, `--cluster`      | Couchbase cluster name                                    |             |
| `-h`, `--host`         | Couchbase host                                            | `localhost` |
| `-p`, `--port`         | Couchbase server port                                     | `8091`      |
| `-u`, `--user`         | Couchbase username                                        |             |
| `-w`, `--password`     | Couchbase password                                        |             |
| `-b`, `--bucket`       | Bucket to export                                          |             |
| `-s`, `--scope`        | Scope name                                                | `_default`  |
| `-l`, `--collection`   | Collection name                                           | `_default`  |
| `-d`, `--data-set`     | Comma-separated datasets or a file containing datasets    |             |
| `-n`, `--index-column` | Secondary index column used to distinguish document types |             |
| `-o`, `--out-dir`      | Output directory for exported JSON files                  |             |
| `-v`, `--verbose`      | Enable verbose logging (`Y`/`N`)                          | `N`         |

---

## Import Documents

Imports previously exported JSON files into a Couchbase cluster.

### Usage

```bash
cb-import [OPTIONS]
```

### Example

```bash
cb-import \
  -c cluster_2 \
  -h slc05mkt.org.com \
  -p 8091 \
  -u sudip \
  -w ******** \
  -b resource \
  -s travel \
  -l booking \
  -i /tmp/export
```

### Options

| Option               | Description                                                                                  | Default     |
| -------------------- | -------------------------------------------------------------------------------------------- | ----------- |
| `-c`, `--cluster`    | Couchbase cluster name                                                                       |             |
| `-h`, `--host`       | Couchbase host                                                                               | `localhost` |
| `-p`, `--port`       | Couchbase server port                                                                        | `8091`      |
| `-u`, `--user`       | Couchbase username                                                                           |             |
| `-w`, `--password`   | Couchbase password                                                                           |             |
| `-b`, `--bucket`     | Destination bucket                                                                           |             |
| `-s`, `--scope`      | Destination scope                                                                            | `_default`  |
| `-l`, `--collection` | Destination collection                                                                       | `_default`  |
| `-i`, `--in-dir`     | Directory containing exported JSON files                                                     |             |
| `-f`, `--file-name`  | Import a specific JSON file. If omitted, all JSON files in the input directory are imported. |             |
| `-v`, `--verbose`    | Enable verbose logging (`Y`/`N`)                                                             | `N`         |

---

## Generate SQL Schema

Generates relational SQL table definitions from exported Couchbase JSON documents.

### Usage

```bash
gen-schema [OPTIONS]
```

### Example

```bash
gen-schema \
  -i /tmp/export \
  -l Y
```

### Options

| Option                  | Description                                                                                                                                       | Default |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| `-d`, `--data-set`      | Dataset name. If omitted, the dataset is derived from the filename.                                                                               |         |
| `-i`, `--in-dir`        | Directory containing exported JSON files                                                                                                          |         |
| `-f`, `--file-name`     | JSON filename. If `--in-dir` is omitted, this must be a fully qualified path. Wildcards such as `*.json` are supported when used with `--in-dir`. |         |
| `-l`, `--flatten-child` | Flatten eligible nested child objects into the parent table (`Y`/`N`)                                                                             | `N`     |
| `-v`, `--verbose`       | Enable verbose logging (`Y`/`N`)                                                                                                                  | `N`     |

---

## Typical Workflow

The following workflow is recommended when migrating Couchbase data.

### Step 1 - Export Documents

```text
Source Couchbase
        │
        ▼
 cb-export
        │
        ▼
   JSON Files
```

### Step 2 - Generate SQL Schema

```text
JSON Files
     │
     ▼
gen-schema
     │
     ▼
CREATE TABLE statements
```

### Step 3 - Import Documents (Optional)

```text
JSON Files
     │
     ▼
cb-import
     │
     ▼
Destination Couchbase
```

---

## Input and Output

### Export

Produces one or more JSON files representing the exported Couchbase documents.

### Schema Generation

Produces SQL `CREATE TABLE` definitions inferred from the JSON document structure.

### Import

Reads exported JSON files and inserts them into the target Couchbase bucket, scope, and collection.

---

## Dataset Support

The `--data-set` option accepts either:

* A comma-separated list of datasets

```text
customer,order,invoice
```

or

* A text file containing one dataset per line

```text
customer
order
invoice
```

---

## Flattening Nested Objects

The `--flatten-child` option controls how nested JSON objects are represented.

When enabled (`Y`), eligible nested child objects are merged into the parent table, reducing the number of generated relational tables.

When disabled (`N`), nested objects are represented as separate relational entities where appropriate.

---

## Logging

Verbose logging can be enabled for every command:

```bash
-v Y
```

This is useful for debugging, troubleshooting, and monitoring long-running migrations.

---

## Error Handling

The tool reports meaningful errors for common failure scenarios including:

* Invalid Couchbase credentials
* Connection failures
* Missing buckets, scopes, or collections
* Invalid input directories
* Malformed JSON files
* File permission issues
* Schema generation failures

---

## Best Practices

* Export data before generating SQL schemas.
* Review generated schemas before creating database tables.
* Use verbose logging while validating a new migration.
* Store exported JSON files in versioned or backed-up locations.
* Test imports in a non-production Couchbase environment before running against production.

---

## Supported Use Cases

* Couchbase database migration
* SQL schema discovery
* ETL pipelines
* Data archival
* Analytics platform onboarding
* NoSQL to relational database modernization

---

## License

Specify the appropriate license for your project (MIT, Apache 2.0, GPL, etc.).


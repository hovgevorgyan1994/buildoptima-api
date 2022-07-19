# Filtering & Sorting for Architect Portal

## Overview

A general dynamic filtering approach is implemented for all Architect Portal entities like news,
songwriters, playlist etc. With this approach it will be possible to generate dynamic filtering with
different condition combinations (OR/AND)
.

## Example

Below is the sample structure of payload to be sent in search request:

```json
{
  "skip": 0,
  "take": 10,
  "sort": [
    {
      "field": "name",
      "order": "asc"
    }
  ],
  "filter": {
    "and": [
      {
        "operation": "eq",
        "name": "name",
        "value": "Royalties"
      },
      {
        "or": [
          {
            "operation": "gt",
            "name": "updatedAt",
            "value": "2018-11-30T18:35:24.00Z"
          },
          {
            "operation": "gt",
            "name": "createdAt",
            "value": "2018-11-30T18:35:24.00Z"
          }
        ]
      }
    ]
  }
}
```

## Explanation

The JSON payload can be empty (e.g {}) which will return all the result without sorting and
filtering. The `skip` and `take` fields must have numerical values (e.g., skip=0, take=10), and if
values aren't provided then will be 0 and 10 respectfully. The `sort`->`field` and `filter`->`name`
fields must consist only from the fields which can be provided per entity.

### Skip/Take

- `skip` - Specifying how many items result needs to skip (default 0), means offset from start
- `take` - Specifying how many items result needs to return starting give offset

### Sort

The `sort` object is a list of object with the following fields:

- `field` - The filed name result will be sorted
- `order` - The order of sorting, possible values are `asc` and `desc`

### Filter

Filer object can contain two type of objects: `or`/`and` (predicate) and combination of these
object. The predicate object has the following structure

- `operation` - The operator (e.g. `eq`,`gt`)
- `name` - The field name (e.g. `firstName`)
- `value` - Single value of filtering
- `values` - Multiple values of filtering (used with `in` operation)

### Filter Operations:

- `eq`       (result in case of exact matching)
- `like`     (result in case if contains any value like)
- `in`       (result if any value is in the scope of provided values)
- `gt`       (result if any value is greater than provided value)
- `ge`       (result if any value is greater than or equal to provided value)
- `lt`       (result if any value is less than provided value)
- `le`       (result if any value is less than or equal to provided value)

## Field Definitions for Architect Portal Entities

### FAQ Category Entity

#### Filtering Fields

| Field        | Type      | Operations                         | Notes                                                                                                           |
|:-------------|:----------|:-----------------------------------|:----------------------------------------------------------------------------------------------------------------|
| `name`       | String    | `eq`, `like`, `in`                 | Must be provided with `value` field in case of `eq`,`like`, and with `values` for `in`                          |
| `createdAt`  | DateTime  | `eq`, `in`, `gt`, `ge`, `lt`, `le` | Must be provided with `value` field in case of `eq`,`like`, `gt`, `ge`, `lt`, `le`, and with `values` for `in`  |
| `updatedAt`  | DateTime  | `eq`, `in`, `gt`, `ge`, `lt`, `le` | Must be provided with `value` field in case of `eq`,`like`, `gt`, `ge`, `lt`, `le`, and with `values` for `in`  |
| `createdBy`  | UUID      | `eq` `in`                          | Must be provided with `value` field in case of `eq`, and with `values` for `in`                                 |
| `updatedBy`  | UUID      | `eq` `in`                          | Must be provided with `value` field in case of `eq`, and with `values` for `in`                                 |
#### Sorting Fields

- `name`
- `createdAt`
- `updatedAt`
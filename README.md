# graphs

A Clojure library for doing graph-theory stuff on small undirected
graphs.

## Usage

Leiningen: `[com.gfredericks/graphs "0.2.0"]`

## Serialization

To use the data reader make sure you require
`com.gfredericks.graphs.graph6` and that the following entry is
present in your `/resources/data_readers.clj`:

``` clojure
{graphs/graph com.gfredericks.graphs.graph6/graph6->vector-graph}
```

## License

Copyright © 2013 Gary Fredericks

Distributed under the Eclipse Public License, the same as Clojure.

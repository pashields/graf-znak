# graf-znak

A Clojure library designed to run arbitrary accumulations across groups. It is written using [core.typed](https://github.com/clojure/core.typed).

## Usage

graf-znak is composed of a few main abstractions:

* Accumulator - a wrapper around a function that will, well, accumulate for you
* Hook - a set of attribute names and a set of accumulators that will run on groups in that attribute set
* Net - a stateful collection of hooks and their backing storage

Say, for example, that we have a list of people, like

```clojure
({:name "Pat Shields" :age 28 :city "Durham"}
 {:name "John Smith" :age 34 :city "Raleigh"}
 ...)
```

Now, say we want to find the number of people of each age in each city. So we want to end up with something like

```clojure
{(28 "Durham") {:count 1} (34 "Raleigh") {:count 1}, ...}
```

We can do that by doing the following:
```clojure
(def counter 
  (->Accumulator :count
                 (fn [state input]
                   (let [state (or state 0)]
                     (inc state)))))
(def hook (->Hook [:name :age] [counter]))
(def net (create-net hook atom-storage/factory))
(doseq [person people] (send-net net person))
(check-net net hook) ; {(28 "Durham") {:count 1} ...)
```

## Status

So very pre-alpha. But you probably want ztellman's [narrator](https://github.com/ztellman/narrator) anyways.

This is really just an idea I am playing with. Mostly I'm hoping someone will tell me I am crazy and that there is a way easier way to accomplish this.

## License

Copyright 2014 Patrick Shields

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
